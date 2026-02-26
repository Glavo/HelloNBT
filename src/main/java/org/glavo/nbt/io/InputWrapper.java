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

import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/// An InputStream wrapper with configurable byte order and internal buffering for reading NBT data.
final class InputWrapper implements Closeable {
    private final ByteOrder byteOrder;
    private final InputStream inputStream;
    private ByteBuffer buffer;

    /// Used for reading UTF-8 strings
    private @Nullable StringBuilder charsBuffer;

    InputWrapper(ByteOrder byteOrder, InputStream inputStream) {
        this.byteOrder = byteOrder;
        this.inputStream = inputStream;
        this.buffer = ByteBuffer.allocate(8192).order(byteOrder);
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

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

    /// @see <a href="https://en.wikipedia.org/wiki/UTF-8#Modified_UTF-8">Modified UTF-8</a>
    public String readUTF() throws IOException {
        int len = readUnsignedShort();

        if (len == 0) {
            return "";
        }

        fillBuffer(len);

        byte[] array = buffer.array();
        int offset = buffer.position();
        int limit = offset + len;

        buffer.position(limit);

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
