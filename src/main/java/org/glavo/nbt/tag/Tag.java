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

import java.lang.invoke.MethodHandles;
import java.util.Objects;

/// @author Glavo
public sealed abstract class Tag
        permits ByteTag, ShortTag, IntTag, LongTag, FloatTag, DoubleTag, StringTag, ArrayTag, ParentTag {
    @Nullable ParentTag<?> parent;

    String name = "";
    int index;

    protected Tag(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    /// Returns the type of the tag.
    public abstract TagType getType();

    /// Returns the name of the tag, or an empty string if it has no name.
    @Contract(pure = true)
    public String getName() {
        return name;
    }

    /// Set the name of the tag.
    ///
    /// @throws IllegalStateException if this tag is a child of a parent tag and the name is not valid for the parent tag.
    public void setName(String name) throws IllegalStateException {
        // If the name is the same as the current name, do nothing.
        if (this.name.equals(name)) {
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

    /// Unsafe operations for internal use.
    public static final class Unsafe {
        private static final Unsafe INSTANCE = new Unsafe();

        /// Get an instance of the unsafe operations.
        ///
        /// @param lookup A lookup object used to check whether a user has access rights to the [Tag].
        /// @return An instance of the unsafe operations.
        /// @throws IllegalAccessException if the user does not have access rights to the [Tag].
        public static Unsafe getInstance(MethodHandles.Lookup lookup) throws IllegalAccessException {
            MethodHandles.privateLookupIn(Tag.class, lookup);
            return INSTANCE;
        }

        private Unsafe() {
        }

        /// Returns the internal value of the tag without cloning.
        public byte[] getInternalArray(ByteArrayTag tag) {
            return tag.value;
        }

        /// Sets the internal value of the tag without cloning.
        public void setInternalArray(ByteArrayTag tag, byte[] value) {
            tag.value = value;
        }

        /// Returns the internal value of the tag without cloning.
        public int[] getInternalArray(IntArrayTag tag) {
            return tag.value;
        }

        /// Sets the internal value of the tag without cloning.
        public void setInternalArray(IntArrayTag tag, int[] value) {
            tag.value = value;
        }

        /// Returns the internal value of the tag without cloning.
        public long[] getInternalArray(LongArrayTag tag) {
            return tag.value;
        }

        /// Sets the internal value of the tag without cloning.
        public void setInternalArray(LongArrayTag tag, long[] value) {
            tag.value = value;
        }
    }
}
