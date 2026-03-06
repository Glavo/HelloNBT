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
package org.glavo.nbt.chunk;

import org.glavo.nbt.NBTElement;
import org.glavo.nbt.NBTParent;
import org.glavo.nbt.internal.ChunkUtils;
import org.glavo.nbt.internal.input.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/// @see <a href="https://minecraft.wiki/w/Region_file_format">Region file format - Minecraft Wiki</a>
/// @see <a href="https://minecraft.wiki/w/Anvil_file_format">Anvil file format - Minecraft Wiki</a>
public final class ChunkRegion implements NBTParent<Chunk>, NBTElement, Iterable<Chunk> {

    private final @Nullable Chunk[] chunks = new Chunk[ChunkUtils.CHUNKS_PRE_REGION];

    public ChunkRegion() {
    }

    /// Always returns `null`. A chunk region is the root of the NBT tree, and it has no parent.
    @Override
    @Contract(value = "-> null", pure = true)
    public @Nullable NBTParent<ChunkRegion> getParent() {
        return null;
    }

    @Contract(pure = true)
    public Chunk getChunk(int localIndex) {
        Objects.checkIndex(localIndex, ChunkUtils.CHUNKS_PRE_REGION);
        Chunk chunk = chunks[localIndex];
        if (chunk == null) {
            chunks[localIndex] = chunk = new Chunk(this, localIndex);
        }
        return chunk;
    }

    @Contract(pure = true)
    public Chunk getChunk(int x, int z) {
        Objects.checkIndex(x, ChunkUtils.CHUNKS_PER_REGION_SIDE);
        Objects.checkIndex(z, ChunkUtils.CHUNKS_PER_REGION_SIDE);

        return getChunk(ChunkUtils.toLocalIndex(x, z));
    }

    @Contract(mutates = "this,param2")
    public void setChunk(int localIndex, Chunk chunk) {
        Objects.checkIndex(localIndex, ChunkUtils.CHUNKS_PRE_REGION);
        Objects.requireNonNull(chunk);

        Chunk old = chunks[localIndex];
        if (old != null) {
            old.setRegion(null, -1);
        }

        if (chunk.getParent() != null) {
            // The chunk is already in another region, so we need to remove it from its old region first.
            ChunkRegion oldRegion = chunk.getParent();
            oldRegion.remove(chunk);
        }

        chunk.setRegion(this, localIndex);
        chunks[localIndex] = chunk;
    }

    @Contract(mutates = "this,param3")
    public void setChunk(int x, int z, Chunk chunk) {
        Objects.checkIndex(x, ChunkUtils.CHUNKS_PER_REGION_SIDE);
        Objects.checkIndex(z, ChunkUtils.CHUNKS_PER_REGION_SIDE);
        setChunk(ChunkUtils.toLocalIndex(x, z), chunk);
    }

    @Override
    public Iterator<Chunk> iterator() {
        return new Iterator<>() {
            private int cursor;

            @Override
            public boolean hasNext() {
                return cursor < ChunkUtils.CHUNKS_PRE_REGION;
            }

            @Override
            public Chunk next() {
                if (cursor >= ChunkUtils.CHUNKS_PRE_REGION) {
                    throw new NoSuchElementException();
                }
                return getChunk(cursor++);
            }
        };
    }

    /// Remove the chunk at the given local index from this region.
    ///
    /// After removing the chunk, the original local index of the chunk will point to a new blank chunk.
    ///
    /// @throws IllegalArgumentException if the chunk is not in this region.
    @Override
    @Contract(mutates = "this,param1")
    public void remove(Chunk chunk) throws IllegalArgumentException {
        if (chunk.getParent() != this) {
            throw new IllegalArgumentException("The chunk is not in this region");
        }

        int localIndex = chunk.getLocalIndex();

        Chunk old = chunks[localIndex];
        if (old != chunk) {
            throw new AssertionError("Expected " + chunk + ", but got " + old);
        }
        chunk.setRegion(null, -1);
        chunks[localIndex] = null;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (Chunk chunk : this) {
            hash = 31 * hash + chunk.hashCode();
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof ChunkRegion that) {
            for (int i = 0; i < chunks.length; i++) {
                if (!this.getChunk(i).equals(that.getChunk(i))) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(65536);
        builder.append("Chunk[");

        for (int i = 0; i < chunks.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }

            builder.append(getChunk(i));
        }

        builder.append(']');

        return builder.toString();
    }
}
