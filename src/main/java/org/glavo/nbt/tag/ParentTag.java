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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/// Base class for tags that can contain other tags as children.
public sealed abstract class ParentTag<E extends Tag> extends Tag
        implements Iterable<E>
        permits CompoundTag, ListTag {

    final ArrayList<E> subTags = new ArrayList<>();

    protected ParentTag(String name) {
        super(name);
    }

    /// Updates the name of the given subtag.
    ///
    /// Used internally by [Tag#setName(String)].
    ///
    /// @see Tag#setName(String)
    abstract void updateSubTagName(Tag tag, String name) throws IllegalStateException;

    protected final void updateIndexes(int startIndex) {
        for (int i = startIndex, end = subTags.size(); i < end; i++) {
            subTags.get(i).index = i;
        }
    }

    /// Returns `true` if this tag is the root tag, `false` otherwise.
    public final boolean isRoot() {
        return parent == null;
    }

    /// Returns `true` if this tag has no subtags, `false` otherwise.
    public final boolean isEmpty() {
        return subTags.isEmpty();
    }

    /// Returns the number of subtags in this tag.
    public final int size() {
        return subTags.size();
    }

    /// Returns the subtag at the given index.
    ///
    /// @throws IndexOutOfBoundsException if the index is out of bounds.
    public final E get(int index) throws IndexOutOfBoundsException {
        return subTags.get(index);
    }

    /// Adds the `tag` to this tag.
    ///
    /// If the `tag` is already a child of this tag, does nothing.
    ///
    /// If the `tag` is already a child of another tag, removes it from old parent and adds it to this tag.
    public abstract void add(E tag) throws IllegalArgumentException;

    /// Removes the `tag` from this tag.
    ///
    /// @throws IllegalArgumentException if the `tag` is not a child of this tag.
    public abstract void remove(Tag tag) throws IllegalArgumentException;

    @Override
    public final Iterator<E> iterator() {
        Iterator<E> iterator = subTags.iterator();

        // Prevent calling Iterator#remove()
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public E next() {
                return iterator.next();
            }
        };
    }

    @Override
    public abstract ParentTag<E> clone();
}
