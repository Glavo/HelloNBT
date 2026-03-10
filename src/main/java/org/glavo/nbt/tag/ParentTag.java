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
import org.glavo.nbt.internal.ArrayUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Stream;

/// Base class for tags that can contain other tags as children.
public sealed abstract class ParentTag<T extends Tag> extends Tag
        implements NBTParent<T>, Iterable<T>
        permits CompoundTag, ListTag {

    private final Tag[] EMPTY_TAGS = new Tag[0];

    @UnknownNullability
    Tag[] tags = EMPTY_TAGS;
    int size;

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

    protected final void ensureCapacityForAdd() {
        if (size >= tags.length) {
            tags = Arrays.copyOf(tags, ArrayUtils.nextCapacity(size));
        }
    }

    /// Removes the tag at the given index from the array, and decreases the size.
    ///
    /// @return The old tag at the given index.
    protected final Tag removeTagFromArray(int index) {
        assert index >= 0 && index < size;

        Tag oldTag = tags[index];

        if (index < size - 1) {
            System.arraycopy(tags, index + 1, tags, index, size - index);
        } else {
            tags[index] = null;
        }
        size--;

        return oldTag;
    }

    /// Updates the indexes of the subtags starting from the given index.
    protected final void updateIndexes(int startIndex) {
        for (int i = startIndex; i < size; i++) {
            Tag subTag = tags[i];
            if (subTag != null) {
                subTag.setIndex(i);
            }
        }
    }

    /// Returns `true` if this tag has no subtags, `false` otherwise.
    @Override
    public final boolean isEmpty() {
        return size == 0;
    }

    /// Returns the number of subtags in this tag.
    @Override
    public final int size() {
        return size;
    }

    /// Returns the subtag at the given index.
    ///
    /// @throws IndexOutOfBoundsException if the index is out of bounds.
    public final T getTag(int index) throws IndexOutOfBoundsException {
        Objects.checkIndex(index, size);
        @SuppressWarnings("unchecked")
        T tag = (T) tags[index];
        return tag;
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
        for (int i = 0; i < size; i++) {
            Tag subTag = tags[i];
            if (subTag != null) {
                subTag.setParent(null, -1);
            }
        }

        tags = EMPTY_TAGS;
    }

    @Override
    public Iterator<T> iterator() {
        if (size == 0) {
            return Collections.emptyIterator();
        }

        return new Iterator<>() {
            private int cursor = 0;

            @Override
            public boolean hasNext() {
                return cursor < size;
            }

            @Override
            public T next() {
                if (cursor >= size) {
                    throw new NoSuchElementException();
                }
                return getTag(cursor++);
            }
        };
    }

    /// Returns a stream of subtags.
    @Override
    public Stream<T> stream() {
        @SuppressWarnings("unchecked")
        var result = (Stream<T>) Arrays.stream(tags, 0, size);
        return result;
    }

    @Override
    public abstract ParentTag<T> clone();
}
