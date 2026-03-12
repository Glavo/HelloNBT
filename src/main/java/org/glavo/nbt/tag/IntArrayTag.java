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
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.*;
import java.util.stream.IntStream;

/// A tag that holds an array of [int tags][IntTag] or a UUID.
///
/// Minecraft also uses int array tags to store UUIDs. UUIDs are stored as an IntArrayTag with a length of 4.
/// You can determine whether a tag is a UUID using [#isUUID()]. If the tag is a UUID, you can get the UUID value using [#getUUID()].
/// You can also set the UUID value using [#setUUID(UUID)].
///
/// @see Tag
/// @see ParentTag
/// @see ArrayTag
/// @see IntTag
public final class IntArrayTag extends ArrayTag<Integer, IntTag, int[], IntBuffer> {
    /// Creates a new IntArrayTag with an empty name and an empty array.
    public IntArrayTag() {
        this("");
    }

    /// Creates a new IntArrayTag with the given name and an empty array.
    public IntArrayTag(String name) {
        super(name);
    }

    /// Creates a new IntArrayTag with the given name and value.
    ///
    /// The value is cloned to avoid external modifications.
    public IntArrayTag(String name, int[] value) {
        super(name);
        setAll(value);
    }

    /// Create a new IntArrayTag with the name and a UUID value.
    public IntArrayTag(String name, UUID uuid) {
        super(name);
        setUUID(uuid);
    }

    @Override
    ArrayAccessor<Integer, IntTag, int[], IntBuffer> accessor() {
        return ArrayAccessor.INT_ARRAY;
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
        return size == 4;
    }

    /// Returns the element at the given index.
    ///
    /// @throws IndexOutOfBoundsException if the index is out of bounds.
    @Contract(pure = true)
    public int get(int index) throws IndexOutOfBoundsException {
        Objects.checkIndex(index, size);
        return values[index];
    }

    /// Returns the UUID value of the tag.
    ///
    /// @throws IllegalStateException if the tag is not a [UUID][#isUUID()].
    @Contract(pure = true)
    public UUID getUUID() {
        if (!isUUID()) {
            throw new IllegalStateException("IntArrayTag is not a UUID");
        }
        long msb = ((long) values[0] << 32) | (long) values[1] & 0xFFFFFFFFL;
        long lsb = ((long) values[2] << 32) | (long) values[3] & 0xFFFFFFFFL;
        return new UUID(msb, lsb);
    }

    void setDirect(int index, int value) {
        values[index] = value;
    }

    /// Sets the value at the given index.
    ///
    /// @throws IndexOutOfBoundsException if the index is out of bounds.
    @Contract(mutates = "this")
    public void set(int index, int value) throws IndexOutOfBoundsException {
        Objects.checkIndex(index, size);
        setDirect(index, value);

        IntTag tag = getTagOrNull(index);
        if (tag != null) {
            tag.setDirect(value);
        }
    }

    /// {@inheritDoc}
    ///
    /// @see #set(int, int)
    @Override
    @Contract(mutates = "this")
    @ApiStatus.Obsolete
    public void set(int index, Integer value) throws IndexOutOfBoundsException {
        set(index, value.intValue());
    }

    @Override
    @Contract(mutates = "this")
    public void setAll(int... array) { // Override to use varargs
        super.setAll(array);
    }

    /// Sets the value of the tag from a UUID.
    ///
    /// Calling this method will clear the current array, all subtags will be removed.
    @Contract(mutates = "this")
    public void setUUID(UUID uuid) {
        int[] array = new int[4];
        array[0] = (int) (uuid.getMostSignificantBits() >>> 32);
        array[1] = (int) (uuid.getMostSignificantBits() & 0xFFFF_FFFFL);
        array[2] = (int) (uuid.getLeastSignificantBits() >>> 32);
        array[3] = (int) (uuid.getLeastSignificantBits() & 0xFFFF_FFFFL);

        clear();
        setArrayWithoutClone(array, 4);
    }

    /// Appends the specified value to the end of this array.
    @Contract(mutates = "this")
    public void add(int value) {
        ensureValuesCapacityForAdd();
        values[size++] = value;
    }

    /// {@inheritDoc}
    ///
    /// @see #add(int)
    @Override
    @Contract(mutates = "this")
    @ApiStatus.Obsolete
    public void add(Integer value) {
        add(value.intValue());
    }

    @Override
    public PrimitiveIterator.OfInt valueIterator() {
        final int[] array = this.values;
        final int size = this.size;
        return new PrimitiveIterator.OfInt() {
            private int cursor;

            @Override
            public boolean hasNext() {
                return cursor < size;
            }

            @Override
            public int nextInt() {
                if (cursor >= size) {
                    throw new NoSuchElementException();
                }
                return array[cursor++];
            }
        };
    }

    /// Returns a sequential [IntStream] with this value as its source.
    @Override
    @Contract(pure = true)
    public IntStream valueStream() {
        return Arrays.stream(values, 0, size);
    }

    @Override
    void readContent(DataReader reader) throws IOException {
        clear();
        int len = reader.readInt();
        setArrayWithoutClone(reader.readIntArray(len), len);
    }

    @Override
    void writeContent(DataWriter writer) throws IOException {
        writer.writeInt(size);
        writer.writeIntArrayDirect(values, 0, size);
    }

    @Override
    @Contract(value = "-> new", pure = true)
    public IntArrayTag clone() {
        IntArrayTag tag = new IntArrayTag(name);
        if (size > 0) {
            tag.values = Arrays.copyOf(values, size);
        }
        return tag;
    }

}
