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
package org.glavo.nbt;

import org.glavo.nbt.chunk.Chunk;
import org.glavo.nbt.chunk.ChunkRegion;
import org.glavo.nbt.tag.ParentTag;
import org.jetbrains.annotations.Contract;

import java.util.stream.Stream;

/// Base interface for NBT elements that can contain other NBT elements as children.
public sealed interface NBTParent<E extends NBTElement> extends NBTElement, Iterable<E>
        permits ParentTag, ChunkRegion, Chunk {

    /// Returns `true` if this parent has no child elements, `false` otherwise.
    @Contract(pure = true)
    boolean isEmpty();

    /// Returns the number of child elements.
    @Contract(pure = true)
    int size();

    /// Returns a stream of child elements.
    @Contract(pure = true)
    Stream<E> stream();

    /// Removes the `element` from this parent.
    ///
    /// @throws IllegalArgumentException if the `element` is not a child of this parent.
    @Contract(mutates = "this,param1")
    void removeElement(E element) throws IllegalArgumentException;
}
