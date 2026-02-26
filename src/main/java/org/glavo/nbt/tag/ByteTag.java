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

/// 1 byte signed integer tag type. Sometimes used for booleans.
public final class ByteTag extends Tag {
    private byte value;

    /// Returns the value of the tag.
    public byte get() {
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

    /// Sets the value of the tag.
    public void set(boolean value) {
        this.value = (byte) (value ? 1 : 0);
    }

    @Override
    public TagType getType() {
        return TagType.BYTE;
    }
}
