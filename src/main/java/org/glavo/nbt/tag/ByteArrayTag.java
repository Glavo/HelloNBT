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
import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/// An ordered list of 8-bit integers.
public final class ByteArrayTag extends ArrayTag<Byte> {
    private static final byte[] EMPTY = new byte[0];

    byte[] value;

    public ByteArrayTag() {
        this("");
    }

    public ByteArrayTag(String name) {
        super(name);
        this.value = EMPTY;
    }

    public ByteArrayTag(String name, byte[] value) {
        super(name);
        this.value = value.clone();
    }

    @Override
    public TagType getType() {
        return TagType.BYTE_ARRAY;
    }

    @Override
    @Contract(pure = true)
    public byte[] get() {
        return value.clone();
    }

    @Override
    @Contract(pure = true)
    public byte[] getValue() {
        return value;
    }

    @Override
    public String getAsString() {
        return Arrays.toString(value);
    }

    /// Sets the value of the tag.
    @Contract(mutates = "this")
    public void set(byte[] value) {
        this.value = value.clone();
    }

    @Override
    @Contract(mutates = "this")
    public void setValue(Object value) {
        set((byte[]) value);
    }

    /// Returns the element at the given index.
    ///
    /// @throws IndexOutOfBoundsException if the index is out of bounds.
    @Contract(pure = true)
    public byte get(int index) throws IndexOutOfBoundsException {
        return value[index];
    }

    @Override
    @Contract(pure = true)
    public Byte getValue(int index) throws IndexOutOfBoundsException {
        return get(index);
    }

    @Override
    @Contract(value = "-> new", pure = true)
    public ByteBuffer getAsBuffer() {
        return ByteBuffer.wrap(value).asReadOnlyBuffer();
    }

    /// Sets the element at the given index.
    ///
    /// @throws IndexOutOfBoundsException if the index is out of bounds.
    @Contract(mutates = "this")
    public void set(int index, byte value) throws IndexOutOfBoundsException {
        this.value[index] = value;
    }

    @Override
    public int size() {
        return value.length;
    }

    @Override
    public Iterator<Byte> iterator() {
        final byte[] array = this.value;
        return new Iterator<>() {
            private int cursor;

            @Override
            public boolean hasNext() {
                return cursor < array.length;
            }

            @Override
            public Byte next() {
                if (cursor >= array.length) {
                    throw new NoSuchElementException();
                }
                return array[cursor++];
            }
        };
    }

    @Override
    public Stream<Byte> stream() {
        return isEmpty()
                ? Stream.empty()
                : StreamSupport.stream(Spliterators.spliterator(iterator(), size(), 0), false);
    }

    @Override
    protected void readContent(DataReader reader) throws IOException {
        value = reader.readByteArray();
    }

    @Override
    protected void writeContent(DataWriter writer) throws IOException {
        writer.writeByteArray(value);
    }

    @Override
    protected int contentHashCode() {
        return Arrays.hashCode(value);
    }

    @Override
    protected boolean contentEquals(Tag other) {
        return other instanceof ByteArrayTag that && Arrays.equals(value, that.value);
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
    public ByteArrayTag clone() {
        return new ByteArrayTag(name, value);
    }
}
