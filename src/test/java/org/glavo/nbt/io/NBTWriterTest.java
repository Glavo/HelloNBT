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
import org.glavo.nbt.internal.NBTCodecImpl;
import org.glavo.nbt.tag.*;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class NBTWriterTest {
    private static com.github.steveice10.opennbt.tag.builtin.Tag convert(
            Tag tag,
            MinecraftEdition edition)
            throws IOException {

        var codec = NBTCodec.newBuilder().setEdition(edition).build();

        var buffer = new ByteArrayOutputStream();
        codec.writeTag(tag, buffer);
        return NBTIO.readTag(new ByteArrayInputStream(buffer.toByteArray()), edition == MinecraftEdition.BEDROCK_EDITION);
    }

    private static void assertTagEquals(
            com.github.steveice10.opennbt.tag.builtin.Tag expected,
            Tag actual) throws IOException {
        assertEquals(expected, convert(actual, MinecraftEdition.JAVA_EDITION));
        assertEquals(expected, convert(actual, MinecraftEdition.BEDROCK_EDITION));
    }

    @Test
    public void testWriteSimpleTag() throws IOException {
        assertTagEquals(new com.github.steveice10.opennbt.tag.builtin.ByteTag("Meow", (byte) 42), new ByteTag("Meow", (byte) 42));
        assertTagEquals(new com.github.steveice10.opennbt.tag.builtin.ShortTag("Meow", (short) 42), new ShortTag("Meow", (short) 42));
        assertTagEquals(new com.github.steveice10.opennbt.tag.builtin.IntTag("Meow", 42), new IntTag("Meow", 42));
        assertTagEquals(new com.github.steveice10.opennbt.tag.builtin.LongTag("Meow", 42L), new LongTag("Meow", 42L));
        assertTagEquals(new com.github.steveice10.opennbt.tag.builtin.FloatTag("Meow", 42.0f), new FloatTag("Meow", 42.0f));
        assertTagEquals(new com.github.steveice10.opennbt.tag.builtin.DoubleTag("Meow", 42.0), new DoubleTag("Meow", 42.0));
        assertTagEquals(new com.github.steveice10.opennbt.tag.builtin.StringTag("Meow", "Glavo"), new StringTag("Meow", "Glavo"));
        assertTagEquals(new com.github.steveice10.opennbt.tag.builtin.ByteArrayTag("Meow", new byte[]{1, 2, 3}), new ByteArrayTag("Meow", new byte[]{1, 2, 3}));
        assertTagEquals(new com.github.steveice10.opennbt.tag.builtin.IntArrayTag("Meow", new int[]{1, 2, 3}), new IntArrayTag("Meow", new int[]{1, 2, 3}));
        assertTagEquals(new com.github.steveice10.opennbt.tag.builtin.LongArrayTag("Meow", new long[]{1, 2, 3}), new LongArrayTag("Meow", new long[]{1, 2, 3}));

        {
            var expected = new com.github.steveice10.opennbt.tag.builtin.ListTag("Meow", com.github.steveice10.opennbt.tag.builtin.IntTag.class);
            var actual = new ListTag<>("Meow", IntTag.class);

            for (int i = 0; i < 10000; i++) {
                expected.add(new com.github.steveice10.opennbt.tag.builtin.IntTag("", i));
                actual.add(new IntTag("", i));
            }

            assertTagEquals(expected, actual);
        }

        {
            var expected = new com.github.steveice10.opennbt.tag.builtin.CompoundTag("Meow");
            expected.put(new com.github.steveice10.opennbt.tag.builtin.ByteTag("Sub0", (byte) 42));
            expected.put(new com.github.steveice10.opennbt.tag.builtin.ShortTag("Sub1", (short) 42));
            expected.put(new com.github.steveice10.opennbt.tag.builtin.IntTag("Sub2", 42));
            expected.put(new com.github.steveice10.opennbt.tag.builtin.LongTag("Sub3", 42L));
            expected.put(new com.github.steveice10.opennbt.tag.builtin.FloatTag("Sub4", 42.0f));
            expected.put(new com.github.steveice10.opennbt.tag.builtin.DoubleTag("Sub5", 42.0));
            expected.put(new com.github.steveice10.opennbt.tag.builtin.StringTag("Sub6", "Glavo"));
            expected.put(new com.github.steveice10.opennbt.tag.builtin.ByteArrayTag("Sub7", new byte[]{1, 2, 3}));
            expected.put(new com.github.steveice10.opennbt.tag.builtin.IntArrayTag("Sub8", new int[]{1, 2, 3}));
            expected.put(new com.github.steveice10.opennbt.tag.builtin.LongArrayTag("Sub9", new long[]{1, 2, 3}));
            {
                var sub10 = new com.github.steveice10.opennbt.tag.builtin.CompoundTag("Sub10");
                sub10.put(new com.github.steveice10.opennbt.tag.builtin.ByteTag("Sub10Sub0", (byte) 42));
                expected.put(sub10);
            }
            {
                var sub11 = new com.github.steveice10.opennbt.tag.builtin.ListTag("Sub11", com.github.steveice10.opennbt.tag.builtin.IntTag.class);
                for (int i = 0; i < 10000; i++) {
                    sub11.add(new com.github.steveice10.opennbt.tag.builtin.IntTag("", i));
                }
                expected.put(sub11);
            }

            var actual = new CompoundTag("Meow");
            actual.add(new ByteTag("Sub0", (byte) 42));
            actual.add(new ShortTag("Sub1", (short) 42));
            actual.add(new IntTag("Sub2", 42));
            actual.add(new LongTag("Sub3", 42L));
            actual.add(new FloatTag("Sub4", 42.0f));
            actual.add(new DoubleTag("Sub5", 42.0));
            actual.add(new StringTag("Sub6", "Glavo"));
            actual.add(new ByteArrayTag("Sub7", new byte[]{1, 2, 3}));
            actual.add(new IntArrayTag("Sub8", new int[]{1, 2, 3}));
            actual.add(new LongArrayTag("Sub9", new long[]{1, 2, 3}));
            {
                var sub10 = new CompoundTag("Sub10");
                sub10.add(new ByteTag("Sub10Sub0", (byte) 42));
                actual.add(sub10);
            }
            {
                var sub11 = new ListTag<>("Sub11", IntTag.class);
                for (int i = 0; i < 10000; i++) {
                    sub11.add(new IntTag("", i));
                }
                actual.add(sub11);
            }

            assertTagEquals(expected, actual);
        }
    }

    @Test
    public void testWriteModifiedUTF8String() throws IOException {
        String value = "ABCǾ喵喵喵🐱ABC123";

        var expected = new com.github.steveice10.opennbt.tag.builtin.StringTag("Meow", value);
        var actual = new StringTag("Meow", value);
        assertTagEquals(expected, actual);
    }
}
