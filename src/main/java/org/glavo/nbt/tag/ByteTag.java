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

import org.glavo.nbt.internal.input.NBTReader;

import java.io.IOException;

/// 1 byte signed integer tag type. Sometimes used for booleans.
public final class ByteTag extends ValueTag<Byte> {
    private byte value;

    public ByteTag() {
        this("", (byte) 0);
    }

    public ByteTag(String name) {
        this(name, (byte) 0);
    }

    public ByteTag(String name, byte value) {
        super(name);
        this.value = value;
    }

    @Override
    public TagType getType() {
        return TagType.BYTE;
    }

    /// Returns the value of the tag.
    public byte get() {
        return value;
    }

    @Override
    public Byte getValue() {
        return value;
    }

    /// Returns the value of the tag as a boolean.
    public boolean getBoolean() {
        // Should stricter checks be performed?
        return value != 0;
    }

    /// Sets the value of the tag.
    public void set(byte value) {
        this.value = value;
    }

    @Override
    public void setValue(Byte value) {
        this.value = value;
    }

    /// Sets the boolean value of the tag.
    ///
    /// If the `value` is `true`, the tag will be set to `1`; otherwise, it will be set to `0`.
    public void setBoolean(boolean value) {
        this.value = (byte) (value ? 1 : 0);
    }

    @Override
    protected void readContent(NBTReader reader) throws IOException {
        set(reader.readByte());
    }

    @Override
    protected int contentHashCode() {
        return Byte.hashCode(value);
    }

    @Override
    protected boolean contentEquals(Tag other) {
        return other instanceof ByteTag that && value == that.value;
    }

    @Override
    protected void contentToString(StringBuilder builder) {
        builder.append(value);
    }
}
