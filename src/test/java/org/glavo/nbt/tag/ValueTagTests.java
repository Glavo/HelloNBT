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

final class ValueTagTests {

    @Test
    void testGetType() {
        assertSame(TagType.BYTE, new ByteTag().getType());
        assertSame(TagType.SHORT, new ShortTag().getType());
        assertSame(TagType.INT, new IntTag().getType());
        assertSame(TagType.LONG, new LongTag().getType());
        assertSame(TagType.FLOAT, new FloatTag().getType());
        assertSame(TagType.DOUBLE, new DoubleTag().getType());
        assertSame(TagType.STRING, new StringTag().getType());
    }

    private static void assertIntegralEquals(Number expected, ValueTag<? extends Number> tag) {
        if (expected instanceof Byte value) {
            ByteTag byteTag = assertInstanceOf(ByteTag.class, tag);

            assertEquals(value, byteTag.get());
            assertEquals(value, byteTag.getValue());
            assertEquals(value.toString(), byteTag.getAsString());
        } else if (expected instanceof Short value) {
            ShortTag shortTag = assertInstanceOf(ShortTag.class, tag);

            assertEquals(value, shortTag.get());
            assertEquals(value, shortTag.getValue());
            assertEquals(value.toString(), shortTag.getAsString());
        } else if (expected instanceof Integer value) {
            IntTag intTag = assertInstanceOf(IntTag.class, tag);

            assertEquals(value, intTag.get());
            assertEquals(value, intTag.getValue());
            assertEquals(value.toString(), intTag.getAsString());
        } else if (expected instanceof Long value) {
            LongTag longTag = assertInstanceOf(LongTag.class, tag);

            assertEquals(value, longTag.get());
            assertEquals(value, longTag.getValue());
            assertEquals(value.toString(), longTag.getAsString());
        } else {
            throw new IllegalArgumentException("Unsupported tag type: " + tag.getType());
        }
    }

    private static void assertFloatingEquals(Number expected, ValueTag<? extends Number> tag) {
        if (expected instanceof Float value) {
            FloatTag floatTag = assertInstanceOf(FloatTag.class, tag);
            assertEquals(value, floatTag.get());
            assertEquals(value, floatTag.getValue());
            assertEquals(Float.toString(value), floatTag.getAsString());

        } else if (expected instanceof Double value) {
            DoubleTag doubleTag = assertInstanceOf(DoubleTag.class, tag);
            assertEquals(value, doubleTag.get());
            assertEquals(value, doubleTag.getValue());
            assertEquals(Double.toString(value), doubleTag.getAsString());
        } else {
            throw new IllegalArgumentException("Unsupported number type: " + expected.getClass().getName());
        }
    }

    @Test
    void testDefaultConstructor() {
        {
            var tag = new ByteTag();
            assertEquals("", tag.getName());
            assertIntegralEquals((byte) 0, tag);
        }

        {
            var tag = new ShortTag();
            assertEquals("", tag.getName());
            assertIntegralEquals((short) 0, tag);
        }

        {
            var tag = new IntTag();
            assertEquals("", tag.getName());
            assertIntegralEquals(0, tag);
        }

        {
            var tag = new LongTag();
            assertEquals("", tag.getName());
            assertIntegralEquals(0L, tag);
        }

        {
            var tag = new FloatTag();
            assertEquals("", tag.getName());
            assertFloatingEquals(0.0f, tag);
        }

        {
            var tag = new DoubleTag();
            assertEquals("", tag.getName());
            assertFloatingEquals(0.0, tag);
        }

        {
            var tag = new StringTag();
            assertEquals("", tag.getName());
            assertEquals("", tag.get());
            assertEquals("", tag.getValue());
            assertEquals("", tag.getAsString());
        }
    }

    @Test
    void testSet() {
        {
            var tag = new ByteTag();

            tag.set((byte) 114);
            assertIntegralEquals((byte) 114, tag);

            tag.setValue((byte) 514);
            assertIntegralEquals((byte) 514, tag);

            tag.setUnsigned(0xFA);
            assertIntegralEquals((byte) 0xFA, tag);
        }

        {
            var tag = new ShortTag();

            tag.set((short) 114);
            assertIntegralEquals((short) 114, tag);

            tag.setValue((short) 514);
            assertIntegralEquals((short) 514, tag);

            tag.setUnsigned(0xFAFA);
            assertIntegralEquals((short) 0xFAFA, tag);
        }

        {
            var tag = new IntTag();
            tag.set(114);
            assertIntegralEquals(114, tag);

            tag.setValue(514);
            assertIntegralEquals(514, tag);

            tag.setUnsigned(0xFAFAFAFAL);
            assertIntegralEquals(0xFAFAFAFA, tag);
        }

        {
            var tag = new LongTag();
            tag.set(114L);
            assertIntegralEquals(114L, tag);

            tag.setValue(514L);
            assertIntegralEquals(514L, tag);
        }

        {
            var tag = new FloatTag();
            tag.set(114.0f);
            assertFloatingEquals(114.0f, tag);

            tag.setValue(514.0f);
            assertFloatingEquals(514.0f, tag);
        }

        {
            var tag = new DoubleTag();
            tag.set(114.0);
            assertFloatingEquals(114.0, tag);

            tag.setValue(514.0);
            assertFloatingEquals(514.0, tag);
        }

        {
            var tag = new StringTag();
            tag.set("Hello");
            assertEquals("Hello", tag.get());
            assertEquals("Hello", tag.getValue());
            assertEquals("Hello", tag.getAsString());

            tag.setValue("World");
            assertEquals("World", tag.get());
            assertEquals("World", tag.getValue());
            assertEquals("World", tag.getAsString());
        }
    }

    @Test
    void testClone() {
        {
            var tag = new ByteTag("Meow", (byte) 114);
            var clone = tag.clone();

            assertNotSame(tag, clone);
            assertEquals("Meow", clone.getName());
            assertIntegralEquals((byte) 114, clone);
        }

        {
            var tag = new ShortTag("Meow", (short) 114);
            var clone = tag.clone();

            assertNotSame(tag, clone);
            assertEquals("Meow", clone.getName());
            assertIntegralEquals((short) 114, clone);
        }

        {
            var tag = new IntTag("Meow", 114);
            var clone = tag.clone();

            assertNotSame(tag, clone);
            assertEquals("Meow", clone.getName());
            assertIntegralEquals(114, clone);
        }

        {
            var tag = new LongTag("Meow", 114L);
            var clone = tag.clone();

            assertNotSame(tag, clone);
            assertEquals("Meow", clone.getName());
            assertIntegralEquals(114L, clone);
        }

        {
            var tag = new FloatTag("Meow", 114.0f);
            var clone = tag.clone();

            assertNotSame(tag, clone);
            assertEquals("Meow", clone.getName());
            assertFloatingEquals(114.0f, clone);
        }

        {
            var tag = new DoubleTag("Meow", 114.0);
            var clone = tag.clone();

            assertNotSame(tag, clone);
            assertEquals("Meow", clone.getName());
            assertFloatingEquals(114.0, clone);
        }

        {
            var tag = new StringTag("Meow", "Hello");
            var clone = tag.clone();

            assertNotSame(tag, clone);
            assertEquals("Meow", clone.getName());
            assertEquals("Hello", clone.get());
            assertEquals("Hello", clone.getValue());
            assertEquals("Hello", clone.getAsString());
        }
    }
}
