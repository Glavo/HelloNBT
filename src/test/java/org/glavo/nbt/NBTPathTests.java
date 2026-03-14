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
package org.glavo.nbt;

import org.glavo.nbt.io.NBTCodec;
import org.glavo.nbt.tag.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class NBTPathTests {

    private static <T> T with(T value, Consumer<T> consumer) {
        consumer.accept(value);
        return value;
    }

    private static CompoundTag createSampleRoot() {
        return with(new CompoundTag(), root -> {
            root.put("player", with(new CompoundTag(), player -> {
                player.setString("name", "Alex");
                player.setInt("score", 42);
            }));

            root.put("players", with(new ListTag<>(TagType.COMPOUND), players -> {
                players.addTag(with(new CompoundTag(), player -> {
                    player.setString("name", "Alex");
                    player.setInt("score", 10);
                }));
                players.addTag(with(new CompoundTag(), player -> {
                    player.setString("name", "Steve");
                    player.setInt("score", 20);
                }));
            }));

            root.setIntArray("numbers", new int[]{3, 5, 8});
            root.setLongArray("longs", new long[]{13L, 21L});
        });
    }

    @Test
    void testLevelDat() throws Exception {
        CompoundTag levelDat = NBTCodec.of().readTag(
                TestResources.getResource("/assets/nbt/level.dat"),
                TagType.COMPOUND
        );

        var path = NBTPath.of("Data.Version.Name").withTagType(TagType.STRING);
        assertEquals("1.21.11", levelDat.getFirstString(path));
    }

    @Test
    void testListSelection() {
        CompoundTag root = createSampleRoot();

        assertEquals("Alex", root.getFirstString(NBTPath.of("players[0].name").withTagType(TagType.STRING)));
        assertEquals("Steve", root.getFirstString(NBTPath.of("players[-1].name").withTagType(TagType.STRING)));
        assertNull(root.getFirstStringOrNull(NBTPath.of("players[2].name").withTagType(TagType.STRING)));

        assertIterableEquals(List.of("Alex", "Steve"),
                root.getAllTags(NBTPath.of("players[].name").withTagType(TagType.STRING))
                        .map(StringTag::getValue)
                        .toList());
    }

    @Test
    void testArraySelection() {
        CompoundTag root = createSampleRoot();

        assertEquals(5, root.getFirstInt(NBTPath.of("numbers[1]").withTagType(TagType.INT)));
        assertEquals(8, root.getFirstInt(NBTPath.of("numbers[-1]").withTagType(TagType.INT)));

        assertIterableEquals(List.of(3, 5, 8),
                root.getAllTags(NBTPath.of("numbers[]").withTagType(TagType.INT))
                        .map(IntTag::getValue)
                        .toList());
    }

    @Test
    void testWithTagTypeBehavior() {
        CompoundTag root = createSampleRoot();

        NBTPath<?> untypedPath = NBTPath.of("player.name");
        assertNull(untypedPath.getTagType());

        var stringPath = untypedPath.withTagType(TagType.STRING);
        assertEquals(TagType.STRING, stringPath.getTagType());
        assertEquals("Alex", root.getFirstString(stringPath));
        assertNull(root.getFirstTagOrNull(untypedPath.withTagType(TagType.INT)));

        assertThrows(IllegalStateException.class, () -> NBTPath.of("{}").withTagType(TagType.STRING));
    }

    @Test
    void testInvalidPathSyntax() {
        assertThrows(IllegalArgumentException.class, () -> NBTPath.of(""));
        assertThrows(IllegalArgumentException.class, () -> NBTPath.of("player."));
        assertThrows(IllegalArgumentException.class, () -> NBTPath.of("numbers[abc]"));
        assertThrows(IllegalArgumentException.class, () -> NBTPath.of("[2147483648]"));
    }
}
