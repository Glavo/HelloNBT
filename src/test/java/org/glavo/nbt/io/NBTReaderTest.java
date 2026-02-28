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
package org.glavo.nbt.io;

import com.github.steveice10.opennbt.NBTIO;
import org.glavo.nbt.internal.io.NBTReader;
import org.glavo.nbt.tag.*;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class NBTReaderTest {

    private static Tag convert(
            com.github.steveice10.opennbt.tag.builtin.Tag tag,
            ByteOrder byteOrder)
            throws IOException {
        var buffer = new ByteArrayOutputStream();
        NBTIO.writeTag(buffer, tag, byteOrder == ByteOrder.LITTLE_ENDIAN);

        return Tag.readTag(new ByteArrayInputStream(buffer.toByteArray()), byteOrder);
    }

    private static void assertTagEquals(
            Tag expected,
            com.github.steveice10.opennbt.tag.builtin.Tag actual) throws IOException {
        assertEquals(expected, convert(actual, ByteOrder.BIG_ENDIAN));
        assertEquals(expected, convert(actual, ByteOrder.LITTLE_ENDIAN));
    }

    @Test
    public void testReadSimpleTag() throws IOException {
        assertTagEquals(new ByteTag("Meow", (byte) 42), new com.github.steveice10.opennbt.tag.builtin.ByteTag("Meow", (byte) 42));
        assertTagEquals(new ShortTag("Meow", (short) 42), new com.github.steveice10.opennbt.tag.builtin.ShortTag("Meow", (short) 42));
        assertTagEquals(new IntTag("Meow", 42), new com.github.steveice10.opennbt.tag.builtin.IntTag("Meow", 42));
        assertTagEquals(new LongTag("Meow", 42L), new com.github.steveice10.opennbt.tag.builtin.LongTag("Meow", 42L));
        assertTagEquals(new FloatTag("Meow", 42.0f), new com.github.steveice10.opennbt.tag.builtin.FloatTag("Meow", 42.0f));
        assertTagEquals(new DoubleTag("Meow", 42.0), new com.github.steveice10.opennbt.tag.builtin.DoubleTag("Meow", 42.0));
        assertTagEquals(new StringTag("Meow", "Glavo"), new com.github.steveice10.opennbt.tag.builtin.StringTag("Meow", "Glavo"));
        assertTagEquals(new ByteArrayTag("Meow", new byte[]{1, 2, 3}), new com.github.steveice10.opennbt.tag.builtin.ByteArrayTag("Meow", new byte[]{1, 2, 3}));
        assertTagEquals(new IntArrayTag("Meow", new int[]{1, 2, 3}), new com.github.steveice10.opennbt.tag.builtin.IntArrayTag("Meow", new int[]{1, 2, 3}));
        assertTagEquals(new LongArrayTag("Meow", new long[]{1, 2, 3}), new com.github.steveice10.opennbt.tag.builtin.LongArrayTag("Meow", new long[]{1, 2, 3}));

        {
            var expected = new ListTag<>("Meow", IntTag.class);
            var actual = new com.github.steveice10.opennbt.tag.builtin.ListTag("Meow", com.github.steveice10.opennbt.tag.builtin.IntTag.class);

            for (int i = 0; i < 10000; i++) {
                expected.add(new IntTag("", i));
                actual.add(new com.github.steveice10.opennbt.tag.builtin.IntTag("", i));
            }

            assertTagEquals(expected, actual);
        }

        {
            var expected = new CompoundTag<>("Meow");
            expected.add(new ByteTag("Sub0", (byte) 42));
            expected.add(new ShortTag("Sub1", (short) 42));
            expected.add(new IntTag("Sub2", 42));
            expected.add(new LongTag("Sub3", 42L));
            expected.add(new FloatTag("Sub4", 42.0f));
            expected.add(new DoubleTag("Sub5", 42.0));
            expected.add(new StringTag("Sub6", "Glavo"));
            expected.add(new ByteArrayTag("Sub7", new byte[]{1, 2, 3}));
            expected.add(new IntArrayTag("Sub8", new int[]{1, 2, 3}));
            expected.add(new LongArrayTag("Sub9", new long[]{1, 2, 3}));
            {
                var sub10 = new CompoundTag<>("Sub10");
                sub10.add(new ByteTag("Sub10Sub0", (byte) 42));
                expected.add(sub10);
            }
            {
                var sub11 = new ListTag<>("Sub11", IntTag.class);
                for (int i = 0; i < 10000; i++) {
                    sub11.add(new IntTag("", i));
                }
                expected.add(sub11);
            }

            var actual = new com.github.steveice10.opennbt.tag.builtin.CompoundTag("Meow");
            actual.put(new com.github.steveice10.opennbt.tag.builtin.ByteTag("Sub0", (byte) 42));
            actual.put(new com.github.steveice10.opennbt.tag.builtin.ShortTag("Sub1", (short) 42));
            actual.put(new com.github.steveice10.opennbt.tag.builtin.IntTag("Sub2", 42));
            actual.put(new com.github.steveice10.opennbt.tag.builtin.LongTag("Sub3", 42L));
            actual.put(new com.github.steveice10.opennbt.tag.builtin.FloatTag("Sub4", 42.0f));
            actual.put(new com.github.steveice10.opennbt.tag.builtin.DoubleTag("Sub5", 42.0));
            actual.put(new com.github.steveice10.opennbt.tag.builtin.StringTag("Sub6", "Glavo"));
            actual.put(new com.github.steveice10.opennbt.tag.builtin.ByteArrayTag("Sub7", new byte[]{1, 2, 3}));
            actual.put(new com.github.steveice10.opennbt.tag.builtin.IntArrayTag("Sub8", new int[]{1, 2, 3}));
            actual.put(new com.github.steveice10.opennbt.tag.builtin.LongArrayTag("Sub9", new long[]{1, 2, 3}));
            {
                var sub10 = new com.github.steveice10.opennbt.tag.builtin.CompoundTag("Sub10");
                sub10.put(new com.github.steveice10.opennbt.tag.builtin.ByteTag("Sub10Sub0", (byte) 42));
                actual.put(sub10);
            }
            {
                var sub11 = new com.github.steveice10.opennbt.tag.builtin.ListTag("Sub11", com.github.steveice10.opennbt.tag.builtin.IntTag.class);
                for (int i = 0; i < 10000; i++) {
                    sub11.add(new com.github.steveice10.opennbt.tag.builtin.IntTag("", i));
                }
                actual.put(sub11);
            }

            assertTagEquals(expected, actual);
        }
    }

    @Test
    public void testReadModifiedUTF8String() throws IOException {
        String value = "ABCǾ喵喵喵🐱ABC123";

        var expected = new StringTag("Meow", value);
        var actual = new com.github.steveice10.opennbt.tag.builtin.StringTag("Meow", value);
        assertTagEquals(expected, actual);
    }
}
