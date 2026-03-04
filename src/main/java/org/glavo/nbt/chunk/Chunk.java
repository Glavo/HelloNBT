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
import org.glavo.nbt.internal.Access;
import org.glavo.nbt.internal.ChunkUtils;
import org.glavo.nbt.tag.CompoundTag;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public final class Chunk implements NBTParent<CompoundTag>, NBTElement {
    @Nullable ChunkRegion region;
    int localIndex;

    @Nullable CompoundTag rootTag;

    public Chunk() {
    }

    public Chunk(@Nullable CompoundTag rootTag) {
        setRootTag(rootTag);
    }

    /// Return the region of this chunk, or `null` if this chunk is not in any region.
    @Contract(pure = true)
    public @Nullable ChunkRegion getRegion() {
        return region;
    }

    void setRegion(@Nullable ChunkRegion region, int localIndex) {
        this.region = region;
        this.localIndex = localIndex;
    }

    /// Return the local index of this chunk in its region, or -1 if this chunk is not in any region.
    @Contract(pure = true)
    public int getLocalIndex() {
        return localIndex;
    }

    /// Return the local x coordinate of this chunk in its region, or -1 if this chunk is not in any region.
    @Contract(pure = true)
    public int getLocalX() {
        return localIndex >= 0 ? ChunkUtils.getLocalX(localIndex) : -1;
    }

    /// Return the local z coordinate of this chunk in its region, or -1 if this chunk is not in any region.
    @Contract(pure = true)
    public int getLocalZ() {
        return localIndex >= 0 ? ChunkUtils.getLocalZ(localIndex) : -1;
    }

    @Contract(pure = true)
    public @Nullable CompoundTag getRootTag() {
        return rootTag;
    }

    @Contract(mutates = "this,param1")
    public void setRootTag(@Nullable CompoundTag rootTag) {
        // TODO: remove old root tag from its parent
        this.rootTag = rootTag;
    }

    @Override
    @Contract(mutates = "this,param1")
    public void remove(CompoundTag element) throws IllegalArgumentException {
        if (element.getParent() != null) {
            throw new IllegalArgumentException("The root tag is not a root element");
        }

        if (element != rootTag) {
            throw new AssertionError("Expected " + element + ", but got " + rootTag);
        }
        if (element.getIndex() != 0) {
            throw new AssertionError("Expected index 0, but got " + element.getIndex());
        }

        Access.TAG.setParent(rootTag, null, -1);
        rootTag = null;
    }

    @Override
    public String toString() {
        return "Chunk[x=" + getLocalX() + ", z=" + getLocalZ() + ", root=" + rootTag + ']';
    }
}
