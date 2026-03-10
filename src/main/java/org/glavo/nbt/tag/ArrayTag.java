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
import org.glavo.nbt.internal.ArrayUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.nio.Buffer;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.BaseStream;

/// Base class for array tags.
///
/// Each array tag holds an array of numbers.
///
/// @see ByteArrayTag
/// @see IntArrayTag
/// @see LongArrayTag
public sealed abstract class ArrayTag<E extends Number, T extends ValueTag<E>, A, B extends Buffer>
        extends ParentTag<T>
        permits ByteArrayTag, IntArrayTag, LongArrayTag {

    A values = accessor().empty();

    protected ArrayTag(String name) {
        super(name);
    }

    @SuppressWarnings("ClassEscapesDefinedScope")
    @Contract(pure = true)
    protected abstract ArrayAccessor<E, T, A, B> accessor();

    /// Creates a tag from the value at the given index.
    @Contract("_ -> new")
    protected abstract T createTagFromIndex(int index);

    private void removeValueFromArray(int index) {
        assert index >= 0 && index < size;

        if (index < size - 1) {
            //noinspection SuspiciousSystemArraycopy
            System.arraycopy(values, index + 1, values, index, size - index - 1);
        }
    }

    protected void ensureValuesCapacityForAdd() {
        if (accessor().getLength(values) == size) {
            values = accessor().copyOf(values, ArrayUtils.nextCapacity(size, size + 1));
        }
    }

    @Override
    protected final void preUpdateSubTagName(Tag tag, String oldName, String newName) throws IllegalArgumentException {
        if (!newName.isEmpty()) {
            throw new IllegalArgumentException("The name of the subtag must be null for ArrayTag");
        }
    }

    @Override
    @Contract(pure = true)
    public abstract TagType<? extends ArrayTag<E, T, A, B>> getType();

    /// Returns an iterator over the elements of this array.
    public abstract Iterator<E> valueIterator();

    /// Returns a sequential stream with this array as its source.
    @Contract(pure = true)
    public abstract BaseStream<E, ?> valueStream();

    /// Returns the clone of the array.
    @Contract(pure = true)
    public final A getArray() {
        return accessor().copyOf(values, size);
    }

    final @Nullable T getTagOrNull(int index) {
        if (index >= tags.length) {
            return null;
        }
        @SuppressWarnings("unchecked")
        T tag = (T) tags[index];
        return tag;
    }

    @Override
    public T getTag(int index) throws IndexOutOfBoundsException {
        Objects.checkIndex(index, size);

        T tag = getTagOrNull(index);
        if (tag != null) {
            return tag;
        }

        tag = createTagFromIndex(index);
        ensureTagsCapacity(index + 1);
        assert tags[index] == null;

        tag.setParent(this, index);
        tags[index] = tag;
        return tag;
    }

    /// Returns the element at the given index.
    ///
    /// @throws IndexOutOfBoundsException if the index is out of bounds.
    @Contract(pure = true)
    public abstract E getValue(int index) throws IndexOutOfBoundsException;

    /// Returns the array as a readonly [Buffer].
    ///
    /// The buffer is readonly, the position is set to `0`, and the limit is set to the size of the array.
    ///
    /// Each call returns a new buffer, but the underlying implementation may share the same array.
    @Contract(value = "-> new", pure = true)
    public abstract B getBuffer();

    /// Sets the value of the tag without cloning the array.
    @Contract(mutates = "this")
    final void setArrayWithoutClone(A array, int size) {
        assert accessor().getLength(array) <= size;

        clear();

        this.values = array;
        this.size = size;
    }

    /// Sets the value at the given index.
    ///
    /// @throws IndexOutOfBoundsException if the index is out of bounds.
    @Contract(mutates = "this")
    public abstract void set(int index, E value) throws IndexOutOfBoundsException;

    /// Sets all values of the tag from an array.
    ///
    /// The array is cloned to avoid external modifications.
    ///
    /// Calling this method will clear the current array, all subtags will be removed.
    @Contract(mutates = "this")
    @MustBeInvokedByOverriders
    public void setAll(A array) {
        clear();

        int newSize = accessor().getLength(array);
        if (newSize > 0) {
            this.values = accessor().copyOf(array, newSize);
            this.size = newSize;
        }
    }

    /// Set all values of the tag from a buffer.
    ///
    /// This method uses the data in the buffer from `position` to `limit` to set the values of the array.
    /// After calling this method, the `position` of the buffer will be set to `limit`.
    ///
    /// Calling this method will clear the current array, all subtags will be removed.
    @Contract(mutates = "this,param1")
    public final void setAll(B buffer) {
        clear();

        if (buffer.hasRemaining()) {
            A array = accessor().get(buffer);
            setArrayWithoutClone(array, accessor().getLength(array));
        }
    }

    /// Appends the specified value to the end of this array.
    @Contract(mutates = "this")
    public abstract void add(E value);

    @Override
    @Contract(mutates = "this,param1")
    public final void addTag(T tag) throws IllegalArgumentException {
        if (tag.getParentTag() != null) {
            if (tag.getParentTag() == this) {
                int index = tag.getIndex();

                if (tag.getIndex() == this.size() - 1) {
                    // The tag is already the last child of this tag, so we don't need to do anything.
                } else {
                    // Move the tag to the end of the subTags list.

                    Tag oldTag = removeTagFromArray(index);
                    if (oldTag != tag) {
                        throw new AssertionError("Expected " + tag + ", but got " + oldTag);
                    }

                    removeValueFromArray(index);

                    tags[size - 1] = tag;
                    Array.set(values, size - 1, tag.getValue());

                    updateIndexes(index);
                }

                return;
            } else {
                // Remove the tag from its old parent.
                tag.getParentTag().removeElement(tag);
            }
        }

        ensureTagsCapacityForAdd();

        add(tag.getValue());
        tags[size - 1] = tag;
    }

    @Override
    @Contract(mutates = "this")
    public final void removeAt(int index) throws IndexOutOfBoundsException {
        Objects.checkIndex(index, size);


        T tag = removeTagFromArray(index);
        if (tag != null) {
            assert tag.getIndex() == index && tag.getParentTag() == this;

            tag.setParent(null, -1);
        }

        removeValueFromArray(index);

        size--;
    }

    @Override
    @Contract(mutates = "this")
    public final T removeTagAt(int index) throws IndexOutOfBoundsException {
        Objects.checkIndex(index, size);

        T tag = removeTagFromArray(index);

        if (tag != null) {
            assert tag.getIndex() == index && tag.getParentTag() == this;

            tag.setParent(null, -1);
        } else {
            tag = createTagFromIndex(index);
        }

        removeValueFromArray(index);

        size--;
        return tag;
    }

    @Override
    public void clear() {
        super.clear();
        values = accessor().empty();
    }

    @Override
    @Contract(value = "-> new", pure = true)
    public abstract ArrayTag<E, T, A, B> clone();

}
