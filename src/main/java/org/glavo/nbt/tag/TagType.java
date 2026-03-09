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

import java.util.List;
import java.util.function.Supplier;

public final class TagType<T extends Tag> {
    /// 1 byte signed integer type. Sometimes used for booleans.
    public static final TagType<ByteTag> BYTE = new TagType<>("TAG_Byte", (byte) 0x01, ByteTag.class, ByteTag::new);

    /// 2 byte signed integer type.
    public static final TagType<ShortTag> SHORT = new TagType<>("TAG_Short", (byte) 0x02, ShortTag.class, ShortTag::new);

    /// 4 byte signed integer type.
    public static final TagType<IntTag> INT = new TagType<>("TAG_Int", (byte) 0x03, IntTag.class, IntTag::new);

    /// 8 byte signed integer type.
    public static final TagType<LongTag> LONG = new TagType<>("TAG_Long", (byte) 0x04, LongTag.class, LongTag::new);

    /// 4 byte floating point type.
    public static final TagType<FloatTag> FLOAT = new TagType<>("TAG_Float", (byte) 0x05, FloatTag.class, FloatTag::new);

    /// 8 byte floating point type.
    public static final TagType<DoubleTag> DOUBLE = new TagType<>("TAG_Double", (byte) 0x06, DoubleTag.class, DoubleTag::new);

    /// An array of bytes.
    public static final TagType<ByteArrayTag> BYTE_ARRAY = new TagType<>("TAG_Byte_Array", (byte) 0x07, ByteArrayTag.class, ByteArrayTag::new);

    /// A UTF-8 encoded string. It has a size, rather than being null terminated.
    public static final TagType<StringTag> STRING = new TagType<>("TAG_String", (byte) 0x08, StringTag.class, StringTag::new);

    /// A list of tag payloads, without tag IDs or names, apart from the one before the length.
    public static final TagType<ListTag<?>> LIST = new TagType<>("TAG_List", (byte) 0x09, ListTag.class, () -> new ListTag<>((TagType<?>) null));

    /// A list of fully formed tags, including their IDs, names, and payloads. No two tags may have the same name.
    public static final TagType<CompoundTag> COMPOUND = new TagType<>("TAG_Compound", (byte) 0x0A, CompoundTag.class, CompoundTag::new);

    /// An array of 4 byte signed integers.
    public static final TagType<IntArrayTag> INT_ARRAY = new TagType<>("TAG_Int_Array", (byte) 0x0B, IntArrayTag.class, IntArrayTag::new);

    /// An array of 8 byte signed integers.
    public static final TagType<LongArrayTag> LONG_ARRAY = new TagType<>("TAG_Long_Array", (byte) 0x0C, LongArrayTag.class, LongArrayTag::new);

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
    private final Supplier<T> tagSupplier;

    @SuppressWarnings("unchecked")
    private TagType(String name, byte id, Class<?> tagClass, Supplier<T> tagSupplier) {
        this.name = name;
        this.id = id;
        this.tagClass = (Class<T>) tagClass;
        this.tagSupplier = tagSupplier;
    }

    public String name() {
        return name;
    }

    public String getFullName() {
        return name;
    }

    public byte id() {
        return id;
    }

    public Class<T> tagClass() {
        return tagClass;
    }

    public T createTag() {
        return tagSupplier.get();
    }

    @Override
    public String toString() {
        return name;
    }
}
