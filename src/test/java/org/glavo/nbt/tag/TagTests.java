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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class TagTests {

    @Test
    public void testDefaultValue() {
        assertEquals((byte) 0, new ByteTag().get());
        assertEquals((short) 0, new ShortTag().get());
        assertEquals(0, new IntTag().get());
        assertEquals(0L, new LongTag().get());
        assertEquals(0.0f, new FloatTag().get());
        assertEquals(0.0, new DoubleTag().get());
        assertEquals("", new StringTag().get());

        {
            var tag = new ByteArrayTag();
            assertEquals(0, tag.size());
            assertTrue(tag.isEmpty());
        }

        {
            var tag = new IntArrayTag();
            assertEquals(0, tag.size());
            assertTrue(tag.isEmpty());
        }

        {
            var tag = new LongArrayTag();
            assertEquals(0, tag.size());
            assertTrue(tag.isEmpty());
        }
    }

    @Test
    public void testDefaultName() {
        assertEquals("", new ByteTag().getName());
        assertEquals("", new ShortTag().getName());
        assertEquals("", new IntTag().getName());
        assertEquals("", new LongTag().getName());
        assertEquals("", new FloatTag().getName());
        assertEquals("", new DoubleTag().getName());
        assertEquals("", new StringTag().getName());
        assertEquals("", new ByteArrayTag().getName());
        assertEquals("", new IntArrayTag().getName());
        assertEquals("", new LongArrayTag().getName());
        assertEquals("", new ListTag<>().getName());
        assertEquals("", new CompoundTag<>().getName());
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
