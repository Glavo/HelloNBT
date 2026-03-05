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
import org.glavo.nbt.internal.NBTCodecImpl;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class CompoundTag extends ParentTag<Tag> {

    private final Map<String, Tag> subTagsByName = new HashMap<>();

    public CompoundTag() {
        this("");
    }

    public CompoundTag(String name) {
        super(name);
    }

    @Override
    void updateSubTagName(Tag tag, String name) throws IllegalStateException {
        if (subTagsByName.containsKey(name)) {
            throw new IllegalStateException("The name '" + name + "' is already used by another subtag");
        }

        Tag removedTag = subTagsByName.remove(tag.getName());
        if (removedTag != tag) {
            throw new AssertionError("Expected " + tag + ", but got " + removedTag);
        }

        subTagsByName.put(name, tag);
    }

    @Override
    @Contract(pure = true)
    public TagType getType() {
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
    public void add(Tag tag) {
        if (tag.getParentTag() != null) {
            if (tag.getParentTag() == this) {
                int index = tag.getIndex();

                if (tag.getIndex() == this.size() - 1) {
                    // The tag is already the last child of this tag, so we don't need to do anything.
                } else {
                    // Move the tag to the end of the subTags list.

                    Tag oldTag = subTags.remove(index);
                    if (oldTag != tag) {
                        throw new AssertionError("Expected " + tag + ", but got " + oldTag);
                    }

                    subTags.add(tag);

                    updateIndexes(index);
                }

                return;
            } else {
                // Remove the tag from its old parent.
                tag.getParentTag().remove(tag);
            }
        }

        // If a tag with the same name already exists, remove it first.
        Tag oldTag = subTagsByName.get(tag.getName());
        if (oldTag != null) {
            remove(oldTag);
        }

        // Set the parent and index of the tag.
        tag.setParent(this, subTags.size());

        // Add the tag to the subTags list and subTagsByName map.
        subTags.add(tag);
        subTagsByName.put(tag.getName(), tag);
    }

    /// Adds a byte tag with the given name and value to this compound tag.
    @Contract(mutates = "this")
    public void putByte(String name, byte value) {
        add(new ByteTag(name, value));
    }

    /// Adds a short tag with the given name and value to this compound tag.
    @Contract(mutates = "this")
    public void putShort(String name, short value) {
        add(new ShortTag(name, value));
    }

    /// Adds an int tag with the given name and value to this compound tag.
    @Contract(mutates = "this")
    public void putInt(String name, int value) {
        add(new IntTag(name, value));
    }

    /// Adds a long tag with the given name and value to this compound tag.
    @Contract(mutates = "this")
    public void putLong(String name, long value) {
        add(new LongTag(name, value));
    }

    /// Adds a float tag with the given name and value to this compound tag.
    @Contract(mutates = "this")
    public void putFloat(String name, float value) {
        add(new FloatTag(name, value));
    }

    /// Adds a double tag with the given name and value to this compound tag.
    @Contract(mutates = "this")
    public void putDouble(String name, double value) {
        add(new DoubleTag(name, value));
    }

    /// Adds a string tag with the given name and value to this compound tag.
    @Contract(mutates = "this")
    public void putString(String name, String value) {
        add(new StringTag(name, value));
    }

    /// Adds a byte array tag with the given name and value to this compound tag.
    @Contract(mutates = "this")
    public void putByteArray(String name, byte[] value) {
        add(new ByteArrayTag(name, value));
    }

    /// Adds an int array tag with the given name and value to this compound tag.
    @Contract(mutates = "this")
    public void putIntArray(String name, int[] value) {
        add(new IntArrayTag(name, value));
    }

    /// Adds a long array tag with the given name and value to this compound tag.
    @Contract(mutates = "this")
    public void putLongArray(String name, long[] value) {
        add(new LongArrayTag(name, value));
    }

    @Override
    @Contract(mutates = "this,param1")
    public void remove(Tag tag) {
        if (tag.getParentTag() != this) {
            throw new IllegalArgumentException("The tag is not a child of this tag");
        }

        // Remove the tag from the subTagsByName map.
        Tag removed = subTagsByName.remove(tag.getName());
        if (removed != tag) {
            throw new AssertionError("Expected " + tag + ", but got " + removed);
        }

        // Remove the tag from the subTags list.
        int subtagIndex = tag.getIndex();
        if (subtagIndex < 0 || subtagIndex >= subTags.size()) {
            throw new AssertionError("Expected subtag index in range [0, " + subTags.size() + "), but got " + subtagIndex);
        }

        removed = subTags.remove(subtagIndex);
        if (removed != tag) {
            throw new AssertionError("Expected " + tag + ", but got " + removed);
        }

        // Clear the tag's parent and index.
        tag.setParent(null, -1);

        // Update the index of the successor tags.
        updateIndexes(subtagIndex);
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
            add(subTag);
        }

        if (count != this.size()) {
            throw new IOException("Duplicate subtags found in compound tag");
        }
    }

    @Override
    protected int contentHashCode() {
        return subTagsByName.hashCode();
    }

    @Override
    protected boolean contentEquals(Tag other) {
        return other instanceof CompoundTag that && subTagsByName.equals(that.subTagsByName);
    }

    @Override
    protected void contentToString(StringBuilder builder) {
        builder.append('[');

        if (!subTags.isEmpty()) {
            Iterator<Tag> it = subTags.iterator();
            it.next().toString(builder);

            while (it.hasNext()) {
                builder.append(", ");
                it.next().toString(builder);
            }
        }
        builder.append(']');
    }

    @Override
    @Contract(value = "-> new", pure = true)
    public CompoundTag clone() {
        var newTag = new CompoundTag(this.name);
        newTag.subTags.ensureCapacity(this.size());
        for (Tag tag : this) {
            newTag.add(tag.clone());
        }
        return newTag;
    }
}
