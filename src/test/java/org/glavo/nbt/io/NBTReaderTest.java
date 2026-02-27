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

        return NBTReader.readTag(new ByteArrayInputStream(buffer.toByteArray()),
                NBTReader.Options.newBuilder().byteOrder(byteOrder).build());
    }

    private static void assertTagEquals(
            Tag expected,
            com.github.steveice10.opennbt.tag.builtin.Tag actual) throws IOException {

        assertEquals(expected, convert(actual, ByteOrder.BIG_ENDIAN));
        assertEquals(expected, convert(actual, ByteOrder.LITTLE_ENDIAN));
    }

    @Test
    public void testReadTag() throws IOException {
        assertTagEquals(new ByteTag("Meow", (byte) 42), new com.github.steveice10.opennbt.tag.builtin.ByteTag("Meow", (byte) 42));
        assertTagEquals(new ShortTag("Meow", (short) 42), new com.github.steveice10.opennbt.tag.builtin.ShortTag("Meow", (short) 42));
        assertTagEquals(new IntTag("Meow", 42), new com.github.steveice10.opennbt.tag.builtin.IntTag("Meow", 42));
        assertTagEquals(new LongTag("Meow", 42L), new com.github.steveice10.opennbt.tag.builtin.LongTag("Meow", 42L));
        assertTagEquals(new FloatTag("Meow", 42.0f), new com.github.steveice10.opennbt.tag.builtin.FloatTag("Meow", 42.0f));
        assertTagEquals(new DoubleTag("Meow", 42.0), new com.github.steveice10.opennbt.tag.builtin.DoubleTag("Meow", 42.0));
        assertTagEquals(new StringTag("Meow", "Glavo"), new com.github.steveice10.opennbt.tag.builtin.StringTag("Meow", "Glavo"));
    }
}
