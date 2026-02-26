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

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class CompoundTag<T extends Tag> extends ParentTag<T> {

    private final Map<String, T> subTagsByName = new HashMap<>();

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

        T removedTag = subTagsByName.remove(tag.getName());
        if (removedTag != tag) {
            throw new AssertionError("Expected " + tag + ", but got " + removedTag);
        }

        @SuppressWarnings("unchecked")
        T castedTag = (T) tag;
        subTagsByName.put(name, castedTag);
    }

    @Override
    public TagType getType() {
        return TagType.COMPOUND;
    }

    /// Returns the subtag with the given name, or `null` if no such subtag exists.
    /// The subtag name must not be null.
    public @Nullable T get(String name) {
        return subTagsByName.get(name);
    }

    /// {@inheritDoc}
    ///
    /// If another tag with the same name already exists, the old tag will be removed.
    @Override
    public void add(T tag) {
        if (tag.getParent() != null) {
            if (tag.getParent() == this) {
                // The tag is already a child of this tag.
                return;
            } else {
                // Remove the tag from its old parent.
                tag.getParent().remove(tag);
            }
        }

        // If a tag with the same name already exists, remove it first.
        T oldTag = subTagsByName.get(tag.getName());
        if (oldTag != null) {
            remove(oldTag);
        }

        // Set the parent and index of the tag.
        tag.parent = this;
        tag.index = subTags.size();

        // Add the tag to the subTags list and subTagsByName map.
        subTags.add(tag);
        subTagsByName.put(tag.getName(), tag);
    }

    @Override
    public void remove(Tag tag) {
        if (tag.getParent() != this) {
            throw new IllegalArgumentException("The tag is not a child of this tag");
        }

        // Remove the tag from the subTagsByName map.
        T removedNode = subTagsByName.remove(tag.getName());
        if (removedNode != tag) {
            throw new AssertionError("Expected " + tag + ", but got " + removedNode);
        }

        // Remove the tag from the subTags list.
        int subtagIndex = tag.getIndex();
        if (subtagIndex < 0 || subtagIndex >= subTags.size()) {
            throw new AssertionError("Expected subtag index in range [0, " + subTags.size() + "), but got " + subtagIndex);
        }
        subTags.remove(subtagIndex);

        // Clear the tag's parent and index.
        tag.index = -1;
        tag.parent = null;

        // Update the index of the successor tags.
        for (int i = subtagIndex; i < subTags.size(); i++) {
            subTags.get(i).index--;
        }
    }
}
