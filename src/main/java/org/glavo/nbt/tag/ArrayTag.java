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

import org.jetbrains.annotations.Contract;

import java.lang.reflect.Array;
import java.nio.Buffer;
import java.util.Iterator;
import java.util.stream.BaseStream;

/// Base class for array tags.
public sealed abstract class ArrayTag<E extends Number, T extends ValueTag<E>, A>
        extends ParentTag<T>
        permits ByteArrayTag, IntArrayTag, LongArrayTag {

    A values;

    protected ArrayTag(String name, A values) {
        super(name);
        this.values = values;
    }

    @Override
    @Contract(pure = true)
    public abstract TagType<? extends ArrayTag<E, T, A>> getType();

    /// Returns an iterator over the elements of this array.
    public abstract Iterator<E> valueIterator();

    /// Returns a sequential stream with this array as its source.
    @Contract(pure = true)
    public abstract BaseStream<E, ?> valueStream();

    /// Returns the clone of the array.
    @Contract(pure = true)
    public A getArray() {
        return clone(values);
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
    public abstract Buffer getBuffer();

    /// Sets the value of the tag.
    @Contract(mutates = "this")
    public void set(A array) {
        this.values = clone(array);
    }

    @Override
    @Contract(value = "-> new", pure = true)
    public abstract ArrayTag<E, T, A> clone();

    protected abstract A clone(A array);

    // ParentTag methods

    @Override
    void preUpdateSubTagName(Tag tag, String oldName, String newName) throws IllegalArgumentException {
        if (!newName.isEmpty()) {
            throw new IllegalArgumentException("The name of the subtag must be null for ArrayTag");
        }
    }
}
