/*
 * Hello Minecraft! Launcher
 * Copyright (C) 2026 huangyuhui <huanghongxun2008@126.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.glavo.nbt.internal.input;

import net.jpountz.lz4.LZ4BlockInputStream;
import org.jetbrains.annotations.Nullable;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

public final class LZ4DataReader extends BoundedDataReader {
    private final LZ4BlockInputStream lz4Stream;

    public LZ4DataReader(RawDataReader rawReader, long limit) {
        super(rawReader, rawReader.getDecompressBuffer(), limit);
        this.lz4Stream = LZ4BlockInputStream.newBuilder().build(new InputStream() {
            private byte @Nullable [] singleByte;

            @Override
            public int read() throws IOException {
                if (singleByte == null) {
                    singleByte = new byte[1];
                }

                if (read(singleByte) < 1) {
                    return -1;
                } else {
                    return Byte.toUnsignedInt(singleByte[0]);
                }
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                Objects.checkFromIndexSize(off, len, b.length);
                if (len == 0) {
                    return 0;
                }

                long rawRemaining = endPosition - rawReader.position();
                if (rawRemaining <= 0) {
                    return -1;
                }

                if (rawReader.getBuffer().remaining() == 0) {
                    try {
                        rawReader.ensureBufferRemaining(1);
                    } catch (EOFException e) {
                        return -1;
                    }
                }

                int n = (int) Math.min(Math.min(len, rawReader.getBuffer().remaining()), rawRemaining);
                rawReader.getBuffer().getBytes(b, off, n);
                return n;
            }

        });

        assert getBuffer().getByteBuffer().hasArray();
    }

    @Override
    public void ensureBufferRemaining(int required) throws IOException {
        if (getBuffer().remaining() >= required) {
            return;
        }

        long remainingInput = endPosition - getRawReader().position();
        if (remainingInput <= 0) {
            throw new EOFException("Not enough data to read, required: " + required + ", remaining: " + remainingInput);
        }

        getBuffer().ensureCapacity(required);
        ByteBuffer output = getBuffer().getByteBuffer();
        output.compact();

        byte[] array = output.array();
        try {
            while (output.position() < required) {
                int n = lz4Stream.read(array, output.arrayOffset() + output.position(), output.remaining());
                if (n <= 0) {
                    throw new EOFException("Not enough data to read, required: " + required + ", remaining: " + (endPosition - getRawReader().position()));
                }

                output.position(output.position() + n);
            }
        } finally {
            output.flip();
        }
    }

    @Override
    public void close() throws IOException {
        getRawReader().releaseDecompressBuffer(getBuffer());
        super.close();
    }
}
