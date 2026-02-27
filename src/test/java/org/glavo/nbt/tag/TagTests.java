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
        assertEquals("Meow", new ListTag<>("Meow", TagType.END).getName());
        assertEquals("Meow", new CompoundTag<>("Meow").getName());
    }

    @Test
    public void testSetName() {
        {
            var parent = new CompoundTag<>("Parent");
            assertEquals("Parent", parent.getName());

            // Set the name to the same value
            parent.setName("Parent");
            assertEquals("Parent", parent.getName());

            // Set the name to a different value
            parent.setName("Parent-Modified");
            assertEquals("Parent-Modified", parent.getName());

            //noinspection DataFlowIssue
            assertThrows(NullPointerException.class, () -> parent.setName(null));

            var sub0 = new ByteTag("Sub0");
            var sub1 = new ByteTag("Sub1");
            parent.add(sub0);
            parent.add(sub1);

            assertSame(sub0, parent.get("Sub0"));
            assertSame(sub1, parent.get("Sub1"));

            assertThrows(IllegalStateException.class, () -> sub1.setName("Sub0"));

            // Set the name of subtag to a different value
            sub1.setName("Sub1-Modified");
            assertSame(sub1, parent.get("Sub1-Modified"));
            assertNull(parent.get("Sub1"));
        }

        {
            var list = new ListTag<>("List", ByteTag.class);

            var sub0 = new ByteTag("Sub0");
            var sub1 = new ByteTag("Sub1");

            list.add(sub0);
            list.add(sub1);

            // When a node is added to ListTag, its name should be set to empty
            assertEquals("", sub0.getName());
            assertEquals("", sub1.getName());
        }
    }

    private static void verifyIndex(ParentTag<?> list) {
        int index = 0;
        for (Tag tag : list) {
            assertEquals(index++, tag.getIndex());
        }
    }

    @Test
    public void testSubTagOfListTag() {
        var listTag = new ListTag<>("Parent", IntTag.class);

        for (int i = 0; i < 100; i++) {
            var subTag = new IntTag("", i);

            assertEquals(-1, subTag.getIndex());
            assertNull(subTag.getParent());

            listTag.add(subTag);

            assertEquals(i, subTag.getIndex());
            assertSame(listTag, subTag.getParent());
        }

        var sub10 = listTag.get(10);
        listTag.remove(sub10);

        assertEquals(-1, sub10.getIndex());
        assertNull(sub10.getParent());

        verifyIndex(listTag);
    }

    @Test
    public void testSubTagOfCompoundTag() {
        var compoundTag = new CompoundTag<>("Parent");

        for (int i = 0; i < 100; i++) {
            var subTag = new IntTag("Sub" + i, i);

            assertEquals(-1, subTag.getIndex());
            assertNull(subTag.getParent());

            compoundTag.add(subTag);

            assertEquals(i, subTag.getIndex());
            assertSame(compoundTag, subTag.getParent());
        }

        var sub10 = compoundTag.get(10);
        compoundTag.remove(sub10);

        assertEquals(-1, sub10.getIndex());
        assertNull(sub10.getParent());

        verifyIndex(compoundTag);
    }
}
