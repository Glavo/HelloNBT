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
package org.glavo.nbt.internal.output;

import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;

public abstract class OutputTarget implements Closeable, Flushable {
    private boolean closed = false;

    public boolean supportDirectBuffer() {
        return false;
    }

    public abstract long position();

    @Override
    public final void close() throws IOException {
        if (!closed) {
            closed = true;
            closeImpl();
        }
    }

    protected abstract void closeImpl() throws IOException;

    @Override
    public void flush() throws IOException {
    }

    protected final void ensureOpen() throws IOException {
        if (closed) {
            throw new IOException("Stream closed");
        }
    }

    public abstract void write(OutputBuffer buffer) throws IOException;

    public abstract void skip(long bytes) throws IOException;

    public static final class OfOutputStream extends OutputTarget {

        private final OutputStream outputStream;
        private final boolean closeOutputStream;
        private long position;

        public OfOutputStream(OutputStream outputStream, boolean closeOutputStream) {
            this.outputStream = outputStream;
            this.closeOutputStream = closeOutputStream;
        }

        @Override
        public long position() {
            return position;
        }

        @Override
        protected void closeImpl() throws IOException {
            if (closeOutputStream) {
                outputStream.close();
            }
        }

        @Override
        public void flush() throws IOException {
            ensureOpen();
            outputStream.flush();
        }

        @Override
        public void write(OutputBuffer buffer) throws IOException {
            ensureOpen();
            buffer.assertStatus();

            outputStream.write(
                    buffer.getByteBuffer().array(),
                    buffer.getByteBuffer().arrayOffset(),
                    buffer.getByteBuffer().position());
            position += buffer.getByteBuffer().position();
            buffer.getByteBuffer().clear();
        }

        private byte @Nullable [] skipBuffer;

        @Override
        public void skip(long bytes) throws IOException {
            if (bytes < 0) {
                throw new IllegalArgumentException("Negative skip length: " + bytes);
            }

            ensureOpen();

            if (bytes == 0) {
                return;
            }


            if (skipBuffer == null) {
                skipBuffer = new byte[512];
            }

            long count = 0;
            while (count < bytes) {
                int toWrite = (int) Math.min(skipBuffer.length, bytes - count);
                outputStream.write(skipBuffer, 0, toWrite);
                position += toWrite;
                count += toWrite;
            }
        }
    }
}
