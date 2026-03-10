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
import java.nio.IntBuffer;
import java.util.*;
import java.util.stream.IntStream;

/// An ordered list of 32-bit integers. Sometimes used for UUIDs.
public final class IntArrayTag extends ArrayTag<Integer, IntTag, int[]> {
    private static final int[] EMPTY = new int[0];

    int[] value;

    /// Creates a new IntArrayTag with an empty name and an empty array.
    public IntArrayTag() {
        this("");
    }

    /// Creates a new IntArrayTag with the given name and an empty array.
    public IntArrayTag(String name) {
        super(name);
        this.value = EMPTY;
    }

    /// Creates a new IntArrayTag with the given name and value.
    ///
    /// The value is cloned to avoid external modifications.
    public IntArrayTag(String name, int[] value) {
        super(name);
        this.value = value.clone();
    }

    /// Create a new IntArrayTag with the name and a UUID value.
    public IntArrayTag(String name, UUID uuid) {
        super(name);
        setUUID(uuid);
    }

    @Override
    public TagType<IntArrayTag> getType() {
        return TagType.INT_ARRAY;
    }

    /// Returns true if the tag is a UUID.
    ///
    /// All int array tags with length 4 are treated as UUIDs.
    ///
    /// @see <a href="https://minecraft.wiki/w/UUID">UUID - Minecraft Wiki</a>
    public boolean isUUID() {
        return value.length == 4;
    }

    @Override
    @Contract(pure = true)
    public int[] getArray() {
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

    /// Sets the value of the tag from a UUID.
    @Contract(mutates = "this")
    public void setUUID(UUID uuid) {
        int[] value = new int[4];
        value[0] = (int) (uuid.getMostSignificantBits() >>> 32);
        value[1] = (int) (uuid.getMostSignificantBits() & 0xFFFF_FFFFL);
        value[2] = (int) (uuid.getLeastSignificantBits() >>> 32);
        value[3] = (int) (uuid.getLeastSignificantBits() & 0xFFFF_FFFFL);
        this.value = value;
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

    @Contract(pure = true)
    public UUID getUUID() {
        if (value.length != 4) {
            throw new IllegalStateException("IntArrayTag is not a UUID");
        }
        long msb = ((long) value[0] << 32) | (long) value[1] & 0xFFFFFFFFL;
        long lsb = ((long) value[2] << 32) | (long) value[3] & 0xFFFFFFFFL;
        return new UUID(msb, lsb);
    }

    @Override
    @Contract(value = "-> new", pure = true)
    public IntBuffer getBuffer() {
        return IntBuffer.wrap(value).asReadOnlyBuffer();
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
    public IntStream valuesStream() {
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
    @Contract(pure = true)
    public int contentHashCode() {
        return Arrays.hashCode(value);
    }

    @Override
    @Contract(pure = true)
    public boolean contentEquals(Tag other) {
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
