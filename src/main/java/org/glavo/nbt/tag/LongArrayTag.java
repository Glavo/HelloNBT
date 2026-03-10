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

import org.glavo.nbt.internal.ArrayAccessor;
import org.glavo.nbt.internal.input.DataReader;
import org.glavo.nbt.internal.output.DataWriter;
import org.jetbrains.annotations.Contract;

import java.io.IOException;
import java.nio.LongBuffer;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.stream.LongStream;

/// An ordered list of 64-bit integers.
public final class LongArrayTag extends ArrayTag<Long, LongTag, long[], LongBuffer> {

    /// Creates a new LongArrayTag with an empty name and an empty array.
    public LongArrayTag() {
        this("");
    }

    /// Creates a new LongArrayTag with the given name and an empty array.
    public LongArrayTag(String name) {
        super(name);
    }

    /// Creates a new LongArrayTag with the given name and value.
    ///
    /// The value is cloned to avoid external modifications.
    public LongArrayTag(String name, long[] values) {
        super(name);
        setAll(values);
    }

    @Override
    protected ArrayAccessor<Long, LongTag, long[], LongBuffer> accessor() {
        return ArrayAccessor.LONG_ARRAY;
    }

    @Override
    protected LongTag createTagFromIndex(int index) {
        assert index >= 0 && index < size;
        return new LongTag("", values[index]);
    }

    @Override
    @Contract(pure = true)
    public TagType<LongArrayTag> getType() {
        return TagType.LONG_ARRAY;
    }

    @Override
    @Contract(value = "-> new", pure = true)
    public LongBuffer getBuffer() {
        return LongBuffer.wrap(values).slice(0, size).asReadOnlyBuffer();
    }

    /// Returns the element at the given index.
    ///
    /// @throws IndexOutOfBoundsException if the index is out of bounds.
    @Contract(pure = true)
    public long getLong(int index) throws IndexOutOfBoundsException {
        Objects.checkIndex(index, size);
        return values[index];
    }

    @Override
    @Contract(pure = true)
    public Long getValue(int index) throws IndexOutOfBoundsException {
        return getLong(index);
    }

    void setDirect(int index, long value) {
        values[index] = value;
    }

    /// Sets the value at the given index.
    ///
    /// @throws IndexOutOfBoundsException if the index is out of bounds.
    @Contract(mutates = "this")
    public void set(int index, long value) throws IndexOutOfBoundsException {
        Objects.checkIndex(index, size);
        setDirect(index, value);

        LongTag tag = getTagOrNull(index);
        if (tag != null) {
            tag.setDirect(value);
        }
    }

    @Override
    @Contract(mutates = "this")
    public void set(int index, Long value) throws IndexOutOfBoundsException {
        set(index, value.longValue());
    }

    @Override
    @Contract(mutates = "this")
    public void setAll(long... array) { // Override to use varargs
        super.setAll(array);
    }

    /// Appends the specified value to the end of this array.
    @Contract(mutates = "this")
    public void add(long value) {
        ensureValuesCapacityForAdd();
        values[size++] = value;
    }

    @Override
    @Contract(mutates = "this")
    public void add(Long value) {
        add(value.longValue());
    }

    @Override
    public PrimitiveIterator.OfLong valueIterator() {
        final long[] array = this.values;
        final int size = this.size;
        return new PrimitiveIterator.OfLong() {
            private int cursor;

            @Override
            public boolean hasNext() {
                return cursor < size;
            }

            @Override
            public long nextLong() {
                if (cursor >= size) {
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
        return Arrays.stream(values, 0, size);
    }

    @Override
    protected void readContent(DataReader reader) throws IOException {
        clear();
        int len = reader.readInt();
        setArrayWithoutClone(reader.readLongArray(len), len);
    }

    @Override
    protected void writeContent(DataWriter writer) throws IOException {
        writer.writeInt(size);
        writer.writeLongArrayDirect(values, 0, size);
    }

    @Override
    @Contract(pure = true)
    public int contentHashCode() {
        int hashCode = 0;
        for (int i = 0; i < size; i++) {
            hashCode = 31 * hashCode + Long.hashCode(values[i]);
        }
        return hashCode;
    }

    @Override
    @Contract(pure = true)
    public boolean contentEquals(Tag other) {
        return other instanceof LongArrayTag that && Arrays.equals(
                this.values, 0, this.size,
                that.values, 0, that.size
        );
    }

    @Override
    protected void contentToString(StringBuilder builder) {
        if (size > 0) {
            builder.append('[').append(values[0]);
            for (int i = 1; i < size; i++) {
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
        LongArrayTag tag = new LongArrayTag(name);
        if (size > 0) {
            tag.values = Arrays.copyOf(values, size);
        }
        return tag;
    }

}
