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

import org.glavo.nbt.MinecraftEdition;
import org.glavo.nbt.internal.IOUtils;
import org.glavo.nbt.internal.StringCache;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Map;

public final class InputContext implements Closeable {
    // Used for reading UTF-8 strings
    private static final StringCache DEFAULT_CACHE = new StringCache(
            "data", "Data", "DataVersion"
            // TODO: More tag names
    );

    public final InputSource source;
    public final MinecraftEdition edition;

    public final RawDataReader rawReader;
    private final long startPosition;

    public final StringCache stringCache = DEFAULT_CACHE;
    @Nullable StringBuilder charsBuffer;

    private @Nullable Map<CacheKey<?>, Object> cacheMap;

    public InputContext(InputSource source, MinecraftEdition edition) {
        this.source = source;
        this.edition = edition;
        this.rawReader = new RawDataReader(
                this,
                InputBuffer.allocate(IOUtils.DEFAULT_BUFFER_SIZE, source.supportDirectBuffer(), edition.byteOrder()));
        this.startPosition = source.position();
    }

    public long position() {
        long position = source.position() - startPosition - rawReader.buffer.remaining();
        assert position >= 0;
        return position;
    }

    public void skip(long bytes) throws IOException {
        if (bytes < 0) {
            throw new IllegalArgumentException("bytes must be non-negative");
        }

        if (bytes == 0L) {
            return;
        }

        int bytesDrop = (int) Math.min(rawReader.buffer.remaining(), bytes);
        rawReader.buffer.drop(bytesDrop);

        bytes -= bytesDrop;
        if (bytes > 0) {
            source.skip(bytes);
        }
    }

    private @Nullable InputBuffer decompressBuffer;

    public InputBuffer getDecompressBuffer() {
        if (decompressBuffer == null) {
            return InputBuffer.allocate(IOUtils.DEFAULT_BUFFER_SIZE, false, edition.byteOrder());
        } else {
            decompressBuffer.drop();
            return decompressBuffer;
        }
    }

    public void releaseDecompressBuffer(InputBuffer buffer) {
        buffer.drop();
        decompressBuffer = buffer;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void close() throws IOException {
        source.close();
        if (cacheMap != null) {
            for (var entry : new ArrayList<>(cacheMap.entrySet())) {
                ((CacheKey) entry.getKey()).close(entry.getValue());
            }

            cacheMap.clear();
        }
    }

    @SuppressWarnings("unchecked")
    public static abstract class CacheKey<T> {

        public T get(InputContext context) {
            if (context.cacheMap != null) {
                T value = (T) context.cacheMap.remove(this);
                if (value != null) {
                    return value;
                }
            }

            return create(context);
        }

        public void release(InputContext context, T value) {
            if (context.cacheMap == null) {
                context.cacheMap = new IdentityHashMap<>();
            }

            T oldValue = (T) context.cacheMap.put(this, value);
            if (oldValue != null) {
                close(oldValue);
            }
        }

        protected abstract T create(InputContext context);

        public void close(T value) {
        }
    }
}
