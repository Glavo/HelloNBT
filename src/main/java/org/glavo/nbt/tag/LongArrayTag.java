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
package org.glavo.nbt.tag;

import org.glavo.nbt.internal.input.DataReader;

import java.io.IOException;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.stream.LongStream;

/// An ordered list of 64-bit integers.
public final class LongArrayTag extends ArrayTag<Long> {
    private static final long[] EMPTY = new long[0];

    long[] value;

    public LongArrayTag() {
        this("");
    }

    public LongArrayTag(String name) {
        super(name);
        this.value = EMPTY;
    }

    public LongArrayTag(String name, long[] value) {
        super(name);
        this.value = value.clone();
    }

    @Override
    public TagType getType() {
        return TagType.LONG_ARRAY;
    }

    @Override
    public long[] get() {
        return value.clone();
    }

    /// Sets the value of the tag.
    public void set(long[] value) {
        this.value = value.clone();
    }

    /// Returns the element at the given index.
    ///
    /// @throws IndexOutOfBoundsException if the index is out of bounds.
    public long get(int index) throws IndexOutOfBoundsException {
        return value[index];
    }

    @Override
    public Long getValue(int index) throws IndexOutOfBoundsException {
        return get(index);
    }

    /// Sets the element at the given index.
    ///
    /// @throws IndexOutOfBoundsException if the index is out of bounds.
    public void set(int index, long value) throws IndexOutOfBoundsException {
        this.value[index] = value;
    }

    @Override
    public int size() {
        return value.length;
    }

    @Override
    public PrimitiveIterator.OfLong iterator() {
        final long[] array = this.value;
        return new PrimitiveIterator.OfLong() {
            private int cursor;

            @Override
            public boolean hasNext() {
                return cursor < array.length;
            }

            @Override
            public long nextLong() {
                if (cursor >= array.length) {
                    throw new NoSuchElementException();
                }
                return array[cursor++];
            }
        };
    }

    /// Returns a sequential [LongStream] with this value as its source.
    public LongStream stream() {
        return Arrays.stream(value);
    }

    @Override
    protected void readContent(DataReader reader) throws IOException {
        value = reader.readLongArray();
    }

    @Override
    protected int contentHashCode() {
        return Arrays.hashCode(value);
    }

    @Override
    protected boolean contentEquals(Tag other) {
        return other instanceof LongArrayTag that && Arrays.equals(value, that.value);
    }

    @Override
    protected void contentToString(StringBuilder builder) {
        if (value.length > 0) {
            builder.append('[').append(value[0]);
            for (int i = 1; i < value.length; i++) {
                builder.append(", ").append(value[i]);
            }
            builder.append(']');
        } else {
            builder.append("[]");
        }
    }

    @Override
    public LongArrayTag clone() {
        return new LongArrayTag(name, value);
    }
}
