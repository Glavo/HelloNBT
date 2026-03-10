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

import org.glavo.nbt.internal.ArrayAccessor;
import org.junit.jupiter.api.Test;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

abstract class ArrayTagTests<AT extends ArrayTag<E, T, A, B>, E extends Number, T extends ValueTag<E>, A, B extends Buffer> {
    abstract ArrayAccessor<E, T, A, B> accessor();

    abstract AT create();

    abstract AT create(String name);

    abstract AT create(String name, A array);

    final A newArray(int size) {
        return accessor().newArray(size);
    }

    final int getLength(A array) {
        return accessor().getLength(array);
    }

    final E get(A array, int index) {
        return accessor().get(array, index);
    }

    final void set(A array, int index, E value) {
        accessor().set(array, index, value);
    }

    abstract A randomArray(Random random, int size);

    void assertArrayEquals(A expected, A actual) {
        if (getLength(expected) != getLength(actual)) {
            throw new AssertionError("Array lengths differ: " + getLength(expected) + " != " + getLength(actual));
        }

        for (int i = 0, end = getLength(expected); i < end; i++) {
            if (!accessor().get(expected, i).equals(accessor().get(actual, i))) {
                throw new AssertionError("Array contents differ at index " + i + ": " + accessor().get(expected, i) + " != " + accessor().get(actual, i));
            }
        }
    }

    @Test
    void testConstructor() {
        AT tag = create();
        assertEquals("", tag.getName());
        assertEquals(0, tag.size());
        assertEquals(0, getLength(tag.getArray()));

        tag = create("test");
        assertEquals("test", tag.getName());
        assertEquals(0, tag.size());
        assertEquals(0, getLength(tag.getArray()));

        tag = create("test", accessor().empty());
        assertEquals("test", tag.getName());
        assertEquals(0, tag.size());
        assertEquals(0, getLength(tag.getArray()));

        Random random = new Random(0);
        A array = randomArray(random, 10);
        tag = create("test", array);
        assertEquals("test", tag.getName());
        assertEquals(10, tag.size());
        assertNotSame(array, tag.values);
        assertArrayEquals(array, tag.values);
        assertArrayEquals(array, tag.getArray());
        assertArrayEquals(array, accessor().get(tag.getBuffer()));
    }

    static final class ByteArrayTagTests extends ArrayTagTests<ByteArrayTag, Byte, ByteTag, byte[], ByteBuffer> {
        @Override
        ArrayAccessor<Byte, ByteTag, byte[], ByteBuffer> accessor() {
            return ArrayAccessor.BYTE_ARRAY;
        }

        @Override
        ByteArrayTag create() {
            return new ByteArrayTag();
        }

        @Override
        ByteArrayTag create(String name) {
            return new ByteArrayTag(name);
        }

        @Override
        ByteArrayTag create(String name, byte[] array) {
            return new ByteArrayTag(name, array);
        }

        @Override
        byte[] randomArray(Random random, int size) {
            byte[] array = new byte[size];
            random.nextBytes(array);
            return array;
        }
    }

    static final class IntArrayTagTests extends ArrayTagTests<IntArrayTag, Integer, IntTag, int[], IntBuffer> {

        @Override
        ArrayAccessor<Integer, IntTag, int[], IntBuffer> accessor() {
            return ArrayAccessor.INT_ARRAY;
        }

        @Override
        IntArrayTag create() {
            return new IntArrayTag();
        }

        @Override
        IntArrayTag create(String name) {
            return new IntArrayTag(name);
        }

        @Override
        IntArrayTag create(String name, int[] array) {
            return new IntArrayTag(name, array);
        }

        @Override
        int[] randomArray(Random random, int size) {
            return random.ints(size).limit(size).toArray();
        }

        @Test
        void testUUID() {
            var tag = new IntArrayTag();
            var uuid = UUID.fromString("f81d4fae-7dec-11d0-a765-00a0c91e6bf6");

            assertFalse(tag.isUUID());
            assertThrows(IllegalStateException.class, tag::getUUID);

            tag.setUUID(uuid);
            assertTrue(tag.isUUID());
            assertEquals(4, tag.size());
            assertArrayEquals(new int[]{-132296786, 2112623056, -1486552928, -920753162}, tag.getArray());
            assertEquals(uuid, tag.getUUID());

            tag.add(114);
            assertFalse(tag.isUUID());
            assertThrows(IllegalStateException.class, tag::getUUID);

            uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
            tag.setUUID(uuid);
            assertTrue(tag.isUUID());
            assertEquals(4, tag.size());
            assertArrayEquals(new int[]{0, 0, 0, 0}, tag.getArray());
            assertEquals(uuid, tag.getUUID());

            tag.clear();
            assertFalse(tag.isUUID());
            assertThrows(IllegalStateException.class, tag::getUUID);

            var random = new Random(0);

            for (int i = 0; i < 100; i++) {
                uuid = new UUID(random.nextLong(), random.nextLong());
                tag.setUUID(uuid);
                assertTrue(tag.isUUID());
                assertEquals(4, tag.size());
                assertEquals(uuid, tag.getUUID());

                tag.clear();
                assertFalse(tag.isUUID());
                assertThrows(IllegalStateException.class, tag::getUUID);
            }
        }
    }

    static final class LongArrayTagTests extends ArrayTagTests<LongArrayTag, Long, LongTag, long[], LongBuffer> {
        @Override
        ArrayAccessor<Long, LongTag, long[], LongBuffer> accessor() {
            return ArrayAccessor.LONG_ARRAY;
        }

        @Override
        LongArrayTag create() {
            return new LongArrayTag();
        }

        @Override
        LongArrayTag create(String name) {
            return new LongArrayTag(name);
        }

        @Override
        LongArrayTag create(String name, long[] array) {
            return new LongArrayTag(name, array);
        }

        @Override
        long[] randomArray(Random random, int size) {
            return random.longs(size).limit(size).toArray();
        }
    }
}
