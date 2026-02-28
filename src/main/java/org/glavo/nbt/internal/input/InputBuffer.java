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

import org.glavo.nbt.internal.IOUtils;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class InputBuffer {
    public static InputBuffer allocateHeap(ByteOrder byteOrder) {
        return new InputBuffer(ByteBuffer.allocate(IOUtils.DEFAULT_BUFFER_SIZE).order(byteOrder));
    }

    public static InputBuffer allocateDirect(ByteOrder byteOrder) {
        return new InputBuffer(ByteBuffer.allocateDirect(IOUtils.DEFAULT_BUFFER_SIZE).order(byteOrder));
    }

    ByteBuffer bytesBuffer;
    private @Nullable StringBuilder charsBuffer;

    public InputBuffer(ByteBuffer bytesBuffer) {
        this.bytesBuffer = bytesBuffer;
    }

    public ByteBuffer bytesBuffer() {
        return bytesBuffer;
    }

    public ByteOrder order() {
        return bytesBuffer.order();
    }
}
