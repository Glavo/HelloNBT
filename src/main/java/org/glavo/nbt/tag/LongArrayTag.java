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

import org.glavo.nbt.internal.ArrayUtils;
import org.glavo.nbt.internal.input.DataReader;
import org.glavo.nbt.internal.output.DataWriter;
import org.jetbrains.annotations.Contract;

import java.io.IOException;
import java.nio.LongBuffer;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.stream.LongStream;

/// An ordered list of 64-bit integers.
public final class LongArrayTag extends ArrayTag<Long, LongTag, long[]> {

    /// Creates a new LongArrayTag with an empty name and an empty array.
    public LongArrayTag() {
        this("");
    }

    /// Creates a new LongArrayTag with the given name and an empty array.
    public LongArrayTag(String name) {
        super(name, ArrayUtils.EMPTY_LONG_ARRAY);
    }

    /// Creates a new LongArrayTag with the given name and value.
    ///
    /// The value is cloned to avoid external modifications.
    public LongArrayTag(String name, long[] values) {
        super(name, values.clone());
    }

    @Override
    @Contract(pure = true)
    public TagType<LongArrayTag> getType() {
        return TagType.LONG_ARRAY;
    }

    @Override
    @Contract(value = "-> new", pure = true)
    public LongBuffer getBuffer() {
        return LongBuffer.wrap(values).asReadOnlyBuffer();
    }

    /// Returns the element at the given index.
    ///
    /// @throws IndexOutOfBoundsException if the index is out of bounds.
    @Contract(pure = true)
    public long getLong(int index) throws IndexOutOfBoundsException {
        return values[index];
    }

    @Override
    @Contract(pure = true)
    public Long getValue(int index) throws IndexOutOfBoundsException {
        return getLong(index);
    }

    @Override
    public PrimitiveIterator.OfLong valueIterator() {
        final long[] array = this.values;
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
    @Override
    @Contract(pure = true)
    public LongStream valueStream() {
        return Arrays.stream(values);
    }

    @Override
    protected void readContent(DataReader reader) throws IOException {
        values = reader.readLongArray();
    }

    @Override
    protected void writeContent(DataWriter writer) throws IOException {
        writer.writeLongArray(values);
    }

    @Override
    @Contract(pure = true)
    public int contentHashCode() {
        return Arrays.hashCode(values);
    }

    @Override
    @Contract(pure = true)
    public boolean contentEquals(Tag other) {
        return other instanceof LongArrayTag that && Arrays.equals(values, that.values);
    }

    @Override
    protected void contentToString(StringBuilder builder) {
        if (values.length > 0) {
            builder.append('[').append(values[0]);
            for (int i = 1; i < values.length; i++) {
                builder.append(", ").append(values[i]);
            }
            builder.append(']');
        } else {
            builder.append("[]");
        }
    }

    @Override
    @Contract(value = "-> new", pure = true)
    public LongArrayTag clone() {
        return new LongArrayTag(name, values);
    }

    @Override
    protected long[] clone(long[] array) {
        return array.clone();
    }
}
