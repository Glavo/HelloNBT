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
import org.glavo.nbt.internal.ChunkMetadata;
import org.glavo.nbt.internal.ChunkUtils;
import org.glavo.nbt.tag.CompoundTag;
import org.jetbrains.annotations.Nullable;

public final class Chunk implements NBTParent<CompoundTag>, NBTElement {
    @Nullable ChunkRegion region;
    int localIndex = -1;

    CompoundTag rootTag;

    Chunk(ChunkRegion region, int localIndex) {
        this.region = region;
        this.localIndex = localIndex;
    }

    Chunk(ChunkRegion region, ChunkMetadata metadata, CompoundTag rootTag) {
        this.region = region;
        this.localIndex = metadata.localIndex();
        this.rootTag = rootTag;
    }

    public Chunk() {
    }

    /// Return the region of this chunk, or `null` if this chunk is not in any region.
    public @Nullable ChunkRegion getRegion() {
        return region;
    }

    /// Return the local x coordinate of this chunk in its region, or -1 if this chunk is not in any region.
    public int getLocalX() {
        return localIndex >= 0 ? localIndex % ChunkUtils.CHUNKS_PER_REGION_SIDE : -1;
    }

    /// Return the local z coordinate of this chunk in its region, or -1 if this chunk is not in any region.
    public int getLocalZ() {
        return localIndex >= 0 ? localIndex / ChunkUtils.CHUNKS_PER_REGION_SIDE : -1;
    }

    public CompoundTag getRootTag() {
        return rootTag;
    }
}
