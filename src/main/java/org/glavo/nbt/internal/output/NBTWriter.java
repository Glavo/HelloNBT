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
package org.glavo.nbt.internal.output;

import org.glavo.nbt.MinecraftEdition;
import org.glavo.nbt.internal.IOUtils;
import org.glavo.nbt.tag.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import static org.glavo.nbt.tag.Tag.readTag;

public final class NBTWriter implements Closeable, Flushable {

    private final OutputStream outputStream;
    private ByteBuffer buffer;
    private final MinecraftEdition edition;

    public NBTWriter(OutputStream outputStream, MinecraftEdition edition) {
        this.outputStream = outputStream;
        this.buffer = ByteBuffer.allocate(8192).order(edition.byteOrder());
        this.edition = edition;
    }

    @Override
    public void flush() throws IOException {
        if (buffer.position() > 0) {
            outputStream.write(buffer.array(), 0, buffer.position());
            buffer.clear();
        }
    }

    @Override
    public void close() throws IOException {
        try {
            flush();
        } finally {
            outputStream.close();
        }
    }

    public void writeTag(Tag tag) throws IOException {
        writeByte(tag.getType().id()); // implicit null check
        writeString(tag.getName());
        writeContent(tag);
    }

    private void writeContent(Tag tag) throws IOException {
        if (tag instanceof ByteTag byteTag) {
            writeByte(byteTag.get());
        } else if (tag instanceof ShortTag shortTag) {
            writeShort(shortTag.get());
        } else if (tag instanceof IntTag intTag) {
            writeInt(intTag.get());
        } else if (tag instanceof LongTag longTag) {
            writeLong(longTag.get());
        } else if (tag instanceof FloatTag floatTag) {
            writeFloat(floatTag.get());
        } else if (tag instanceof DoubleTag doubleTag) {
            writeDouble(doubleTag.get());
        } else if (tag instanceof StringTag stringTag) {
            writeString(stringTag.get());
        } else if (tag instanceof ByteArrayTag byteArrayTag) {
            writeByteArray(IOUtils.TAG_UNSAFE.getInternalArray(byteArrayTag));
        } else if (tag instanceof IntArrayTag intArrayTag) {
            writeIntArray(IOUtils.TAG_UNSAFE.getInternalArray(intArrayTag));
        } else if (tag instanceof LongArrayTag longArrayTag) {
            writeLongArray(IOUtils.TAG_UNSAFE.getInternalArray(longArrayTag));
        } else if (tag instanceof ListTag<?> listTag) {
            writeByte(listTag.getElementType() != null ? listTag.getElementType().id() : 0);
            writeInt(listTag.size());

            for (Tag subTag : listTag) {
                writeContent(subTag);
            }
        } else if (tag instanceof CompoundTag<?> compoundTag) {
            for (Tag subTag : compoundTag) {
                writeTag(subTag);
            }

            writeByte((byte) 0x00);
        } else {
            throw new AssertionError("Unexpected tag type: " + tag.getType());
        }
    }

    /// Flushes the buffer if it is not large enough.
    private void flushBuffer(int required) throws IOException {
        if (buffer.remaining() < required) {
            if (buffer.position() > 0) {
                outputStream.write(buffer.array(), 0, buffer.position());
            }

            if (buffer.capacity() >= required) {
                buffer.clear();
            } else {
                buffer = ByteBuffer.allocate(Math.max(required, buffer.capacity() * 2)).order(buffer.order());
            }
        }
    }

    private void writeByteArray(byte[] value) throws IOException {
        flushBuffer(Integer.BYTES + value.length);
        buffer.putInt(value.length);
        buffer.put(value);
    }

    private void writeIntArray(int[] value) throws IOException {
        if (value.length > Integer.MAX_VALUE / Integer.BYTES - 8) {
            throw new IOException("Array length too large");
        }

        writeInt(value.length);

        int bytes = value.length * Integer.BYTES;
        flushBuffer(bytes);
        buffer.asIntBuffer().put(value);
        buffer.position(buffer.position() + bytes);
    }

    private void writeLongArray(long[] value) throws IOException {
        if (value.length > Integer.MAX_VALUE / Long.BYTES - 8) {
            throw new IOException("Array length too large");
        }

        writeInt(value.length);

        int bytes = value.length * Long.BYTES;
        flushBuffer(bytes);
        buffer.asLongBuffer().put(value);
        buffer.position(buffer.position() + bytes);
    }

    private void writeByte(byte value) throws IOException {
        flushBuffer(Byte.BYTES);
        buffer.put(value);
    }

    private void writeUnsignedByte(int value) throws IOException {
        flushBuffer(Byte.BYTES);
        buffer.put((byte) value);
    }

    private void writeShort(short value) throws IOException {
        flushBuffer(Short.BYTES);
        buffer.putShort(value);
    }

    private void writeUnsignedShort(int value) throws IOException {
        flushBuffer(Short.BYTES);
        buffer.putShort((short) value);
    }

    private void writeInt(int value) throws IOException {
        flushBuffer(Integer.BYTES);
        buffer.putInt(value);
    }

    private void writeUnsignedInt(long value) throws IOException {
        flushBuffer(Integer.BYTES);
        buffer.putInt((int) value);
    }

    private void writeLong(long value) throws IOException {
        flushBuffer(Long.BYTES);
        buffer.putLong(value);
    }

    private void writeFloat(float value) throws IOException {
        flushBuffer(Float.BYTES);
        buffer.putFloat(value);
    }

    private void writeDouble(double value) throws IOException {
        flushBuffer(Double.BYTES);
        buffer.putDouble(value);
    }

    private static int encodedMutf8Length(String value, int asciiLength) {
        int length = value.length();

        for (int i = asciiLength; i < value.length(); i++) {
            char ch = value.charAt(i);

            if (ch >= 0x800) {
                length += 2;
            } else if (ch >= 0x80 || ch == 0) {
                length += 1;
            }
        }

        return length;
    }


    @SuppressWarnings("deprecation")
    private void writeString(String value) throws IOException {
        int asciiLength = 0;
        while (asciiLength < value.length()) {
            char ch = value.charAt(asciiLength);
            if (ch > 0 && ch < 128) {
                asciiLength++;
            } else {
                break;
            }
        }

        if (asciiLength == value.length()) {
            // Fast path for ASCII strings

            if (value.length() > 65535) {
                throw new UTFDataFormatException("String too long: " + value);
            }

            writeUnsignedShort(value.length());
            flushBuffer(value.length());

            value.getBytes(0, value.length(), buffer.array(), buffer.position());
            buffer.position(buffer.position() + value.length());

            return;
        }

        if (edition == MinecraftEdition.BEDROCK_EDITION) {
            // Need Optimization
            byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            if (value.length() > 65535) {
                throw new UTFDataFormatException("String too long: " + value);
            }
            writeUnsignedShort(bytes.length);
            flushBuffer(bytes.length);
            buffer.put(bytes);
            return;
        }

        // Slow path for non-ASCII strings
        int encodedLength = encodedMutf8Length(value, asciiLength);
        if (encodedLength > 65535) {
            throw new UTFDataFormatException("String too long: " + value);
        }

        writeUnsignedShort(encodedLength);

        flushBuffer(encodedLength);

        byte[] array = buffer.array();
        int offset = buffer.position();

        buffer.position(buffer.position() + encodedLength);

        value.getBytes(0, asciiLength, array, offset);
        offset += asciiLength;

        for (int i = asciiLength; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch > 0 && ch < 0x80) {
                array[offset++] = (byte) ch;
            } else if (ch >= 0x800) {
                array[offset++] = (byte) (0xE0 | ch >> 12 & 0x0F);
                array[offset++] = (byte) (0x80 | ch >> 6 & 0x3F);
                array[offset++] = (byte) (0x80 | ch & 0x3F);
            } else {
                array[offset++] = (byte) (0xC0 | ch >> 6 & 0x1F);
                array[offset++] = (byte) (0x80 | ch & 0x3F);
            }
        }

        if (offset != buffer.position()) {
            throw new AssertionError("Unexpected buffer position: " + offset + " != " + buffer.position());
        }
    }
}
