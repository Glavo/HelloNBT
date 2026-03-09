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

/// 1 byte signed integer tag type. Sometimes used for booleans.
public final class ByteTag extends ValueTag<Byte> {
    private byte value;

    /// Creates a new ByteTag with an empty name and a value of `0`.
    public ByteTag() {
        this("", (byte) 0);
    }

    /// Creates a new ByteTag with the given name and a value of `0`.
    public ByteTag(String name) {
        this(name, (byte) 0);
    }

    /// Creates a new ByteTag with the given name and value.
    public ByteTag(String name, byte value) {
        super(name);
        this.value = value;
    }

    /// Creates a new ByteTag with the given name and boolean value.
    public ByteTag(String name, boolean value) {
        super(name);
        this.value = (byte) (value ? 1 : 0);
    }

    @Override
    @Contract(pure = true)
    public TagType getType() {
        return TagType.BYTE;
    }

    /// Returns the value of the tag.
    @Contract(pure = true)
    public byte get() {
        return value;
    }

    /// Returns the value of the tag converted to an unsigned integer.
    @Contract(pure = true)
    public int getUnsigned() {
        return Byte.toUnsignedInt(value);
    }

    /// Returns the value of the tag as a boolean.
    @Contract(pure = true)
    public boolean getBoolean() {
        // Should stricter checks be performed?
        return value != 0;
    }

    @Override
    @Contract(pure = true)
    public Byte getValue() {
        return value;
    }

    @Override
    @Contract(pure = true)
    public String getAsString() {
        return Byte.toString(value);
    }

    /// Sets the value of the tag.
    @Contract(mutates = "this")
    public void set(byte value) {
        this.value = value;
    }

    /// Sets the value of the tag from an unsigned integer.
    @Contract(mutates = "this")
    public void setUnsigned(int value) {
        this.value = (byte) value;
    }

    @Override
    @Contract(mutates = "this")
    public void setValue(Byte value) {
        this.value = value;
    }

    /// Sets the boolean value of the tag.
    ///
    /// If the `value` is `true`, the tag will be set to `1`; otherwise, it will be set to `0`.
    @Contract(mutates = "this")
    public void setBoolean(boolean value) {
        this.value = (byte) (value ? 1 : 0);
    }

    @Override
    protected void readContent(DataReader reader) throws IOException {
        set(reader.readByte());
    }

    @Override
    protected void writeContent(DataWriter writer) throws IOException {
        writer.writeByte(value);
    }

    @Override
    protected int contentHashCode() {
        return Byte.hashCode(value);
    }

    @Override
    public boolean contentEquals(Tag other) {
        return other instanceof ByteTag that && value == that.value;
    }

    @Override
    protected void contentToString(StringBuilder builder) {
        builder.append(value);
    }

    @Override
    @Contract(value = "-> new", pure = true)
    public ByteTag clone() {
        return new ByteTag(name, value);
    }
}
