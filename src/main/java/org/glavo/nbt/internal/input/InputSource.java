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
package org.glavo.nbt.internal.input;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public sealed abstract class InputSource implements Closeable {
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

    protected final void ensureOpen() throws IOException {
        if (closed) {
            throw new IOException("Stream closed");
        }
    }

    public final void fillBuffer(InputBuffer buffer, int required) throws IOException {
        ensureOpen();

        if (buffer.remaining() > required) {
            return;
        }

        buffer.ensureCapacity(required);
        fillBufferImpl(buffer.getByteBuffer(), required);
    }

    protected abstract void fillBufferImpl(ByteBuffer target, int required) throws IOException;

    public abstract void skip(long bytes) throws IOException;

    public static final class OfInputStream extends InputSource {
        private final InputStream inputStream;
        private final boolean closeInputStream;
        private long position;

        public OfInputStream(InputStream inputStream, boolean closeInputStream) {
            this.inputStream = inputStream;
            this.closeInputStream = closeInputStream;
        }

        @Override
        public long position() {
            return position;
        }

        @Override
        protected void closeImpl() throws IOException {
            if (closeInputStream) {
                inputStream.close();
            }
        }

        @Override
        protected void fillBufferImpl(ByteBuffer target, int required) throws IOException {
            assert target.hasArray();

            while (target.remaining() < required) {
                int offset = target.limit() + target.arrayOffset();
                int len = target.capacity() - target.limit();

                int read = inputStream.read(target.array(), offset, len);
                if (read < 0) {
                    throw new EOFException("Unexpected end of stream");
                }
                target.limit(target.limit() + read);
                position += read;
            }
        }

        @Override
        public void skip(long bytes) throws IOException {
            inputStream.skipNBytes(bytes);
            position += bytes;
        }
    }
}
