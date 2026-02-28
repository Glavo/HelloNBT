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

/// 8 byte signed integer tag type.
public final class LongTag extends ValueTag<Long> {
    private long value;

    public LongTag() {
        this("", 0L);
    }

    public LongTag(String name) {
        this(name, 0L);
    }

    public LongTag(String name, long value) {
        super(name);
        this.value = value;
    }

    @Override
    public TagType getType() {
        return TagType.LONG;
    }

    /// Returns the value of the tag.
    public long get() {
        return value;
    }

    @Override
    public Long getValue() {
        return value;
    }

    /// Sets the value of the tag.
    public void set(long value) {
        this.value = value;
    }

    @Override
    public void setValue(Long value) {
        this.value = value;
    }

    @Override
    protected void readContent(NBTReader reader) throws IOException {
        set(reader.readLong());
    }

    @Override
    protected int contentHashCode() {
        return Long.hashCode(value);
    }

    @Override
    protected boolean contentEquals(Tag other) {
        return other instanceof LongTag that && value == that.value;
    }

    @Override
    protected void contentToString(StringBuilder builder) {
        builder.append(value);
    }
}
