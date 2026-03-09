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
package org.glavo.nbt.internal.snbt;

import org.glavo.nbt.internal.Access;
import org.glavo.nbt.tag.ArrayTag;
import org.glavo.nbt.tag.ByteArrayTag;
import org.glavo.nbt.tag.IntArrayTag;
import org.glavo.nbt.tag.LongArrayTag;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

sealed abstract class PrimaryArrayBuilder<T extends ArrayTag<?>, A> {
    protected A array;
    protected int capacity;
    protected int length;

    protected void prepareForAdd() {
        if (capacity == length) {
            int newCapacity = capacity == 0 ? 16 : capacity << 1;
            array = copyOf(array, newCapacity);
            capacity = newCapacity;
        }
    }

    protected abstract IntegralType getIntegralType();

    protected abstract T newTag(@Nullable A array);

    protected abstract void setValue(A array, int index, long value);

    protected abstract A copyOf(@Nullable A array, int newLength);

    public void add(Token.IntegralToken token) {
        prepareForAdd();
        if (token.type().compareTo(getIntegralType()) <= 0) {
            setValue(array, length++, token.value());
        } else {
            throw new IllegalArgumentException("Invalid " + getIntegralType().name() + " array element: " + token.value());
        }
    }

    public T build() {
        if (length != 0) {
            return newTag(capacity == length ? array : copyOf(array, length));
        } else {
            return newTag(null);
        }
    }

    static final class OfByte extends PrimaryArrayBuilder<ByteArrayTag, byte[]> {

        @Override
        protected IntegralType getIntegralType() {
            return IntegralType.BYTE;
        }

        @Override
        protected byte[] copyOf(byte @Nullable [] array, int newLength) {
            return array != null ? Arrays.copyOf(array, newLength) : new byte[newLength];
        }

        @Override
        protected ByteArrayTag newTag(byte @Nullable [] array) {
            var tag = new ByteArrayTag();
            if (array != null) {
                Access.TAG.setInternalArray(tag, array);
            }
            return tag;
        }

        @Override
        protected void setValue(byte[] array, int index, long value) {
            array[index] = (byte) value;
        }
    }

    static final class OfInt extends PrimaryArrayBuilder<IntArrayTag, int[]> {

        @Override
        protected IntegralType getIntegralType() {
            return IntegralType.INT;

        }

        @Override
        protected IntArrayTag newTag(int @Nullable [] array) {
            var tag = new IntArrayTag();
            if (array != null) {
                Access.TAG.setInternalArray(tag, array);
            }
            return tag;
        }

        @Override
        protected void setValue(int[] array, int index, long value) {
            array[index] = (int) value;
        }

        @Override
        protected int[] copyOf(int @Nullable [] array, int newLength) {
            return array != null ? Arrays.copyOf(array, newLength) : new int[newLength];
        }
    }

    static final class OfLong extends PrimaryArrayBuilder<ArrayTag<Long>, long[]> {
        @Override
        protected IntegralType getIntegralType() {
            return IntegralType.LONG;
        }

        @Override
        protected ArrayTag<Long> newTag(long @Nullable [] array) {
            var tag = new LongArrayTag();
            if (array != null) {
                Access.TAG.setInternalArray(tag, array);
            }
            return tag;
        }

        @Override
        protected void setValue(long[] array, int index, long value) {
            array[index] = value;
        }

        @Override
        protected long[] copyOf(long @Nullable [] array, int newLength) {
            return array != null ? Arrays.copyOf(array, newLength) : new long[newLength];
        }
    }
}
