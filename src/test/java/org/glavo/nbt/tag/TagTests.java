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

    @Test
    public void testAddToOtherListTag() {
        var list0 = new ListTag<>("List0", IntTag.class);
        var list1 = new ListTag<>("List1", IntTag.class);

        var subTag = new IntTag("", 0);

        list0.add(subTag);

        assertSame(subTag, list0.get(0));
        assertSame(list0, subTag.getParent());
        assertEquals(0, subTag.getIndex());

        // Move subTag to list1

        list1.add(new IntTag()); // Add a placeholder tag to list1
        list1.add(subTag);

        assertTrue(list0.isEmpty());
        assertEquals(0, list0.size());

        assertSame(subTag, list1.get(1));
        assertSame(list1, subTag.getParent());
        assertEquals(1, subTag.getIndex());
    }

    @Test
    public void testAddToOtherCompoundTag() {
        var compound0 = new CompoundTag<>("List0");
        var compound1 = new CompoundTag<>("List1");

        var subTag = new IntTag("Sub", 0);

        compound0.add(subTag);

        assertSame(subTag, compound0.get(0));
        assertSame(subTag, compound0.get("Sub"));
        assertSame(compound0, subTag.getParent());
        assertEquals(0, subTag.getIndex());

        // Move subTag to compound1

        compound1.add(new IntTag("Placeholder")); // Add a placeholder tag to compound1
        compound1.add(subTag);

        assertTrue(compound0.isEmpty());
        assertEquals(0, compound0.size());
        assertNull(compound0.get("Sub"));

        assertSame(subTag, compound1.get(1));
        assertSame(subTag, compound1.get("Sub"));
        assertSame(compound1, subTag.getParent());
        assertEquals(1, subTag.getIndex());
    }

    @Test
    public void testToString() {
        assertEquals("TAG_BYTE[0]", new ByteTag().toString());
        assertEquals("TAG_SHORT[0]", new ShortTag().toString());
        assertEquals("TAG_INT[0]", new IntTag().toString());
        assertEquals("TAG_LONG[0]", new LongTag().toString());
        assertEquals("TAG_FLOAT[0.0]", new FloatTag().toString());
        assertEquals("TAG_DOUBLE[0.0]", new DoubleTag().toString());
        assertEquals("TAG_STRING[\"\"]", new StringTag().toString());
    }

    @Test
    public void testEquals() {
        assertEquals(new ByteTag("Meow", (byte) 42), new ByteTag("Meow", (byte) 42));
        assertNotEquals(new ByteTag("Meow", (byte) 42), new ByteTag("Meow", (byte) 43));
        assertNotEquals(new ByteTag("Meow", (byte) 42), new ByteTag("Glavo", (byte) 42));

        assertEquals(new ShortTag("Meow", (short) 42), new ShortTag("Meow", (short) 42));
        assertNotEquals(new ShortTag("Meow", (short) 42), new ShortTag("Meow", (short) 43));
        assertNotEquals(new ShortTag("Meow", (short) 42), new ShortTag("Glavo", (short) 42));

        assertEquals(new IntTag("Meow", 42), new IntTag("Meow", 42));
        assertNotEquals(new IntTag("Meow", 42), new IntTag("Meow", 43));
        assertNotEquals(new IntTag("Meow", 42), new IntTag("Glavo", 42));

        assertEquals(new LongTag("Meow", 42L), new LongTag("Meow", 42L));
        assertNotEquals(new LongTag("Meow", 42L), new LongTag("Meow", 43L));
        assertNotEquals(new LongTag("Meow", 42L), new LongTag("Glavo", 42L));

        assertEquals(new FloatTag("Meow", 42.0f), new FloatTag("Meow", 42.0f));
        assertNotEquals(new FloatTag("Meow", 42.0f), new FloatTag("Meow", 43.0f));
        assertNotEquals(new FloatTag("Meow", 42.0f), new FloatTag("Glavo", 42.0f));

        assertEquals(new DoubleTag("Meow", 42.0), new DoubleTag("Meow", 42.0));
        assertNotEquals(new DoubleTag("Meow", 42.0), new DoubleTag("Meow", 43.0));
        assertNotEquals(new DoubleTag("Meow", 42.0), new DoubleTag("Glavo", 42.0));

        assertEquals(new StringTag("Meow", "Glavo"), new StringTag("Meow", "Glavo"));
        assertNotEquals(new StringTag("Meow", "Glavo"), new StringTag("Meow", "Hello"));
        assertNotEquals(new StringTag("Meow", "Glavo"), new StringTag("Glavo", "Glavo"));

        assertEquals(new ByteArrayTag("Meow", new byte[]{1, 2, 3}), new ByteArrayTag("Meow", new byte[]{1, 2, 3}));
        assertNotEquals(new ByteArrayTag("Meow", new byte[]{1, 2, 3}), new ByteArrayTag("Meow", new byte[]{1, 2, 4}));
        assertNotEquals(new ByteArrayTag("Meow", new byte[]{1, 2, 3}), new ByteArrayTag("Glavo", new byte[]{1, 2, 3}));

        assertEquals(new IntArrayTag("Meow", new int[]{1, 2, 3}), new IntArrayTag("Meow", new int[]{1, 2, 3}));
        assertNotEquals(new IntArrayTag("Meow", new int[]{1, 2, 3}), new IntArrayTag("Meow", new int[]{1, 2, 4}));
        assertNotEquals(new IntArrayTag("Meow", new int[]{1, 2, 3}), new IntArrayTag("Glavo", new int[]{1, 2, 3}));

        assertEquals(new LongArrayTag("Meow", new long[]{1, 2, 3}), new LongArrayTag("Meow", new long[]{1, 2, 3}));
        assertNotEquals(new LongArrayTag("Meow", new long[]{1, 2, 3}), new LongArrayTag("Meow", new long[]{1, 2, 4}));
        assertNotEquals(new LongArrayTag("Meow", new long[]{1, 2, 3}), new LongArrayTag("Glavo", new long[]{1, 2, 3}));

        assertEquals(new ListTag<>("Meow", ByteTag.class), new ListTag<>("Meow", ByteTag.class));
        assertNotEquals(new ListTag<>("Meow", ByteTag.class), new ListTag<>("Glavo", ByteTag.class));

        assertEquals(new CompoundTag<>("Meow"), new CompoundTag<>("Meow"));
        assertNotEquals(new CompoundTag<>("Meow"), new CompoundTag<>("Glavo"));
    }
}
