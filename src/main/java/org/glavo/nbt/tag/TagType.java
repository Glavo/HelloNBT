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

/// @author Glavo
public enum TagType {
    /// Used to mark the end of compound tags.
    /// This tag does not have a name, so it is always a single byte 0.
    /// It may also be the type of empty List tags.
    END(0x00),

    /// 1 byte signed integer type. Sometimes used for booleans.
    BYTE(0x01),

    /// 2 byte signed integer type.
    SHORT(0x02),

    /// 4 byte signed integer type.
    INT(0x03),

    /// 8 byte signed integer type.
    LONG(0x04),

    /// 4 byte floating point type.
    FLOAT(0x05),

    /// 8 byte floating point type.
    DOUBLE(0x06),

    /// An array of bytes.
    BYTE_ARRAY(0x07),

    /// A UTF-8 encoded string. It has a size, rather than being null terminated.
    STRING(0x08),

    /// A list of tag payloads, without tag IDs or names, apart from the one before the length.
    LIST(0x09),

    /// A list of fully formed tags, including their IDs, names, and payloads. No two tags may have the same name.
    COMPOUND(0x0A),

    /// An array of 4 byte signed integers.
    INT_ARRAY(0x0B),

    /// An array of 8 byte signed integers.
    LONG_ARRAY(0x0C);

    private final byte id;

    TagType(int id) {
        this.id = (byte) id;
    }

    public byte getId() {
        return id;
    }
}
