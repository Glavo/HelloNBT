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

import java.util.stream.BaseStream;

/// Base class for array tags.
public sealed abstract class ArrayTag<E extends Number> extends ValueTag<Object> implements Iterable<E>
        permits ByteArrayTag, IntArrayTag, LongArrayTag {

    protected ArrayTag(String name) {
        super(name);
    }

    /// Returns `true` if the array is empty; otherwise, returns `false`.
    @Contract(pure = true)
    public final boolean isEmpty() {
        return size() == 0;
    }

    /// Returns the size of the array.
    @Contract(pure = true)
    public abstract int size();

    @Contract(pure = true)
    public abstract BaseStream<E, ?> stream();

    /// Returns the clone of the array.
    @Contract(pure = true)
    public abstract Object get();

    /// Returns the element at the given index.
    ///
    /// @throws IndexOutOfBoundsException if the index is out of bounds.
    @Contract(pure = true)
    public abstract E getValue(int index) throws IndexOutOfBoundsException;

    @Override
    @Contract(value = "-> new", pure = true)
    public abstract ArrayTag<E> clone();
}
