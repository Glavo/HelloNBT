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

import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class InputBuffer {
    public static InputBuffer allocate(int size, boolean direct, ByteOrder byteOrder) {
        ByteBuffer bytesBuffer = direct
                ? ByteBuffer.allocateDirect(size)
                : ByteBuffer.allocate(size);

        bytesBuffer.order(byteOrder);
        bytesBuffer.limit(0);
        return new InputBuffer(bytesBuffer);
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

    public byte getByte() {
        return bytesBuffer.get();
    }

    public short getShort() {
        return bytesBuffer.getShort();
    }

    public int getInt() {
        return bytesBuffer.getInt();
    }

    public long getLong() {
        return bytesBuffer.getLong();
    }

    public float getFloat() {
        return bytesBuffer.getFloat();
    }

    public double getDouble() {
        return bytesBuffer.getDouble();
    }

    public byte[] getByteArray(int len) {
        var array = new byte[len];
        bytesBuffer.get(array);
        return array;
    }

    public int[] getIntArray(int len) {
        var array = new int[len];
        bytesBuffer.asIntBuffer().get(array);
        bytesBuffer.position(bytesBuffer.position() + len * Integer.BYTES);
        return array;
    }

    public long[] getLongArray(int len) {
        var array = new long[len];
        bytesBuffer.asLongBuffer().get(array);
        bytesBuffer.position(bytesBuffer.position() + len * Long.BYTES);
        return array;
    }
}
