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
import org.glavo.nbt.internal.output.DataWriter;
import org.jetbrains.annotations.Contract;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.IntBuffer;
import java.util.*;
import java.util.stream.IntStream;

/// An ordered list of 32-bit integers.
public final class IntArrayTag extends ArrayTag<Integer> {
    private static final int[] EMPTY = new int[0];

    int[] value;

    public IntArrayTag() {
        this("");
    }

    public IntArrayTag(String name) {
        super(name);
        this.value = EMPTY;
    }

    public IntArrayTag(String name, int[] value) {
        super(name);
        this.value = value.clone();
    }

    @Override
    public TagType getType() {
        return TagType.INT_ARRAY;
    }

    @Override
    @Contract(pure = true)
    public int[] get() {
        return value.clone();
    }

    @Override
    @Contract(pure = true)
    public int[] getValue() {
        return value;
    }

    @Override
    public String getAsString() {
        return Arrays.toString(value);
    }

    /// Sets the value of the tag.
    @Contract(mutates = "this")
    public void set(int[] value) {
        this.value = value.clone();
    }

    @Override
    @Contract(mutates = "this")
    public void setValue(Object value) {
        set((int[]) value);
    }

    /// Returns the element at the given index.
    ///
    /// @throws IndexOutOfBoundsException if the index is out of bounds.
    @Contract(pure = true)
    public int get(int index) throws IndexOutOfBoundsException {
        return value[index];
    }

    @Override
    @Contract(pure = true)
    public Integer getValue(int index) throws IndexOutOfBoundsException {
        return get(index);
    }

    @Override
    @Contract(value = "-> new", pure = true)
    public IntBuffer getAsBuffer() {
        return IntBuffer.wrap(value).asReadOnlyBuffer();
    }

    /// Sets the element at the given index.
    ///
    /// @throws IndexOutOfBoundsException if the index is out of bounds.
    @Contract(mutates = "this")
    public void set(int index, int value) throws IndexOutOfBoundsException {
        this.value[index] = value;
    }

    @Override
    @Contract(pure = true)
    public int size() {
        return value.length;
    }

    @Override
    public PrimitiveIterator.OfInt iterator() {
        final int[] array = this.value;
        return new PrimitiveIterator.OfInt() {
            private int cursor;

            @Override
            public boolean hasNext() {
                return cursor < array.length;
            }

            @Override
            public int nextInt() {
                if (cursor >= array.length) {
                    throw new NoSuchElementException();
                }
                return array[cursor++];
            }
        };
    }

    /// Returns a sequential [IntStream] with this value as its source.
    @Override
    @Contract(pure = true)
    public IntStream stream() {
        return Arrays.stream(value);
    }

    @Override
    protected void readContent(DataReader reader) throws IOException {
        value = reader.readIntArray();
    }

    @Override
    protected void writeContent(DataWriter writer) throws IOException {
        writer.writeIntArray(value);
    }

    @Override
    protected int contentHashCode() {
        return Arrays.hashCode(value);
    }

    @Override
    protected boolean contentEquals(Tag other) {
        return other instanceof IntArrayTag that && Arrays.equals(value, that.value);
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
    public IntArrayTag clone() {
        return new IntArrayTag(name, value);
    }
}
