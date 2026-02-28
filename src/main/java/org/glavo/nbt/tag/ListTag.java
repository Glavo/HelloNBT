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

import org.glavo.nbt.internal.input.NBTReader;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Iterator;

public final class ListTag<T extends Tag> extends ParentTag<T> {

    /// The type of the elements in the list.
    private @Nullable TagType elementType;

    /// Creates a new empty list tag with the given element type.
    ///
    /// @param elementType The type of the elements in the list.
    public ListTag(@Nullable TagType elementType) {
        this("", elementType);
    }

    /// Creates a new empty list tag with the given name and element type.
    ///
    /// @param name        The name of the list tag.
    /// @param elementType The type of the elements in the list.
    public ListTag(String name, @Nullable TagType elementType) {
        super(name);
        this.elementType = elementType;
    }

    /// Creates a new empty list tag with the given element type.
    ///
    /// @param elementType The type of the elements in the list.
    /// @throws IllegalArgumentException if the element type is not valid.
    public ListTag(@Nullable Class<? super T> elementType) {
        this("", elementType);
    }

    /// Creates a new empty list tag with the given name and element type.
    ///
    /// @param name        The name of the list tag.
    /// @param elementType The type of the elements in the list.
    /// @throws IllegalArgumentException if the element type is not valid.
    public ListTag(String name, @Nullable Class<? super T> elementType) {
        super(name);

        if (elementType != null) {
            @SuppressWarnings("unchecked")
            TagType tagType = TagType.getByClass((Class<? extends Tag>) elementType);
            if (tagType == null) {
                throw new IllegalArgumentException("Invalid element type: " + elementType);
            }
            this.elementType = tagType;
        } else {
            this.elementType = null;
        }
    }

    @Override
    void updateSubTagName(Tag tag, String name) throws IllegalStateException {
        if (!name.isEmpty()) {
            throw new IllegalStateException("The name of the subtag must be null for ListTag");
        }
    }

    @Override
    public TagType getType() {
        return TagType.LIST;
    }

    /// Returns the type of the elements in the list.
    ///
    /// If the element type is `null`, the list is empty.
    public @Nullable TagType getElementType() {
        return elementType;
    }

    /// Sets the type of the elements in the list.
    ///
    /// If the list is not empty, the element type must match the type of all elements in the list.
    public void setElementType(@Nullable TagType elementType) throws IllegalStateException {
        if (!isEmpty()) {
            if (elementType == null) {
                throw new IllegalStateException("Cannot set element type to END for a non-empty list");
            }

            for (T subTag : subTags) {
                if (subTag.getType() != elementType) {
                    throw new IllegalStateException("Cannot set element type to " + elementType + " for a list with elements of type " + subTag.getType());
                }
            }
        }

        this.elementType = elementType;
    }

    /// {@inheritDoc}
    ///
    /// The name of the tag will be set to empty.
    @Override
    public void add(T tag) {
        if (tag.getType() != elementType) {
            throw new IllegalArgumentException("Cannot add a tag of type " + tag.getType() + " to a list of type " + elementType);
        }

        if (tag.getParent() != null) {
            if (tag.getParent() == this) {
                int index = tag.getIndex();

                if (tag.getIndex() == this.size() - 1) {
                    // The tag is already the last child of this tag, so we don't need to do anything.
                } else {
                    // Move the tag to the end of the subTags list.

                    T oldTag = subTags.remove(index);
                    if (oldTag != tag) {
                        throw new AssertionError("Expected " + tag + ", but got " + oldTag);
                    }

                    subTags.add(tag);

                    updateIndexes(index);
                }

                return;
            } else {
                tag.getParent().remove(tag);
            }
        }

        // Clear the name of the tag.
        tag.name = "";

        // Set the parent and index of the tag.
        tag.parent = this;
        tag.index = subTags.size();

        // Add the tag to the subTags list.
        subTags.add(tag);
    }

    @Override
    public void remove(Tag tag) {
        if (tag.getParent() != this) {
            throw new IllegalArgumentException("The tag is not a child of this tag");
        }

        // Remove the tag from the subTags list.
        int subtagIndex = tag.getIndex();
        if (subtagIndex < 0 || subtagIndex >= subTags.size()) {
            throw new AssertionError("Expected subtag index in range [0, " + subTags.size() + "), but got " + subtagIndex);
        }

        T removedTag = subTags.remove(subtagIndex);
        if (removedTag != tag) {
            throw new AssertionError("Expected " + tag + ", but got " + removedTag);
        }

        // Clear the tag's parent and index.
        tag.index = -1;
        tag.parent = null;

        // Update the index of the successor tags.
        updateIndexes(subtagIndex);
    }

    @Override
    protected void readContent(NBTReader reader) throws IOException {
        byte elementTypeId = reader.readByte();

        TagType elementType;
        if (elementTypeId != 0) {
            elementType = TagType.getById(elementTypeId);
            if (elementType == null) {
                throw new IOException("Invalid element type: %02x".formatted(Byte.toUnsignedInt(elementTypeId)));
            }
        } else {
            elementType = null;
        }

        setElementType(elementType);

        int count = reader.readInt();
        if (count < 0) {
            throw new IOException("Invalid list length: " + Integer.toUnsignedLong(count));
        }

        if (elementType == null && count != 0) {
            throw new IOException("Cannot create a non-empty list with element type END");
        }

        @SuppressWarnings("unchecked")
        var uncheckedListTag = (ListTag<Tag>) this;
        for (int i = 0; i < count; i++) {
            Tag subTag = elementType.createTag();
            subTag.readContent(reader);
            uncheckedListTag.add(subTag);
        }
    }

    @Override
    protected int contentHashCode() {
        return subTags.hashCode();
    }

    @Override
    protected boolean contentEquals(Tag other) {
        return other instanceof ListTag<?> that && subTags.equals(that.subTags);
    }

    @Override
    protected void contentToString(StringBuilder builder) {
        builder.append('[').append(getElementType()).append(';');

        if (!subTags.isEmpty()) {
            Iterator<T> it = subTags.iterator();
            it.next().contentToString(builder);

            while (it.hasNext()) {
                builder.append(", ");
                it.next().contentToString(builder);
            }
        }
        builder.append(']');
    }

    @Override
    @SuppressWarnings("unchecked")
    public ListTag<T> clone() {
        var newTag = new ListTag<T>(this.name, this.elementType);
        newTag.subTags.ensureCapacity(this.size());
        for (T tag : this) {
            newTag.add((T) tag.clone());
        }
        return newTag;
    }
}
