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

import org.glavo.nbt.internal.input.DataReader;
import org.glavo.nbt.internal.output.DataWriter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;

/// A [parent tag][ParentTag] that holds an ordered collection of isomorphic unnamed tags.
///
/// All elements in the list must have the same type, and its name will always be empty.
///
/// For heterogeneous lists in SNBT, the [ListTag#addAny(Tag)] method can be used to simulate their behavior.
///
/// @see Tag
/// @see ParentTag
public final class ListTag<T extends Tag> extends ParentTag<T> {

    /// The type of the elements in the list.
    private @Nullable TagType<?> elementType;

    /// Creates a new empty list tag with an empty name and no element type.
    public ListTag() {
        this("", null);
    }

    /// Creates a new empty list tag with the given element type.
    ///
    /// @param elementType The type of the elements in the list.
    public ListTag(@Nullable TagType<? super T> elementType) {
        this("", elementType);
    }

    /// Creates a new empty list tag with the given name and no element type.
    ///
    /// @param name The name of the list tag.
    public ListTag(String name) {
        this(name, null);
    }

    /// Creates a new empty list tag with the given name and element type.
    ///
    /// @param name        The name of the list tag.
    /// @param elementType The type of the elements in the list.
    public ListTag(String name, @Nullable TagType<? super T> elementType) {
        super(name);
        this.elementType = elementType;
    }

    @Override
    void preUpdateSubTagName(Tag tag, String oldName, String newName) throws IllegalArgumentException {
        if (!newName.isEmpty()) {
            throw new IllegalArgumentException("The name of the subtag must be null for ListTag");
        }
    }

    @Override
    @Contract(pure = true)
    public TagType<ListTag<?>> getType() {
        return TagType.LIST;
    }

    /// Returns the type of the elements in the list.
    ///
    /// If the element type is `null`, the list is empty.
    @Contract(pure = true)
    public @Nullable TagType<?> getElementType() {
        return elementType;
    }

    /// Sets the type of the elements in the list.
    ///
    /// If the list is empty, the element type will be set to the given type;
    /// If the new element type is [TagType#COMPOUND], the list will be converted to a list of compound tags,
    /// every element will be converted to a compound tag with a single subtag.
    @Contract(mutates = "this")
    public void setElementType(@Nullable TagType<?> elementType) throws IllegalStateException {
        if (this.elementType == elementType) {
            return;
        }

        if (isEmpty()) {
            this.elementType = elementType;
        } else if (elementType == null) {
            throw new IllegalStateException("Cannot set element type to END for a non-empty list");
        } else if (elementType != TagType.COMPOUND) {
            throw new IllegalStateException("Cannot set element type to " + elementType + " for a " + this.elementType + " list");
        } else {
            var oldTags = tags.clone();

            this.clear();
            this.elementType = elementType;

            for (Tag subTag : oldTags) {
                assert subTag.getName().isEmpty();
                assert subTag.getParent() == null;
                assert subTag.getIndex() == -1;

                CompoundTag newSubTag = new CompoundTag();
                newSubTag.addTag(subTag);

                @SuppressWarnings("unchecked")
                T castedTag = (T) newSubTag;
                this.addTag(castedTag);
            }

        }
    }

    /// {@inheritDoc}
    ///
    /// The name of the tag will be set to empty.
    ///
    /// If the [element type](#getElementType) is `null`, the element type will be set to the type of the tag.
    ///
    /// @throws IllegalArgumentException if the type of the tag is not the same as the element type of this list.
    @Override
    @Contract(mutates = "this,param1")
    public void addTag(T tag) {
        if (tag.getType() != elementType) { // implicit null check
            if (this.elementType == null) {
                assert isEmpty();
                this.elementType = tag.getType();
            } else {
                throw new IllegalArgumentException("Cannot add a tag of type " + tag.getType() + " to a list of type " + elementType);
            }
        }

        if (tag.getParentTag() != null) {
            if (tag.getParentTag() == this) {
                int index = tag.getIndex();

                if (tag.getIndex() == this.size() - 1) {
                    // The tag is already the last child of this tag, so we don't need to do anything.
                } else {
                    // Move the tag to the end of the subTags list.

                    Tag oldTag = removeTagFromArray(index);
                    if (oldTag != tag) {
                        throw new AssertionError("Expected " + tag + ", but got " + oldTag);
                    }

                    tags[size - 1] = tag;
                    updateIndexes(index);
                }

                return;
            } else {
                tag.getParentTag().removeElement(tag);
            }
        }

        // Clear the name of the tag.
        tag.name = "";

        // Set the parent and index of the tag.
        tag.setParent(this, size);

        // Add the tag to the subTags list.
        ensureTagsCapacityForAdd();
        tags[size++] = tag;
    }

    /// For the heterogeneous list in SNBT, this method can be used to add any tag to the list.
    ///
    /// If the tag is of the same type as the element type of this list, it will be added directly.
    ///
    /// If the element type is not equal to the type of the tag:
    /// - If the list is empty, the element type of this list will be set to the type of the tag, and the tag will be added.
    /// - If the list is not empty, the element type of this list will be set to the [TagType#COMPOUND],
    /// and all existing tags and the new tag will be converted to compound tags with a single unnamed subtag.
    @Contract(mutates = "this,param1")
    @SuppressWarnings("unchecked")
    public void addAny(T tag) {
        if (elementType == null || tag.getType() == elementType) {
            addTag(tag);
        } else if (this.isEmpty()) {
            elementType = tag.getType();
            addTag(tag);
        } else {
            setElementType(TagType.COMPOUND);

            CompoundTag subTag = new CompoundTag();
            subTag.put("", tag);
            addTag((T) subTag);
        }
    }

    @Override
    public T removeTagAt(int index) throws IndexOutOfBoundsException {
        Objects.checkIndex(index, size);

        T tag = removeTagFromArray(index);
        assert tag.getIndex() == index && tag.getParentTag() == this;

        // Clear the tag's parent and index.
        tag.setParent(null, -1);

        // Decrease the size.
        size--;

        // Update the index of the successor tags.
        updateIndexes(index);

        return tag;
    }

    @Override
    void readContent(DataReader reader) throws IOException {
        byte elementTypeId = reader.readByte();

        TagType<?> elementType;
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
            uncheckedListTag.addTag(subTag);
        }
    }

    @Override
    void writeContent(DataWriter writer) throws IOException {
        writer.writeByte(getElementType() != null ? getElementType().id() : 0);
        writer.writeInt(size());

        for (int i = 0; i < size; i++) {
            tags[i].writeContent(writer);
        }
    }

    @Override
    @Contract(pure = true)
    public int contentHashCode() {
        int hashCode = 0;
        for (int i = 0; i < size; i++) {
            hashCode = 31 * hashCode + tags[i].contentHashCode();
        }
        return hashCode;
    }

    @Override
    @Contract(pure = true)
    public boolean contentEquals(Tag other) {
        if (other instanceof ListTag<?> that) {
            if (this.size != that.size) {
                return false;
            }

            for (int i = 0; i < size; i++) {
                if (!tags[i].contentEquals(that.tags[i])) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    void contentToString(StringBuilder builder) {
        builder.append('[');
        if (getElementType() != null)
            builder.append(getElementType().getFullName()).append(';');

        if (size > 0) {
            tags[0].contentToString(builder);
            for (int i = 1; i < size; i++) {
                builder.append(", ");
                tags[i].contentToString(builder);
            }
        }
        builder.append(']');
    }

    @Override
    @Contract(value = "-> new", pure = true)
    @SuppressWarnings("unchecked")
    public ListTag<T> clone() {
        var newTag = new ListTag<>(this.name, (TagType<T>) this.elementType);
        if (size > 0) {
            Tag[] newArray = new Tag[size];
            for (int i = 0; i < size; i++) {
                newArray[i] = tags[i].clone();
            }
            newTag.tags = newArray;
            newTag.size = size;
        }
        return newTag;
    }
}
