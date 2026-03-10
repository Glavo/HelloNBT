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

import org.glavo.nbt.NBTElement;
import org.glavo.nbt.NBTParent;
import org.glavo.nbt.internal.input.DataReader;
import org.glavo.nbt.internal.output.DataWriter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;

/// Represents a tag in NBT (Named Binary Tag) format.
///
/// A tag is a basic unit of data in NBT, which have [a type][#getType()], [a name][#getName()], and a payload.
///
/// The payload of a tag can be a primitive value, an array of primitive values, or a collection of other tags.
///
/// These are the all possible types of tags:
///
/// - [ValueTag]: A tag that holds a single value.
///     - [ByteTag]: A tag that holds a 1 byte signed integer. Sometimes used for booleans.
///     - [ShortTag]: A tag that holds a 2 byte signed integer.
///     - [IntTag]: A tag that holds a 4 byte signed integer.
///     - [LongTag]: A tag that holds an 8 byte signed integer.
///     - [FloatTag]: A tag that holds a 4 byte floating point number.
///     - [DoubleTag]: A tag that holds an 8 byte floating point number.
///     - [StringTag]: A tag that holds a Unicode string.
///     - [ArrayTag]: A tag that holds an array of primitive values.
///         - [ByteArrayTag]: A tag that holds an array of 1 byte signed integers.
///         - [IntArrayTag]: A tag that holds an array of 4 byte signed integers.
///         - [LongArrayTag]: A tag that holds an array of 8 byte signed integers.
/// - [ParentTag]: A tag that holds a collection of other tags.
///     - [CompoundTag]: A tag that holds a collection of named tags.
///     - [ListTag]: A tag that holds a collection of unnamed tags.
///
/// @author Glavo
/// @see <a href="https://minecraft.wiki/w/NBT_format">NBT format - Minecraft Wiki</a>
public sealed abstract class Tag implements NBTElement
        permits ValueTag, ParentTag {

    String name;

    private @Nullable NBTParent<? extends Tag> parent;
    private int index = -1;

    protected Tag(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    /// Returns the type of the tag.
    public abstract TagType<?> getType();

    /// Returns the name of the tag, or an empty string if it has no name.
    @Contract(pure = true)
    public String getName() {
        return name;
    }

    /// Set the name of the tag.
    ///
    /// @throws IllegalStateException if this tag is a child of a parent tag and the name is not valid for the parent tag.
    @Contract(mutates = "this")
    public void setName(String name) throws IllegalStateException {
        // If the name is the same as the current name, do nothing.
        if (name.equals(this.name)) { // implicit null check
            return;
        }

        if (parent instanceof ParentTag<?> parentTag) {
            parentTag.updateSubTagName(this, name);
        } else {
            this.name = name;
        }
    }

    /// If the tag is a child of a [parent][NBTParent], returns the index of the tag in its parent; otherwise, returns `-1`.
    @Contract(pure = true)
    public int getIndex() {
        return index;
    }

    /// Updates the index of this tag in its parent tag.
    void setIndex(int index) {
        this.index = index;
    }

    /// If the tag is a child of a [parent][NBTParent], returns the parent; otherwise, returns `null`.
    @Override
    @Contract(pure = true)
    public @Nullable NBTParent<? extends Tag> getParent() {
        return parent;
    }

    /// If the tag is a child of a [parent tag][ParentTag], returns the parent tag; otherwise, returns `null`.
    @Contract(pure = true)
    public @Nullable ParentTag<?> getParentTag() {
        return parent instanceof ParentTag<?> parentTag ? parentTag : null;
    }

    /// Sets the parent of this tag.
    ///
    /// Internal method, should only be called by [ParentTag] or [org.glavo.nbt.chunk.Chunk] when adding or removing subtags.
    @Contract(mutates = "this")
    void setParent(@Nullable NBTParent<? extends Tag> parent, int index) {
        assert parent == null ^ index >= 0;
        this.parent = parent;
        this.index = index;
    }

    /// Internal method for reading the content of the tag.
    protected abstract void readContent(DataReader reader) throws IOException;

    /// Internal method for writing the content of the tag.
    protected abstract void writeContent(DataWriter writer) throws IOException;

    protected abstract void contentToString(StringBuilder builder);

    protected void appendString(StringBuilder builder, String value) {
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);

            switch (ch) {
                case '\0' -> builder.append("\\0");
                case '\b' -> builder.append("\\b");
                case '\t' -> builder.append("\\t");
                case '\n' -> builder.append("\\n");
                case '\f' -> builder.append("\\f");
                case '\r' -> builder.append("\\r");
                case '"' -> builder.append("\"");
                case ' ' -> builder.append(' ');
                default -> {
                    if (Character.isJavaIdentifierPart(ch)) {
                        builder.appendCodePoint(ch);
                    } else {
                        builder.append("\\u%04x".formatted((int) ch));
                    }
                }
            }
        }
    }

    protected void toString(StringBuilder builder) {
        builder.append(getType().getFullName());
        if (!name.isEmpty()) {
            builder.append('(');
            appendString(builder, name);
            builder.append(')');
        }

        if (this instanceof ArrayTag<?> || this instanceof ParentTag<?>) {
            this.contentToString(builder);
        } else {
            builder.append('[');
            this.contentToString(builder);
            builder.append(']');
        }
    }

    @Override
    public String toString() {
        var builder = new StringBuilder();
        toString(builder);
        return builder.toString();
    }

    /// Returns a new tag with the same name and content as the current tag.
    ///
    /// This method always performs a deep copy, and the returned tag will not have a parent tag.
    @Override
    @Contract(pure = true)
    public abstract Tag clone();

    /// Returns a hash code for this tag.
    ///
    /// The hash code is based on the content of the tag.
    /// The name, parent tag, and index are not considered.
    public abstract int contentHashCode();

    /// Returns `true` if The content of this tag is equal to the content of the given tag.
    ///
    /// The name, parent tag, and index are not considered.
    public abstract boolean contentEquals(Tag other);

    /// Returns a hash code for this tag.
    ///
    /// The hash code is based on the name, type, and content of the tag.
    /// The parent tag and index are not considered.
    @Override
    public int hashCode() {
        return Objects.hash(name, getType(), contentHashCode());
    }

    /// Returns `true` if this tag is equal to the given tag.
    ///
    /// Two tags are considered equal if they have the same name, type, and content.
    /// The parent tag and index are not considered.
    public boolean equals(Object obj) {
        return this == obj || obj instanceof Tag that
                && Objects.equals(this.name, that.name)
                && this.contentEquals(that);
    }

}
