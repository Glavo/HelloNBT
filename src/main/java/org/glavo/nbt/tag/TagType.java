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

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/// @author Glavo
public enum TagType {
    // /// Used to mark the end of compound tags.
    // /// This tag does not have a name, so it is always a single byte 0.
    // /// It may also be the type of empty List tags.
    // END(0x00, null),

    /// 1 byte signed integer type. Sometimes used for booleans.
    BYTE(0x01, ByteTag.class),

    /// 2 byte signed integer type.
    SHORT(0x02, ShortTag.class),

    /// 4 byte signed integer type.
    INT(0x03, IntTag.class),

    /// 8 byte signed integer type.
    LONG(0x04, LongTag.class),

    /// 4 byte floating point type.
    FLOAT(0x05, FloatTag.class),

    /// 8 byte floating point type.
    DOUBLE(0x06, DoubleTag.class),

    /// An array of bytes.
    BYTE_ARRAY(0x07, ByteArrayTag.class),

    /// A UTF-8 encoded string. It has a size, rather than being null terminated.
    STRING(0x08, StringTag.class),

    /// A list of tag payloads, without tag IDs or names, apart from the one before the length.
    LIST(0x09, ListTag.class),

    /// A list of fully formed tags, including their IDs, names, and payloads. No two tags may have the same name.
    COMPOUND(0x0A, CompoundTag.class),

    /// An array of 4 byte signed integers.
    INT_ARRAY(0x0B, IntArrayTag.class),

    /// An array of 8 byte signed integers.
    LONG_ARRAY(0x0C, LongArrayTag.class);

    private static final TagType[] ID_TO_TYPE;
    private static final Map<Class<? extends Tag>, TagType> CLASS_TO_TYPE;

    static {
        TagType[] values = values();

        TagType[] idToType = new TagType[values.length + 1];
        for (TagType type : values) {
            idToType[Byte.toUnsignedInt(type.id)] = type;
        }
        ID_TO_TYPE = idToType;

        var map = new HashMap<Class<? extends Tag>, TagType>();
        for (TagType type : values) {
            map.put(type.tagClass, type);
        }
        CLASS_TO_TYPE = Map.copyOf(map);
    }

    /// Returns the tag type by its id; returns `null` if the id is invalid.
    public static @Nullable TagType getById(byte id) {
        return id > 0 && id < ID_TO_TYPE.length ? ID_TO_TYPE[id] : null;
    }

    /// Returns the tag type by its class; returns `null` if the class is not found.
    ///
    /// @apiNote Only the final subclasses of `Tag` define `TagType`.
    public static @Nullable TagType getByClass(Class<? extends Tag> tagClass) {
        return CLASS_TO_TYPE.get(tagClass);
    }

    private final byte id;
    private final Class<? extends Tag> tagClass;

    TagType(int id, Class<? extends Tag> tagClass) {
        this.id = (byte) id;
        this.tagClass = tagClass;
    }

    public Tag createTag() {
        return switch (this) {
            case BYTE -> new ByteTag();
            case SHORT -> new ShortTag();
            case INT -> new IntTag();
            case LONG -> new LongTag();
            case FLOAT -> new FloatTag();
            case DOUBLE -> new DoubleTag();
            case STRING -> new StringTag();
            case BYTE_ARRAY -> new ByteArrayTag();
            case INT_ARRAY -> new IntArrayTag();
            case LONG_ARRAY -> new LongArrayTag();
            case LIST -> new ListTag<>((TagType) null);
            case COMPOUND -> new CompoundTag();
        };
    }

    /// Returns the tag id.
    public byte id() {
        return id;
    }
}
