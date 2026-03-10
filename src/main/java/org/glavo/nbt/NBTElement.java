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
import org.glavo.nbt.tag.Tag;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/// Base interface for all NBT elements.
///
/// @see Tag
/// @see ChunkRegion
/// @see Chunk
public sealed interface NBTElement permits ChunkRegion, Chunk, Tag, NBTParent {
    /// Returns the parent of this element, or `null` if this element is not a child of any parent.
    @Contract(pure = true)
    @Nullable NBTParent<?> getParent();

    /// Returns `true` if this element is the root element of an NBT structure, i.e. it has no parent.
    @Contract(pure = true)
    default boolean isRoot() {
        return getParent() == null;
    }

}
