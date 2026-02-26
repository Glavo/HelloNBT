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
    /// Used to mark the end of compound tags.
    /// This tag does not have a name, so it is always a single byte 0.
    /// It may also be the type of empty List tags.
    END(null),

    /// 1 byte signed integer type. Sometimes used for booleans.
    BYTE(ByteTag.class),

    /// 2 byte signed integer type.
    SHORT(ShortTag.class),

    /// 4 byte signed integer type.
    INT(IntTag.class),

    /// 8 byte signed integer type.
    LONG(LongTag.class),

    /// 4 byte floating point type.
    FLOAT(FloatTag.class),

    /// 8 byte floating point type.
    DOUBLE(DoubleTag.class),

    /// An array of bytes.
    BYTE_ARRAY(ByteArrayTag.class),

    /// A UTF-8 encoded string. It has a size, rather than being null terminated.
    STRING(StringTag.class),

    /// A list of tag payloads, without tag IDs or names, apart from the one before the length.
    LIST(ListTag.class),

    /// A list of fully formed tags, including their IDs, names, and payloads. No two tags may have the same name.
    COMPOUND(CompoundTag.class),

    /// An array of 4 byte signed integers.
    INT_ARRAY(IntArrayTag.class),

    /// An array of 8 byte signed integers.
    LONG_ARRAY(LongArrayTag.class);

    private static final TagType[] TYPES = values();
    private static final Map<Class<? extends Tag>, TagType> CLASS_TO_TYPE;

    static {
        var map = new HashMap<Class<? extends Tag>, TagType>();
        for (TagType type : TYPES) {
            if (type.tagClass != null) {
                map.put(type.tagClass, type);
            }
        }
        CLASS_TO_TYPE = Map.copyOf(map);
    }

    /// Returns the tag type by its id; returns `null` if the id is invalid.
    public static @Nullable TagType getById(byte id) {
        return id >= 0 && id < TYPES.length ? TYPES[id] : null;
    }

    /// Returns the tag type by its class; returns `null` if the class is not found.
    ///
    /// @apiNote Only the final subclasses of `Tag` define `TagType`.
    public static @Nullable TagType getByClass(Class<? extends Tag> tagClass) {
        return CLASS_TO_TYPE.get(tagClass);
    }

    private final @Nullable Class<? extends Tag> tagClass;

    TagType(@Nullable Class<? extends Tag> tagClass) {
        this.tagClass = tagClass;
    }

    /// Returns the tag id.
    public byte id() {
        return (byte) ordinal();
    }

    /// Creates a new tag of this type with the given name.
    ///
    /// @throws UnsupportedOperationException if this tag type is `END`.
    public Tag createTag(String name) {
        return switch (this) {
            case END -> throw new UnsupportedOperationException("Cannot create an END tag");
            case BYTE -> new ByteTag(name);
            case SHORT -> new ShortTag(name);
            case INT -> new IntTag(name);
            case LONG -> new LongTag(name);
            case FLOAT -> new FloatTag(name);
            case DOUBLE -> new DoubleTag(name);
            case STRING -> new StringTag(name);
            case BYTE_ARRAY -> new ByteArrayTag(name);
            case INT_ARRAY -> new IntArrayTag(name);
            case LONG_ARRAY -> new LongArrayTag(name);
            case LIST -> new ListTag<>(name);
            case COMPOUND -> new CompoundTag<>(name);
        };
    }
}
