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

/// 2 byte signed integer tag type.
public final class ShortTag extends ValueTag<Short> {
    private short value;

    /// Creates a new ShortTag with an empty name and a value of `0`.
    public ShortTag() {
        this("", (short) 0);
    }

    /// Creates a new ShortTag with the given name and a value of `0`.
    public ShortTag(String name) {
        this(name, (short) 0);
    }

    /// Creates a new ShortTag with the given name and value.
    public ShortTag(String name, short value) {
        super(name);
        this.value = value;
    }

    @Override
    @Contract(pure = true)
    public TagType getType() {
        return TagType.SHORT;
    }

    /// Returns the value of the tag.
    @Contract(pure = true)
    public short get() {
        return value;
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

    @Override
    @Contract(mutates = "this")
    public void setValue(Short value) {
        this.value = value;
    }

    @Override
    protected void readContent(DataReader reader) throws IOException {
        set(reader.readShort());
    }

    @Override
    protected void writeContent(DataWriter writer) throws IOException {
        writer.writeShort(value);
    }

    @Override
    protected int contentHashCode() {
        return Short.hashCode(value);
    }

    @Override
    protected boolean contentEquals(Tag other) {
        return other instanceof ShortTag that && value == that.value;
    }

    @Override
    protected void contentToString(StringBuilder builder) {
        builder.append(value);
    }

    @Override
    @Contract(value = "-> new", pure = true)
    public ShortTag clone() {
        return new ShortTag(getName(), value);
    }
}
