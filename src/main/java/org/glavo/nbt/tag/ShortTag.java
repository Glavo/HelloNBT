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

import java.io.IOException;

/// A [value tag][ValueTag] that holds a 2 byte integer.
///
/// @see Tag
/// @see ValueTag
public final class ShortTag extends ValueTag<Short> {
    private short value;

    /// Creates a new ShortTag with an empty name and a value of `0`.
    public ShortTag() {
        this((short) 0);
    }

    /// Creates a new ShortTag with the given value.
    public ShortTag(short value) {
        this.value = value;
    }

    /// Creates a new ShortTag with the given name and a value of `0`.
    public ShortTag(String name) {
        this();
        setName(name);
    }

    /// Creates a new ShortTag with the given name and value.
    public ShortTag(String name, short value) {
        this(value);
        setName(name);
    }

    @Override
    @Contract(pure = true)
    public TagType<ShortTag> getType() {
        return TagType.SHORT;
    }

    /// Returns the value of the tag.
    @Contract(pure = true)
    public short get() {
        return value;
    }

    /// Returns the value of the tag converted to an unsigned integer.
    @Contract(pure = true)
    public int getUnsigned() {
        return Short.toUnsignedInt(value);
    }

    @Override
    @Contract(pure = true)
    public Short getValue() {
        return value;
    }

    @Override
    @Contract(pure = true)
    public String getAsString() {
        return Short.toString(value);
    }

    /// Sets the value of the tag.
    @Contract(mutates = "this")
    public void set(short value) {
        this.value = value;
    }

    /// Sets the value of the tag from an unsigned integer.
    @Contract(mutates = "this")
    public void setUnsigned(int value) {
        this.value = (short) value;
    }

    @Override
    @Contract(mutates = "this")
    public void setValue(Short value) {
        this.value = value;
    }

    @Override
    void readContent(DataReader reader) throws IOException {
        set(reader.readShort());
    }

    @Override
    void writeContent(DataWriter writer) throws IOException {
        writer.writeShort(value);
    }

    @Override
    @Contract(pure = true)
    public int contentHashCode() {
        return Short.hashCode(value);
    }

    @Override
    @Contract(pure = true)
    public boolean contentEquals(Tag other) {
        return other instanceof ShortTag that && value == that.value;
    }

    @Override
    @Contract(value = "-> new", pure = true)
    public ShortTag clone() {
        return new ShortTag(getName(), value);
    }
}
