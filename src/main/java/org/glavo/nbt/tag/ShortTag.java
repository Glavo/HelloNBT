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

/// 2 byte signed integer tag type.
public final class ShortTag extends Tag {
    private short value;

    public ShortTag() {
        this("", (short) 0);
    }

    public ShortTag(String name) {
        this(name, (short) 0);
    }

    public ShortTag(String name, short value) {
        super(name);
        this.value = value;
    }

    @Override
    public TagType getType() {
        return TagType.SHORT;
    }

    /// Returns the value of the tag.
    public short get() {
        return value;
    }

    /// Sets the value of the tag.
    public void set(short value) {
        this.value = value;
    }

    @Override
    protected int contentHashCode() {
        return Short.hashCode(value);
    }

    @Override
    protected boolean contentEquals(Tag other) {
        return other instanceof ShortTag that && value == that.value;
    }
}
