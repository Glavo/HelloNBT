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

public final class UncompressedDataReader extends DataReader {

    /// Used for the default reader;
    UncompressedDataReader(InputContext context, InputBuffer buffer) {
        super(context, buffer);
    }

    public UncompressedDataReader(InputContext context, long limit) {
        super(context, context.rawReader.buffer);
        this.remainingInput = limit;
    }

    @Override
    public void ensureBufferRemaining(int required) throws IOException {
        if (remainingInput >= 0 && remainingInput < required) {
            throw new EOFException("Not enough data to read, required: " + required + ", remaining: " + remainingInput);
        }

        context.source.fillBuffer(buffer, required);
    }
}
