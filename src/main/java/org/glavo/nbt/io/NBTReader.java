/*
 * Copyright 2026 Glavo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.glavo.nbt.io;

import org.glavo.nbt.internal.StringCache;
import org.glavo.nbt.tag.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class NBTReader implements Closeable {

    private static final Tag.Unsafe TAG_UNSAFE = Tag.Unsafe.getUnsafe(MethodHandles.lookup());

    // Used for reading UTF-8 strings
    private static final StringCache CACHE = new StringCache(
            "data", "Data", "DataVersion"
            // TODO: More tag names
    );

    public static NBTReader create(InputStream inputStream, Options options) {
        return new NBTReader(inputStream, options);
    }

    public static Tag readTag(InputStream inputStream) throws IOException {
        return readTag(inputStream, Options.getDefault());
    }

    public static Tag readTag(InputStream inputStream, Options options) throws IOException {
        try (var reader = new NBTReader(inputStream, options)) {
            Tag tag = reader.readTag();
            if (tag == null) {
                throw new IOException("No tag found");
            }
            return tag;
        }
    }

    public static CompoundTag<?> readCompoundTag(InputStream inputStream) throws IOException {
        return readCompoundTag(inputStream, Options.getDefault());
    }

    public static CompoundTag<?> readCompoundTag(InputStream inputStream, Options options) throws IOException {
        Tag rootTag = readTag(inputStream, options);
        if (rootTag instanceof CompoundTag<?> compoundTag) {
            return compoundTag;
        } else {
            throw new IOException("Expected a compound tag, but got " + rootTag);
        }
    }

    private final InputStream inputStream;
    private final ByteOrder byteOrder;
    private ByteBuffer buffer;

    /// Used for reading UTF-8 strings
    private @Nullable StringBuilder charsBuffer;

    @VisibleForTesting
    NBTReader(InputStream inputStream, Options options) {
        this.inputStream = inputStream;
        this.byteOrder = options.byteOrder;
        this.buffer = ByteBuffer.allocate(8192).order(byteOrder).flip();
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    // High-level read methods

    public @Nullable Tag readTag() throws IOException {
        byte tagByte = readByte();
        var type = TagType.getById(tagByte);
        if (type == null) {
            throw new IOException("Invalid tag type: %02x".formatted(Byte.toUnsignedInt(tagByte)));
        }

        if (type == TagType.END) {
            return null;
        }

        Tag tag = createTag(type, readString());
        readContent(tag);
        return tag;
    }

    private static Tag createTag(TagType type, String name) {
        return switch (type) {
            case END -> throw new UnsupportedOperationException("Cannot create an END tag");
            case BYTE -> new ByteTag(name);
            case SHORT -> new ShortTag(name);
            case INT -> new IntTag(name);
            case LONG -> new LongTag(name);
            case FLOAT -> new FloatTag(name);
            case DOUBLE -> new DoubleTag(name);
            case STRING -> new StringTag(name);
            case BYTE_ARRAY -> new ByteArrayTag(name);
            case INT_ARRAY -> new IntArrayTag(name);
            case LONG_ARRAY -> new LongArrayTag(name);
            case LIST -> new ListTag<>(name, TagType.END);
            case COMPOUND -> new CompoundTag<>(name);
        };
    }

    private void readContent(Tag tag) throws IOException {
        if (tag instanceof ByteTag byteTag) {
            byteTag.set(readByte());
        } else if (tag instanceof ShortTag shortTag) {
            shortTag.set(readShort());
        } else if (tag instanceof IntTag intTag) {
            intTag.set(readInt());
        } else if (tag instanceof LongTag longTag) {
            longTag.set(readLong());
        } else if (tag instanceof FloatTag floatTag) {
            floatTag.set(readFloat());
        } else if (tag instanceof DoubleTag doubleTag) {
            doubleTag.set(readDouble());
        } else if (tag instanceof StringTag stringTag) {
            stringTag.set(readString());
        } else if (tag instanceof ByteArrayTag byteArrayTag) {
            TAG_UNSAFE.setInternalArray(byteArrayTag, readByteArray());
        } else if (tag instanceof IntArrayTag intArrayTag) {
            TAG_UNSAFE.setInternalArray(intArrayTag, readIntArray());
        } else if (tag instanceof LongArrayTag longArrayTag) {
            TAG_UNSAFE.setInternalArray(longArrayTag, readLongArray());
        } else if (tag instanceof ListTag<?> listTag) {
            byte elementTypeId = readByte();
            var elementType = TagType.getById(elementTypeId);

            if (elementType == null) {
                throw new IOException("Invalid element type: %02x".formatted(Byte.toUnsignedInt(elementTypeId)));
            }

            listTag.setElementType(elementType);

            int count = readInt();
            if (count < 0) {
                throw new IOException("Invalid list length: " + Integer.toUnsignedLong(count));
            }

            if (elementType == TagType.END && count != 0) {
                throw new IOException("Cannot create a non-empty list with element type END");
            }

            @SuppressWarnings("unchecked")
            var uncheckedListTag = (ListTag<Tag>) listTag;
            for (int i = 0; i < count; i++) {
                Tag subTag = createTag(elementType, "");
                readContent(subTag);
                uncheckedListTag.add(subTag);
            }
        } else if (tag instanceof CompoundTag<?> compoundTag) {
            @SuppressWarnings("unchecked")
            var uncheckCompoundTag = (CompoundTag<Tag>) compoundTag;

            int count = 0;

            Tag subTag;
            while ((subTag = readTag()) != null) {
                count++;
                uncheckCompoundTag.add(subTag);
            }

            if (count != compoundTag.size()) {
                throw new IOException("Duplicate subtags found in compound tag");
            }
        } else {
            throw new AssertionError("Unexpected tag type: " + tag.getType());
        }
    }

    // Low-level read methods

    private void fillBuffer(int required) throws IOException {
        if (buffer.remaining() >= required) {
            return;
        }

        if (buffer.capacity() < required) {
            ByteBuffer newBuffer = ByteBuffer.allocate(Math.max(required, buffer.capacity() * 2)).order(byteOrder);
            newBuffer.put(buffer);
            newBuffer.flip();
            buffer = newBuffer;
        } else if (buffer.position() > 0) {
            buffer.compact();
            buffer.flip();
        }

        while (buffer.remaining() < required) {
            int read = inputStream.read(buffer.array(), buffer.limit(), buffer.capacity() - buffer.limit());
            if (read < 0) {
                throw new EOFException("Unexpected end of stream");
            }
            buffer.limit(buffer.limit() + read);
        }
    }

    public byte[] readByteArray() throws IOException {
        int len = readInt();
        if (len < 0 || len >= Integer.MAX_VALUE - 8) {
            throw new IOException("Array length too large");
        }

        fillBuffer(len);

        byte[] array = new byte[len];
        buffer.get(array);
        return array;
    }

    public int[] readIntArray() throws IOException {
        int len = readInt();
        if (len < 0 || len > Integer.MAX_VALUE / Integer.BYTES - 8) {
            throw new IOException("Array length too large");
        }

        int bytes = len * Integer.BYTES;
        fillBuffer(bytes);

        int[] result = new int[len];
        buffer.asIntBuffer().get(result);
        buffer.position(buffer.position() + bytes);
        return result;
    }

    public long[] readLongArray() throws IOException {
        int len = readInt();
        if (len < 0 || len > Integer.MAX_VALUE / Long.BYTES - 8) {
            throw new IOException("Array length too large");
        }

        int bytes = len * Long.BYTES;
        fillBuffer(bytes);

        long[] result = new long[len];
        buffer.asLongBuffer().get(result);
        buffer.position(buffer.position() + bytes);
        return result;
    }

    /// Read a byte from the input stream.
    public byte readByte() throws IOException {
        fillBuffer(Byte.BYTES);
        return buffer.get();
    }

    /// Read an unsigned byte from the input stream.
    public int readUnsignedByte() throws IOException {
        return Byte.toUnsignedInt(readByte());
    }

    /// Read a short from the input stream.
    public short readShort() throws IOException {
        fillBuffer(Short.BYTES);
        return buffer.getShort();
    }

    /// Read an unsigned short from the input stream.
    public int readUnsignedShort() throws IOException {
        return Short.toUnsignedInt(readShort());
    }

    /// Read an int from the input stream.
    public int readInt() throws IOException {
        fillBuffer(Integer.BYTES);
        return buffer.getInt();
    }

    /// Read an unsigned int from the input stream.
    public long readUnsignedInt() throws IOException {
        return Integer.toUnsignedLong(readInt());
    }

    /// Read a long from the input stream.
    public long readLong() throws IOException {
        fillBuffer(Long.BYTES);
        return buffer.getLong();
    }

    /// Read a float from the input stream.
    public float readFloat() throws IOException {
        fillBuffer(Float.BYTES);
        return buffer.getFloat();
    }

    /// Read a double from the input stream.
    public double readDouble() throws IOException {
        fillBuffer(Double.BYTES);
        return buffer.getDouble();
    }


    private static String getString(byte[] array, int offset, int length) {
        String cached = CACHE.get(array, offset, length);
        return cached != null ? cached : new String(array, offset, length, StandardCharsets.UTF_8);
    }

    /// Read a string from the input stream.
    ///
    /// For big-endian byte order, the string is encoded in [modified UTF-8](https://en.wikipedia.org/wiki/UTF-8#Modified_UTF-8);
    /// For little-endian byte order, the string is encoded in standard UTF-8.
    public String readString() throws IOException {
        int len = readUnsignedShort();

        if (len == 0) {
            return "";
        }

        fillBuffer(len);

        byte[] array = buffer.array();
        int offset = buffer.position();
        int limit = offset + len;

        buffer.position(limit);

        // For Minecraft Bedrock Edition, the string is encoded in standard UTF-8
        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            return getString(array, offset, len);
        }

        // Scan the number of ASCII characters in the prefix
        int asciiLen = 0;
        for (int i = offset; i < limit; i++) {
            if (array[i] > 0) {
                asciiLen++;
            } else {
                break;
            }
        }

        // If all characters are ASCII, return the string directly
        if (asciiLen == len) {
            return getString(array, offset, asciiLen);
        }

        // Slow path
        if (charsBuffer != null) {
            charsBuffer.setLength(0);
        } else {
            charsBuffer = new StringBuilder(len);
        }

        int c, char2, char3;
        int i = offset;

        while (i < limit) {
            c = (int) array[i] & 0xff;
            if (c > 127) break;
            i++;
            charsBuffer.append((char) c);
        }

        while (i < limit) {
            c = (int) array[i] & 0xff;
            switch (c >> 4) {
                case 0, 1, 2, 3, 4, 5, 6, 7 -> {
                    /* 0xxxxxxx*/
                    i++;
                    charsBuffer.append((char) c);
                }
                case 12, 13 -> {
                    /* 110x xxxx   10xx xxxx*/
                    i += 2;
                    if (i > limit)
                        throw new IllegalArgumentException("malformed input: partial character at end");
                    char2 = (int) array[i - 1] & 0xff;
                    if ((char2 & 0xC0) != 0x80)
                        throw new IllegalArgumentException("malformed input around byte " + (i - 1));
                    charsBuffer.append((char) (((c & 0x1F) << 6) |
                            (char2 & 0x3F)));
                }
                case 14 -> {
                    /* 1110 xxxx  10xx xxxx  10xx xxxx */
                    i += 3;
                    if (i > limit)
                        throw new IllegalArgumentException("malformed input: partial character at end");
                    char2 = array[i - 2];
                    char3 = array[i - 1];
                    if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
                        throw new IllegalArgumentException("malformed input around byte " + (i - 1));
                    charsBuffer.append((char) (((c & 0x0F) << 12) |
                            ((char2 & 0x3F) << 6) |
                            (char3 & 0x3F)));
                }
                default ->
                    /* 10xx xxxx,  1111 xxxx */
                        throw new IllegalArgumentException("malformed input around byte " + i);
            }
        }
        return charsBuffer.toString();
    }

    /// Options for reading NBT data.
    public static final class Options {

        private static final Options DEFAULT = new Options(ByteOrder.BIG_ENDIAN);

        /// Returns the default options.
        public static Options getDefault() {
            return DEFAULT;
        }

        /// Creates a new builder for options.
        public static Builder newBuilder() {
            return new Builder();
        }

        private final ByteOrder byteOrder;

        private Options(ByteOrder byteOrder) {
            this.byteOrder = byteOrder;
        }

        public static final class Builder {
            private ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;

            public Options build() {
                return new Options(byteOrder);
            }

            @Contract("_ -> this")
            public Builder byteOrder(ByteOrder byteOrder) {
                this.byteOrder = Objects.requireNonNull(byteOrder);
                return this;
            }

            private Builder() {
            }
        }
    }
}
