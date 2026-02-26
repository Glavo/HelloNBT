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

public final class ListTag<T extends Tag> extends ParentTag<T> {

    @Override
    void updateSubTagName(Tag tag, @Nullable String name) throws IllegalStateException {
        if (name != null) {
            throw new IllegalStateException("The name of the subtag must be null for ListTag");
        }
    }

    @Override
    public TagType getType() {
        return TagType.LIST;
    }

    /// {@inheritDoc}
    ///
    /// The name of the tag will be set to `null`.
    @Override
    public void add(T tag) {
        if (tag.getParent() != null) {
            if (tag.getParent() == this) {
                // The tag is already a child of this tag.
                return;
            } else {
                tag.getParent().remove(tag);
            }
        }

        // Clear the name of the tag.
        tag.name = null;

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
