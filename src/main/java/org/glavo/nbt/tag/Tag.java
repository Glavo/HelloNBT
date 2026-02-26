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
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Objects;

/// @author Glavo
public sealed abstract class Tag
        permits ByteTag, ShortTag, IntTag, LongTag, FloatTag, DoubleTag, ArrayTag, ParentTag {
    @Nullable ParentTag<?> parent;

    @Nullable String name;
    int index;

    /// Returns the type of the tag.
    public abstract TagType getType();

    /// Returns the name of the tag, or `null` if it has no name.
    @Contract(pure = true)
    public @Nullable String getName() {
        return name;
    }

    /// Set the name of the tag.
    ///
    /// @throws IllegalStateException if this tag is a child of a parent tag and the name is not valid for the parent tag.
    public void setName(@Nullable String name) throws IllegalStateException {
        // If the name is the same as the current name, do nothing.
        if (Objects.equals(this.name, name)) {
            return;
        }

        if (parent != null) {
            parent.updateSubTagName(this, name);
        } else {
            this.name = name;
        }
    }

    /// If the tag is a child of a [parent tag][ParentTag], returns the index of the tag in its parent; otherwise, returns `-1`.
    @Range(from = -1, to = Integer.MAX_VALUE)
    @Contract(pure = true)
    public int getIndex() {
        return index;
    }

    /// If the tag is a child of a [parent tag][ParentTag], returns the parent tag; otherwise, returns `null`.
    @Contract(pure = true)
    public @Nullable ParentTag<?> getParent() {
        return parent;
    }
}
