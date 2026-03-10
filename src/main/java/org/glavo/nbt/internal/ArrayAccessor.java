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

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.Arrays;

public interface ArrayAccessor<A, B extends Buffer> {
    ArrayAccessor<byte[], ByteBuffer> BYTE_ARRAY = new ArrayAccessor<>() {
        private final byte[] empty = new byte[0];

        @Override
        public byte[] empty() {
            return empty;
        }

        @Override
        public byte[] copyOf(byte[] array, int newLength) {
            return Arrays.copyOf(array, newLength);
        }
    };

    ArrayAccessor<int[], IntBuffer> INT_ARRAY = new ArrayAccessor<>() {
        private final int[] empty = new int[0];

        @Override
        public int[] empty() {
            return empty;
        }

        @Override
        public int[] copyOf(int[] array, int newLength) {
            return Arrays.copyOf(array, newLength);
        }
    };

    ArrayAccessor<long[], LongBuffer> LONG_ARRAY = new ArrayAccessor<>() {
        private final long[] empty = new long[0];

        @Override
        public long[] empty() {
            return empty;
        }

        @Override
        public long[] copyOf(long[] array, int newLength) {
            return Arrays.copyOf(array, newLength);
        }
    };


    A empty();

    A copyOf(A array, int newLength);
}
