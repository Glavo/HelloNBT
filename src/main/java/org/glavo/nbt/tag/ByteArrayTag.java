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

/// An ordered list of 8-bit integers.
public final class ByteArrayTag extends ArrayTag {
    private static final byte[] EMPTY = new byte[0];

    byte[] value = EMPTY;

    public ByteArrayTag() {
    }

    public ByteArrayTag(String name) {
        this.name = name;
    }

    public ByteArrayTag(String name, byte[] value) {
        this.name = name;
        this.value = value.clone();
    }

    /// Returns the value of the tag.
    public byte[] get() {
        return value.clone();
    }

    /// Sets the value of the tag.
    public void set(byte[] value) {
        this.value = value.clone();
    }

    /// Returns the element at the given index.
    ///
    /// @throws IndexOutOfBoundsException if the index is out of bounds.
    public byte get(int index) throws IndexOutOfBoundsException {
        return value[index];
    }

    /// Sets the element at the given index.
    ///
    /// @throws IndexOutOfBoundsException if the index is out of bounds.
    public void set(int index, byte value) throws IndexOutOfBoundsException {
        this.value[index] = value;
    }

    @Override
    public int size() {
        return value.length;
    }

    @Override
    public TagType getType() {
        return TagType.BYTE_ARRAY;
    }
}
