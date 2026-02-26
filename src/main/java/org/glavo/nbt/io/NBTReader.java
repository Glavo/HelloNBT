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

import org.glavo.nbt.tag.*;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public final class NBTReader implements Closeable {
    private final ByteOrder byteOrder;
    private final InputStream inputStream;
    private ByteBuffer buffer;

    /// Used for reading UTF-8 strings
    private @Nullable StringBuilder charsBuffer;

    NBTReader(ByteOrder byteOrder, InputStream inputStream) {
        this.byteOrder = byteOrder;
        this.inputStream = inputStream;
        this.buffer = ByteBuffer.allocate(8192).order(byteOrder);
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

        Tag tag = type.createTag(readString());
        readContent(tag);
        return tag;
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
            NBTIO.TAG_UNSAFE.setInternalArray(byteArrayTag, readByteArray());
        } else if (tag instanceof IntArrayTag intArrayTag) {
            NBTIO.TAG_UNSAFE.setInternalArray(intArrayTag, readIntArray());
        } else if (tag instanceof LongArrayTag longArrayTag) {
            NBTIO.TAG_UNSAFE.setInternalArray(longArrayTag, readLongArray());
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
                Tag subTag = elementType.createTag("");
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
        } else {
            buffer.compact();
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
            return new String(array, offset, len, StandardCharsets.UTF_8);
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
            return new String(array, offset, asciiLen, StandardCharsets.US_ASCII);
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
}
