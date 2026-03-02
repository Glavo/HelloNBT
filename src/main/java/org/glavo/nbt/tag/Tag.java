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

import org.glavo.nbt.MinecraftEdition;
import org.glavo.nbt.NBTElement;
import org.glavo.nbt.internal.input.DataReader;
import org.glavo.nbt.internal.input.InputContext;
import org.glavo.nbt.internal.input.InputSource;
import org.glavo.nbt.internal.output.NBTWriter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.util.Objects;

/// @author Glavo
public sealed abstract class Tag implements NBTElement
        permits ValueTag, ArrayTag, ParentTag {

    public static @Nullable Tag readTag(DataReader reader) throws IOException {
        byte tagByte = reader.readByte();
        if (tagByte == 0) {
            return null;
        }

        var type = TagType.getById(tagByte);
        if (type == null) {
            throw new IOException("Invalid tag type: %02x".formatted(Byte.toUnsignedInt(tagByte)));
        }

        Tag tag = type.createTag();
        tag.setName(reader.readString());
        tag.readContent(reader);
        return tag;
    }

    public static Tag readTag(InputStream inputStream) throws IOException {
        return readTag(inputStream, MinecraftEdition.JAVA_EDITION);
    }

    public static Tag readTag(InputStream inputStream, MinecraftEdition edition) throws IOException {
        try (var context = new InputContext(new InputSource.OfInputStream(inputStream, false), edition)) {
            Tag tag = readTag(context.rawReader);
            if (tag == null) {
                throw new IOException("No tag found");
            }
            return tag;
        }
    }

    public static CompoundTag readCompoundTag(InputStream inputStream) throws IOException {
        return readCompoundTag(inputStream, MinecraftEdition.JAVA_EDITION);
    }

    public static CompoundTag readCompoundTag(InputStream inputStream, MinecraftEdition edition) throws IOException {
        Tag rootTag = readTag(inputStream, edition);
        if (rootTag instanceof CompoundTag compoundTag) {
            return compoundTag;
        } else {
            throw new IOException("Expected a compound tag, but got " + rootTag);
        }
    }

    @Nullable ParentTag<?> parent;

    String name;
    int index = -1;

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
        if (name.equals(this.name)) { // implicit null check
            return;
        }

        if (parent != null) {
            parent.updateSubTagName(this, name);
        } else {
            this.name = name;
        }
    }

    /// If the tag is a child of a [parent tag][ParentTag], returns the index of the tag in its parent; otherwise, returns `-1`.
    @Contract(pure = true)
    public int getIndex() {
        return index;
    }

    /// If the tag is a child of a [parent tag][ParentTag], returns the parent tag; otherwise, returns `null`.
    @Contract(pure = true)
    public @Nullable ParentTag<?> getParentTag() {
        return parent;
    }

    public void writeTo(OutputStream outputStream) throws IOException {
        writeTo(outputStream, MinecraftEdition.JAVA_EDITION);
    }

    public void writeTo(OutputStream outputStream, MinecraftEdition edition) throws IOException {
        try (var writer = new NBTWriter(outputStream, edition)) {
            writer.writeTag(this);
        }
    }

    protected abstract void readContent(DataReader reader) throws IOException;

    protected abstract int contentHashCode();

    protected abstract boolean contentEquals(Tag other);

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
    /// This method always performs a deep copy.
    @Override
    public abstract Tag clone();

    /// Returns a hash code for this tag.
    @Override
    public int hashCode() {
        return Objects.hash(name, this.getClass(), contentHashCode());
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

    /// Unsafe operations for internal use.
    public static final class Unsafe {
        private static final Unsafe INSTANCE = new Unsafe();

        /// Get an instance of the unsafe operations.
        ///
        /// @param lookup A lookup object used to check whether a user has access rights to the [Tag].
        /// @return An instance of the unsafe operations.
        /// @throws UnsupportedOperationException if the user does not have access rights to the [Tag].
        public static Unsafe getUnsafe(MethodHandles.Lookup lookup) {
            try {
                MethodHandles.privateLookupIn(Tag.class, lookup);
            } catch (IllegalAccessException e) {
                throw new UnsupportedOperationException(e);
            }
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
