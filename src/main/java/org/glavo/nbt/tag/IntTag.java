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

/// 4 byte signed integer tag type.
public final class IntTag extends ValueTag<Integer> {
    private int value;

    public IntTag() {
        this("", 0);
    }

    public IntTag(String name) {
        this(name, 0);
    }

    public IntTag(String name, int value) {
        super(name);
        this.value = value;
    }

    @Override
    @Contract(pure = true)
    public TagType getType() {
        return TagType.INT;
    }

    /// Returns the value of the tag.
    @Contract(pure = true)
    public int get() {
        return value;
    }

    @Override
    @Contract(pure = true)
    public Integer getValue() {
        return value;
    }

    /// Sets the value of the tag.
    @Contract(mutates = "this")
    public void set(int value) {
        this.value = value;
    }

    @Override
    @Contract(mutates = "this")
    public void setValue(Integer value) {
        this.value = value;
    }

    @Override
    protected void readContent(DataReader reader) throws IOException {
        set(reader.readInt());
    }

    @Override
    protected void writeContent(DataWriter writer) throws IOException {
        writer.writeInt(value);
    }

    @Override
    protected int contentHashCode() {
        return Integer.hashCode(value);
    }

    @Override
    protected boolean contentEquals(Tag other) {
        return other instanceof IntTag that && value == that.value;
    }

    @Override
    protected void contentToString(StringBuilder builder) {
        builder.append(value);
    }

    @Override
    @Contract(value = "-> new", pure = true)
    public IntTag clone() {
        return new IntTag(name, value);
    }
}
