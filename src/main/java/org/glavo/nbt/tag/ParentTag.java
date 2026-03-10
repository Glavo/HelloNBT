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
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Stream;

/// Base class for tags that can contain other tags as children.
public sealed abstract class ParentTag<T extends Tag> extends Tag
        implements NBTParent<T>, Iterable<T>
        permits CompoundTag, ListTag {

    final ArrayList<T> subTags = new ArrayList<>();

    protected ParentTag(String name) {
        super(name);
    }

    @Override
    public abstract TagType<? extends ParentTag<?>> getType();

    /// Prepares to update the name of the given subtag.
    ///
    /// Used internally by [Tag#setName(String)].
    ///
    /// @see Tag#setName(String)
    abstract void preUpdateSubTagName(Tag tag, String oldName, String newName) throws IllegalArgumentException;

    /// Updates the indexes of the subtags starting from the given index.
    protected final void updateIndexes(int startIndex) {
        for (int i = startIndex, end = subTags.size(); i < end; i++) {
            subTags.get(i).setIndex(i);
        }
    }

    /// Returns `true` if this tag has no subtags, `false` otherwise.
    @Override
    public final boolean isEmpty() {
        return subTags.isEmpty();
    }

    /// Returns the number of subtags in this tag.
    @Override
    public final int size() {
        return subTags.size();
    }

    /// Returns the subtag at the given index.
    ///
    /// @throws IndexOutOfBoundsException if the index is out of bounds.
    public final T getTag(int index) throws IndexOutOfBoundsException {
        return subTags.get(index);
    }

    /// Adds the `tag` to this tag.
    ///
    /// If the `tag` is already a child of this tag, move it to the end of the list.
    ///
    /// If the `tag` is already a child of another tag, removes it from old parent and adds it to this tag.
    public abstract void addTag(T tag) throws IllegalArgumentException;

    /// Adds all `tags` to this tag.
    ///
    /// @see #addTag(Tag)
    public final void addTags(Iterable<? extends T> tags) throws IllegalArgumentException {
        for (T tag : tags) {
            this.addTag(tag);
        }
    }

    /// Adds all `tags` to this tag.
    ///
    /// @see #addTag(Tag)
    @SafeVarargs
    public final void addTags(T... tags) throws IllegalArgumentException {
        for (T tag : tags) {
            this.addTag(tag);
        }
    }

    /// Removes the `tag` from this tag.
    ///
    /// @throws IllegalArgumentException if the `tag` is not a child of this tag.
    public abstract void removeTag(Tag tag) throws IllegalArgumentException;

    /// @see #removeTag(Tag)
    @ApiStatus.Obsolete
    @Override
    public final void removeElement(Tag tag) throws IllegalArgumentException {
        removeTag(tag);
    }

    /// Removes all subtags from this tag.
    public void clear() {
        for (T subTag : subTags) {
            // Clear the parent and index of the subtag.
            subTag.setParent(null, -1);
        }

        subTags.clear();
    }

    @Override
    public final Iterator<T> iterator() {
        Iterator<T> iterator = subTags.iterator();

        // Prevent calling Iterator#remove()
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public T next() {
                return iterator.next();
            }
        };
    }

    /// Returns a stream of subtags.
    public Stream<T> stream() {
        return subTags.stream();
    }

    @Override
    public abstract ParentTag<T> clone();
}
