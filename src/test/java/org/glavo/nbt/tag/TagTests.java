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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public final class TagTests {

    @Test
    public void testDefaultValue() {
        {
            var tag = new ByteTag();
            assertEquals((byte) 0, tag.get());
            assertEquals("", tag.getName());
            assertFalse(tag.getBoolean());
        }

        {
            var tag = new ShortTag();
            assertEquals((short) 0, tag.get());
            assertEquals("", tag.getName());
        }

        {
            var tag = new IntTag();
            assertEquals(0, tag.get());
            assertEquals("", tag.getName());
        }

        {
            var tag = new LongTag();
            assertEquals(0L, tag.get());
            assertEquals("", tag.getName());
        }

        {
            var tag = new FloatTag();
            assertEquals(0.0f, tag.get());
            assertEquals("", tag.getName());
        }

        {
            var tag = new DoubleTag();
            assertEquals(0.0, tag.get());
            assertEquals("", tag.getName());
        }

        {
            var tag = new StringTag();
            assertEquals("", tag.get());
            assertEquals("", tag.getName());

        }

        {
            var tag = new ByteArrayTag();
            assertEquals("", tag.getName());
            assertEquals(0, tag.size());
            assertTrue(tag.isEmpty());
        }

        {
            var tag = new IntArrayTag();
            assertEquals("", tag.getName());
            assertEquals(0, tag.size());
            assertTrue(tag.isEmpty());
        }

        {
            var tag = new LongArrayTag();
            assertEquals("", tag.getName());
            assertEquals(0, tag.size());
            assertTrue(tag.isEmpty());
        }
    }

    @Test
    public void testCreateTagWithName() {
        assertEquals("Meow", new ByteTag("Meow").getName());
        assertEquals("Meow", new ShortTag("Meow").getName());
        assertEquals("Meow", new IntTag("Meow").getName());
        assertEquals("Meow", new LongTag("Meow").getName());
        assertEquals("Meow", new FloatTag("Meow").getName());
        assertEquals("Meow", new DoubleTag("Meow").getName());
        assertEquals("Meow", new StringTag("Meow").getName());
        assertEquals("Meow", new ByteArrayTag("Meow").getName());
        assertEquals("Meow", new IntArrayTag("Meow").getName());
        assertEquals("Meow", new LongArrayTag("Meow").getName());
        assertEquals("Meow", new ListTag<>("Meow").getName());
        assertEquals("Meow", new CompoundTag<>("Meow").getName());
    }
}
