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
package org.glavo.nbt.internal;

import org.glavo.nbt.tag.ByteTag;
import org.glavo.nbt.tag.IntTag;
import org.glavo.nbt.tag.LongTag;
import org.glavo.nbt.tag.ValueTag;

import java.lang.reflect.Array;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.Arrays;

public abstract class ArrayAccessor<E extends Number, T extends ValueTag<E>, A, B extends Buffer> {
    protected final A empty;

    private ArrayAccessor(A empty) {
        this.empty = empty;
    }

    /// Returns an empty array.
    public final A empty() {
        return empty;
    }

    public abstract A copyOf(A array, int newLength);

    public final int getLength(A array) {
        return Array.getLength(array);
    }

    public abstract E get(A array, int index);

    public abstract void set(A array, int index, E value);

    public abstract void set(A array, int index, T tag);

    public abstract A get(B buffer);

    public static final ArrayAccessor<Byte, ByteTag, byte[], ByteBuffer> BYTE_ARRAY = new ArrayAccessor<>(new byte[0]) {
        @Override
        public byte[] copyOf(byte[] array, int newLength) {
            return Arrays.copyOf(array, newLength);
        }

        @Override
        public Byte get(byte[] array, int index) {
            return array[index];
        }

        @Override
        public void set(byte[] array, int index, Byte value) {
            array[index] = value;
        }

        @Override
        public void set(byte[] array, int index, ByteTag tag) {
            array[index] = tag.get();
        }

        @Override
        public byte[] get(ByteBuffer buffer) {
            int remaining = buffer.remaining();
            if (remaining > 0) {
                byte[] array = new byte[remaining];
                buffer.get(array);
                return array;
            } else {
                return empty;
            }
        }
    };

    public static final ArrayAccessor<Integer, IntTag, int[], IntBuffer> INT_ARRAY = new ArrayAccessor<>(new int[0]) {

        @Override
        public int[] copyOf(int[] array, int newLength) {
            return Arrays.copyOf(array, newLength);
        }

        @Override
        public Integer get(int[] array, int index) {
            return array[index];
        }

        @Override
        public void set(int[] array, int index, Integer value) {
            array[index] = value;
        }

        @Override
        public void set(int[] array, int index, IntTag tag) {
            array[index] = tag.getValue();
        }

        @Override
        public int[] get(IntBuffer buffer) {
            int remaining = buffer.remaining();
            if (remaining > 0) {
                int[] array = new int[remaining];
                buffer.get(array);
                return array;
            } else {
                return empty;
            }
        }
    };

    public static final ArrayAccessor<Long, LongTag, long[], LongBuffer> LONG_ARRAY = new ArrayAccessor<>(new long[0]) {

        @Override
        public long[] copyOf(long[] array, int newLength) {
            return Arrays.copyOf(array, newLength);
        }

        @Override
        public Long get(long[] array, int index) {
            return array[index];
        }

        @Override
        public void set(long[] array, int index, Long value) {
            array[index] = value;
        }

        @Override
        public void set(long[] array, int index, LongTag tag) {
            array[index] = tag.getValue();
        }

        @Override
        public long[] get(LongBuffer buffer) {
            int remaining = buffer.remaining();
            if (remaining > 0) {
                long[] array = new long[remaining];
                buffer.get(array);
                return array;
            } else {
                return empty;
            }
        }
    };

}
