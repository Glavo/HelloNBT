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

import org.glavo.nbt.NBTParent;
import org.glavo.nbt.internal.input.DataReader;
import org.glavo.nbt.internal.NBTCodecImpl;
import org.glavo.nbt.internal.output.DataWriter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

public final class CompoundTag extends ParentTag<Tag> {

    private final Map<String, Tag> subTagsByName = new HashMap<>();

    public CompoundTag() {
        this("");
    }

    public CompoundTag(String name) {
        super(name);
    }

    @Override
    void preUpdateSubTagName(Tag tag, String oldName, String newName) throws IllegalArgumentException {
        if (subTagsByName.containsKey(newName)) {
            throw new IllegalArgumentException("The name '" + newName + "' is already used by another subtag");
        }

        Tag removedTag = subTagsByName.remove(oldName);
        if (removedTag != tag) {
            throw new AssertionError("Expected " + tag + ", but got " + removedTag);
        }

        subTagsByName.put(newName, tag);
    }

    @Override
    @Contract(pure = true)
    public TagType<CompoundTag> getType() {
        return TagType.COMPOUND;
    }

    /// Returns the subtag with the given name, or `null` if no such subtag exists.
    @Contract(pure = true)
    public @Nullable Tag get(String name) {
        return subTagsByName.get(name);
    }

    /// {@inheritDoc}
    ///
    /// If another tag with the same name already exists, the old tag will be removed.
    @Override
    @Contract(mutates = "this,param1")
    public void addTag(Tag tag) {
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

                    assert size < tags.length;
                    tags[size++] = tag;

                    updateIndexes(index);
                }

                return;
            } else {
                // Remove the tag from its old parent.
                tag.getParentTag().removeElement(tag);
            }
        }

        // If a tag with the same name already exists, remove it first.
        Tag oldTag = subTagsByName.get(tag.getName());
        if (oldTag != null) {
            this.removeElement(oldTag);
        }

        // Set the parent and index of the tag.
        tag.setParent(this, size);

        // Add the tag to the subTags list and subTagsByName map.
        ensureCapacityForAdd();
        tags[size++] = tag;
        subTagsByName.put(tag.getName(), tag);
    }

    /// Adds a tag with the given name to this compound tag.
    ///
    /// If the tag is already a child of another tag, removes it from the old parent and adds it to this tag.
    ///
    /// If another tag with the same name already exists, the old tag will be removed.
    @Contract(mutates = "this,param2")
    public void put(String name, Tag tag) {
        @SuppressWarnings("unchecked")
        var oldParent = (NBTParent<Tag>) tag.getParent();
        if (oldParent != null) {
            oldParent.removeElement(tag);
        }

        tag.setName(name);
        addTag(tag);
    }

    /// Adds a byte tag with the given name and value to this compound tag.
    @Contract(mutates = "this")
    public void putByte(String name, byte value) {
        addTag(new ByteTag(name, value));
    }

    /// Adds a byte tag with the given name and value to this compound tag.
    @Contract(mutates = "this")
    public void putBoolean(String name, boolean value) {
        addTag(new ByteTag(name, value));
    }

    /// Adds a short tag with the given name and value to this compound tag.
    @Contract(mutates = "this")
    public void putShort(String name, short value) {
        addTag(new ShortTag(name, value));
    }

    /// Adds an int tag with the given name and value to this compound tag.
    @Contract(mutates = "this")
    public void putInt(String name, int value) {
        addTag(new IntTag(name, value));
    }

    /// Adds a long tag with the given name and value to this compound tag.
    @Contract(mutates = "this")
    public void putLong(String name, long value) {
        addTag(new LongTag(name, value));
    }

    /// Adds a float tag with the given name and value to this compound tag.
    @Contract(mutates = "this")
    public void putFloat(String name, float value) {
        addTag(new FloatTag(name, value));
    }

    /// Adds a double tag with the given name and value to this compound tag.
    @Contract(mutates = "this")
    public void putDouble(String name, double value) {
        addTag(new DoubleTag(name, value));
    }

    /// Adds a string tag with the given name and value to this compound tag.
    @Contract(mutates = "this")
    public void putString(String name, String value) {
        addTag(new StringTag(name, value));
    }

    /// Adds a byte array tag with the given name and value to this compound tag.
    @Contract(mutates = "this")
    public void putByteArray(String name, byte[] value) {
        addTag(new ByteArrayTag(name, value));
    }

    /// Adds an int array tag with the given name and value to this compound tag.
    @Contract(mutates = "this")
    public void putIntArray(String name, int[] value) {
        addTag(new IntArrayTag(name, value));
    }

    /// Adds a long array tag with the given name and value to this compound tag.
    @Contract(mutates = "this")
    public void putLongArray(String name, long[] value) {
        addTag(new LongArrayTag(name, value));
    }

    /// Adds an int array tag with the given name and UUID value to this compound tag.
    @Contract(mutates = "this")
    public void putUUID(String name, UUID value) {
        addTag(new IntArrayTag(name, value));
    }

    @Override
    public Tag removeTagAt(int index) throws IndexOutOfBoundsException {
        Objects.checkIndex(index, size);

        Tag tag = removeTagFromArray(index);
        assert tag.getIndex() == index && tag.getParentTag() == this;

        // Clear the tag's parent and index.
        tag.setParent(null, -1);

        // Remove the tag from the subTagsByName map.
        Tag removedFromMap = subTagsByName.remove(tag.getName());
        if (removedFromMap != tag) {
            throw new AssertionError("Expected " + tag + ", but got " + removedFromMap);
        }

        // Update the index of the successor tags.
        updateIndexes(index);

        return tag;
    }

    @Override
    @Contract(mutates = "this")
    public void clear() {
        super.clear();
        subTagsByName.clear();
    }

    @Override
    protected void readContent(DataReader reader) throws IOException {
        int count = 0;

        Tag subTag;
        while ((subTag = NBTCodecImpl.readTag(reader)) != null) {
            count++;
            addTag(subTag);
        }

        if (count != this.size()) {
            throw new IOException("Duplicate subtags found in compound tag");
        }
    }

    @Override
    protected void writeContent(DataWriter writer) throws IOException {
        for (int i = 0; i < size; i++) {
            NBTCodecImpl.writeTag(writer, tags[i]);
        }
        writer.writeByte((byte) 0x00);
    }

    @Override
    @Contract(pure = true)
    public int contentHashCode() {
        return subTagsByName.hashCode();
    }

    @Override
    @Contract(pure = true)
    public boolean contentEquals(Tag other) {
        return other instanceof CompoundTag that && subTagsByName.equals(that.subTagsByName);
    }

    @Override
    protected void contentToString(StringBuilder builder) {
        builder.append('[');

        if (size > 0) {
            tags[0].toString(builder);
            for (int i = 1; i < size; i++) {
                builder.append(", ");
                tags[i].toString(builder);
            }
        }
        builder.append(']');
    }

    @Override
    @Contract(value = "-> new", pure = true)
    public CompoundTag clone() {
        var newTag = new CompoundTag(this.name);
        for (int i = 0; i < size; i++) {
            newTag.addTag(tags[i].clone());
        }
        return newTag;
    }
}
