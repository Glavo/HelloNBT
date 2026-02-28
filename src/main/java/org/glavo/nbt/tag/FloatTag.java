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

/// 4 byte floating point tag type.
public final class FloatTag extends ValueTag<Float> {
    private float value;

    public FloatTag() {
        this("", 0.0f);
    }

    public FloatTag(String name) {
        this(name, 0.0f);
    }

    public FloatTag(String name, float value) {
        super(name);
        this.value = value;
    }

    @Override
    public TagType getType() {
        return TagType.FLOAT;
    }

    /// Returns the value of the tag.
    public float get() {
        return value;
    }

    @Override
    public Float getValue() {
        return value;
    }

    /// Sets the value of the tag.
    public void set(float value) {
        this.value = value;
    }

    @Override
    public void setValue(Float value) {
        this.value = value;
    }

    @Override
    protected void readContent(NBTReader reader) throws IOException {
        set(reader.readFloat());
    }

    @Override
    protected int contentHashCode() {
        return Float.hashCode(value);
    }

    @Override
    protected boolean contentEquals(Tag other) {
        return other instanceof FloatTag that && Float.floatToIntBits(value) == Float.floatToIntBits(that.value);
    }

    @Override
    protected void contentToString(StringBuilder builder) {
        builder.append(value);
    }

    @Override
    public FloatTag clone() {
        return new FloatTag(getName(), value);
    }
}
