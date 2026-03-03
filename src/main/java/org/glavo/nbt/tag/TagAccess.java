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

import org.jetbrains.annotations.ApiStatus;

import java.lang.invoke.MethodHandles;

/// Internal access to the tag implementation.
///
/// This class is **NOT** a public API and should not be used directly.
@ApiStatus.Internal
public final class TagAccess {
    private static final TagAccess INSTANCE = new TagAccess();

    /// Get an instance of the internal operations.
    ///
    /// @param lookup A lookup object used to check whether a user has access rights to the [Tag].
    /// @return An instance of the internal operations.
    /// @throws UnsupportedOperationException if the user does not have access rights to the [Tag].
    public static TagAccess getInstance(MethodHandles.Lookup lookup) {
        try {
            MethodHandles.privateLookupIn(Tag.class, lookup);
        } catch (IllegalAccessException e) {
            throw new UnsupportedOperationException(e);
        }
        return INSTANCE;
    }

    private TagAccess() {
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
