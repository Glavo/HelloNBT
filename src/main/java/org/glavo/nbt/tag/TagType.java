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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/// This class represents the type of [tag][Tag] in NBT (Named Binary Tag) format.
public abstract class TagType<T extends Tag> {
    /// 1 byte signed integer type. Sometimes used for booleans.
    ///
    /// @see ByteTag
    public static final TagType<ByteTag> BYTE = new TagType<>("TAG_Byte", 0x01, ByteTag.class) {
        @Override
        public ByteTag createTag() {
            return new ByteTag();
        }
    };

    /// 2 byte signed integer type.
    ///
    /// @see ShortTag
    public static final TagType<ShortTag> SHORT = new TagType<>("TAG_Short", 0x02, ShortTag.class) {
        @Override
        public ShortTag createTag() {
            return new ShortTag();
        }
    };

    /// 4 byte signed integer type.
    ///
    /// @see IntTag
    public static final TagType<IntTag> INT = new TagType<>("TAG_Int", 0x03, IntTag.class) {
        @Override
        public IntTag createTag() {
            return new IntTag();
        }
    };

    /// 8 byte signed integer type.
    ///
    /// @see LongTag
    public static final TagType<LongTag> LONG = new TagType<>("TAG_Long", 0x04, LongTag.class) {
        @Override
        public LongTag createTag() {
            return new LongTag();
        }
    };

    /// 4 byte floating point type.
    ///
    /// @see FloatTag
    public static final TagType<FloatTag> FLOAT = new TagType<>("TAG_Float", 0x05, FloatTag.class) {
        @Override
        public FloatTag createTag() {
            return new FloatTag();
        }
    };

    /// 8 byte floating point type.
    ///
    /// @see DoubleTag
    public static final TagType<DoubleTag> DOUBLE = new TagType<>("TAG_Double", 0x06, DoubleTag.class) {
        @Override
        public DoubleTag createTag() {
            return new DoubleTag();
        }
    };

    /// An array of bytes.
    ///
    /// @see ByteArrayTag
    public static final TagType<ByteArrayTag> BYTE_ARRAY = new TagType<>("TAG_Byte_Array", 0x07, ByteArrayTag.class) {
        @Override
        public ByteArrayTag createTag() {
            return new ByteArrayTag();
        }
    };

    /// A UTF-8 encoded string. It has a size, rather than being null terminated.
    ///
    /// @see StringTag
    public static final TagType<StringTag> STRING = new TagType<>("TAG_String", 0x08, StringTag.class) {
        @Override
        public StringTag createTag() {
            return new StringTag();
        }
    };

    /// A list of tag payloads, without tag IDs or names, apart from the one before the length.
    ///
    /// @see ListTag
    public static final TagType<ListTag<?>> LIST = new TagType<>("TAG_List", 0x09, ListTag.class) {
        @Override
        public ListTag<?> createTag() {
            return new ListTag<>();
        }
    };

    /// A list of fully formed tags, including their IDs, names, and payloads. No two tags may have the same name.
    ///
    /// @see CompoundTag
    public static final TagType<CompoundTag> COMPOUND = new TagType<>("TAG_Compound", 0x0A, CompoundTag.class) {
        @Override
        public CompoundTag createTag() {
            return new CompoundTag();
        }
    };

    /// An array of 4 byte signed integers.
    ///
    /// @see IntArrayTag
    public static final TagType<IntArrayTag> INT_ARRAY = new TagType<>("TAG_Int_Array", 0x0B, IntArrayTag.class) {
        @Override
        public IntArrayTag createTag() {
            return new IntArrayTag();
        }
    };

    /// An array of 8 byte signed integers.
    ///
    /// @see LongArrayTag
    public static final TagType<LongArrayTag> LONG_ARRAY = new TagType<>("TAG_Long_Array", 0x0C, LongArrayTag.class) {
        @Override
        public LongArrayTag createTag() {
            return new LongArrayTag();
        }
    };

    /// Returns the tag type by its id; returns `null` if the id is invalid.
    public static @Nullable TagType<?> getById(byte id) {
        return id > 0 && id < ID_TO_TYPE.length ? ID_TO_TYPE[id] : null;
    }

    private static final List<TagType<?>> VALUES;
    private static final @Nullable TagType<?>[] ID_TO_TYPE;

    static {
        VALUES = List.of(BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, BYTE_ARRAY, STRING, LIST, COMPOUND, INT_ARRAY, LONG_ARRAY);

        ID_TO_TYPE = new TagType<?>[VALUES.size() + 1];

        for (int i = 0; i < VALUES.size(); i++) {
            TagType<?> type = VALUES.get(i);

            assert i == type.id() - 1 : "Invalid order: " + type.name();
            assert ID_TO_TYPE[Byte.toUnsignedInt(type.id())] == null : "Duplicate ID: " + type.id();

            ID_TO_TYPE[Byte.toUnsignedInt(type.id())] = type;
        }
    }

    private final String name;
    private final byte id;
    private final Class<T> tagClass;

    @SuppressWarnings("unchecked")
    private TagType(String name, int id, Class<?> tagClass) {
        this.name = name;
        this.id = (byte) id;
        this.tagClass = (Class<T>) tagClass;
    }

    /// Returns the name of the tag type.
    @Contract(pure = true)
    public String name() {
        return name;
    }

    /// Returns the full name of the tag type.
    @Contract(pure = true)
    public String getFullName() {
        return name;
    }

    /// Returns the ID of the tag type.
    @Contract(pure = true)
    public byte id() {
        return id;
    }

    /// Returns the class of the tag type.
    @Contract(pure = true)
    public Class<T> tagClass() {
        return tagClass;
    }

    /// Creates a new tag of this type.
    @Contract("-> new")
    public abstract T createTag();

    /// Creates a new tag of this type with the given name.
    @Contract("_ -> new")
    public T createTag(String name) {
        T tag = createTag();
        tag.setName(name);
        return tag;
    }

    @Override
    public String toString() {
        return name;
    }
}
