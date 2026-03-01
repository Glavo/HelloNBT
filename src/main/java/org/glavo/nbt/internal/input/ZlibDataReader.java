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

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.Inflater;

public final class ZlibDataReader extends DataReader {
    static final InputContext.CacheKey<Inflater> INFLATER_CACHE_KEY = new InputContext.CacheKey<>();
    private final Inflater inflater;

    ZlibDataReader(InputContext context, long compressedSize) {
        super(context, context.getDecompressBuffer());
        this.inflater = INFLATER_CACHE_KEY.getOrCreate(context, Inflater::new);
        this.remainingInput = compressedSize;
    }

    @Override
    public void ensureBufferRemaining(int required) throws IOException {
        if (this.buffer.remaining() >= required) {
            return;
        }

        buffer.ensureCapacity(required);
        ByteBuffer byteBuffer = this.buffer.getByteBuffer();

        // TODO

//        do {
//            if (inflater.finished() || inflater.needsDictionary()) {
//                throw new EOFException();
//            }
//
//            if (inflater.needsInput()) {
//                if (context.rawReader.buffer.remaining() == 0) {
//                    context.rawReader.ensureBufferRemaining(1);
//                }
//
//                inflater.setInput(context.rawReader.buffer.getByteBuffer());
//            }
//        } while ((n = inflater.inflate(b, off, len)) == 0);
    }
}
