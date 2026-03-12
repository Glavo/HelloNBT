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

import java.util.List;
import java.util.function.Supplier;

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
            assertEquals(Byte.toUnsignedInt(value), byteTag.getUnsigned());
            assertEquals(value.toString(), byteTag.getAsString());
        } else if (expected instanceof Short value) {
            ShortTag shortTag = assertInstanceOf(ShortTag.class, tag);

            assertEquals(value, shortTag.get());
            assertEquals(value, shortTag.getValue());
            assertEquals(Short.toUnsignedInt(value), shortTag.getUnsigned());
            assertEquals(value.toString(), shortTag.getAsString());
        } else if (expected instanceof Integer value) {
            IntTag intTag = assertInstanceOf(IntTag.class, tag);

            assertEquals(value, intTag.get());
            assertEquals(value, intTag.getValue());
            assertEquals(Integer.toUnsignedLong(value), intTag.getUnsigned());
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
    @SuppressWarnings("DataFlowIssue")
    void testConstructor() {
        {
            var tag = new ByteTag();
            assertEquals("", tag.getName());
            assertIntegralEquals((byte) 0, tag);

            tag = new ByteTag("Meow");
            assertEquals("Meow", tag.getName());
            assertIntegralEquals((byte) 0, tag);

            tag = new ByteTag("Meow", (byte) 114);
            assertEquals("Meow", tag.getName());
            assertIntegralEquals((byte) 114, tag);

            assertThrows(NullPointerException.class, () -> new ByteTag(null));
            assertThrows(NullPointerException.class, () -> new ByteTag(null, (byte) 114));
        }

        {
            var tag = new ShortTag();
            assertEquals("", tag.getName());
            assertIntegralEquals((short) 0, tag);

            tag = new ShortTag("Meow");
            assertEquals("Meow", tag.getName());
            assertIntegralEquals((short) 0, tag);

            tag = new ShortTag("Meow", (short) 114);
            assertEquals("Meow", tag.getName());
            assertIntegralEquals((short) 114, tag);

            assertThrows(NullPointerException.class, () -> new ShortTag(null));
            assertThrows(NullPointerException.class, () -> new ShortTag(null, (short) 114));
        }

        {
            var tag = new IntTag();
            assertEquals("", tag.getName());
            assertIntegralEquals(0, tag);

            tag = new IntTag("Meow");
            assertEquals("Meow", tag.getName());
            assertIntegralEquals(0, tag);

            tag = new IntTag("Meow", 114);
            assertEquals("Meow", tag.getName());
            assertIntegralEquals(114, tag);

            assertThrows(NullPointerException.class, () -> new IntTag(null));
            assertThrows(NullPointerException.class, () -> new IntTag(null, 114));
        }

        {
            var tag = new LongTag();
            assertEquals("", tag.getName());
            assertIntegralEquals(0L, tag);

            tag = new LongTag("Meow");
            assertEquals("Meow", tag.getName());
            assertIntegralEquals(0L, tag);

            tag = new LongTag("Meow", 114L);
            assertEquals("Meow", tag.getName());
            assertIntegralEquals(114L, tag);

            assertThrows(NullPointerException.class, () -> new LongTag(null));
            assertThrows(NullPointerException.class, () -> new LongTag(null, 114L));
        }

        {
            var tag = new FloatTag();
            assertEquals("", tag.getName());
            assertFloatingEquals(0.0f, tag);

            tag = new FloatTag("Meow");
            assertEquals("Meow", tag.getName());
            assertFloatingEquals(0.0f, tag);

            tag = new FloatTag("Meow", 114.0f);
            assertEquals("Meow", tag.getName());
            assertFloatingEquals(114.0f, tag);

            assertThrows(NullPointerException.class, () -> new FloatTag(null));
            assertThrows(NullPointerException.class, () -> new FloatTag(null, 114.0f));
        }

        {
            var tag = new DoubleTag();
            assertEquals("", tag.getName());
            assertFloatingEquals(0.0, tag);

            tag = new DoubleTag("Meow");
            assertEquals("Meow", tag.getName());
            assertFloatingEquals(0.0, tag);

            tag = new DoubleTag("Meow", 114.0);
            assertEquals("Meow", tag.getName());
            assertFloatingEquals(114.0, tag);


            assertThrows(NullPointerException.class, () -> new DoubleTag(null));
            assertThrows(NullPointerException.class, () -> new DoubleTag(null, 114.0));
        }

        {
            var tag = new StringTag();
            assertEquals("", tag.getName());
            assertEquals("", tag.get());
            assertEquals("", tag.getValue());
            assertEquals("", tag.getAsString());

            tag = new StringTag("Meow");
            assertEquals("Meow", tag.getName());
            assertEquals("", tag.get());
            assertEquals("", tag.getValue());
            assertEquals("", tag.getAsString());

            tag = new StringTag("Meow", "Hello");
            assertEquals("Meow", tag.getName());
            assertEquals("Hello", tag.get());
            assertEquals("Hello", tag.getValue());
            assertEquals("Hello", tag.getAsString());

            assertThrows(NullPointerException.class, () -> new StringTag(null));
            assertThrows(NullPointerException.class, () -> new StringTag(null, "Hello"));
            assertThrows(NullPointerException.class, () -> new StringTag("Meow", null));
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

    private static void assertContentEquals(ValueTag<?> expected, ValueTag<?> actual) {
        Supplier<String> errorMessage = () -> "Expected %s to be equal to %s".formatted(expected, actual);

        assertTrue(expected.contentEquals(actual), errorMessage);
        assertTrue(actual.contentEquals(expected), errorMessage);
        assertEquals(expected.contentHashCode(), actual.contentHashCode(), errorMessage);
    }

    private static void assertContentNotEquals(ValueTag<?> expected, ValueTag<?> actual) {
        Supplier<String> errorMessage = () -> "Expected %s not to be equal to %s".formatted(expected, actual);

        assertFalse(expected.contentEquals(actual), errorMessage);
        assertFalse(actual.contentEquals(expected), errorMessage);
        assertNotEquals(expected.contentHashCode(), actual.contentHashCode(), errorMessage);
    }

    @Test
    void testContentEquals() {
        assertContentEquals(new ByteTag("Meow", (byte) 114), new ByteTag("Meow", (byte) 114));
        assertContentEquals(new ByteTag("Meow", (byte) 114), new ByteTag("MeowMeow", (byte) 114));
        assertContentNotEquals(new ByteTag("Meow", (byte) 114), new ByteTag("Meow", (byte) 514));
        assertContentNotEquals(new ByteTag("Meow", (byte) 114), new ByteTag("MeowMeow", (byte) 514));

        assertContentEquals(new ShortTag("Meow", (short) 114), new ShortTag("Meow", (short) 114));
        assertContentEquals(new ShortTag("Meow", (short) 114), new ShortTag("MeowMeow", (short) 114));
        assertContentNotEquals(new ShortTag("Meow", (short) 114), new ShortTag("Meow", (short) 514));
        assertContentNotEquals(new ShortTag("Meow", (short) 114), new ShortTag("MeowMeow", (short) 514));

        assertContentEquals(new IntTag("Meow", 114), new IntTag("Meow", 114));
        assertContentEquals(new IntTag("Meow", 114), new IntTag("MeowMeow", 114));
        assertContentNotEquals(new IntTag("Meow", 114), new IntTag("Meow", 514));
        assertContentNotEquals(new IntTag("Meow", 114), new IntTag("MeowMeow", 514));

        assertContentEquals(new LongTag("Meow", 114L), new LongTag("Meow", 114L));
        assertContentEquals(new LongTag("Meow", 114L), new LongTag("MeowMeow", 114L));
        assertContentNotEquals(new LongTag("Meow", 114L), new LongTag("Meow", 514L));
        assertContentNotEquals(new LongTag("Meow", 114L), new LongTag("MeowMeow", 514L));

        assertContentEquals(new FloatTag("Meow", 114.0f), new FloatTag("Meow", 114.0f));
        assertContentEquals(new FloatTag("Meow", 114.0f), new FloatTag("MeowMeow", 114.0f));
        assertContentEquals(new FloatTag("Meow", Float.NaN), new FloatTag("Meow", Float.NaN));
        assertContentEquals(new FloatTag("Meow", Float.NaN), new FloatTag("Meow", Float.intBitsToFloat(0x7f800001)));
        assertContentEquals(new FloatTag("Meow", Float.POSITIVE_INFINITY), new FloatTag("Meow", Float.POSITIVE_INFINITY));
        assertContentEquals(new FloatTag("Meow", Float.NEGATIVE_INFINITY), new FloatTag("Meow", Float.NEGATIVE_INFINITY));
        assertContentNotEquals(new FloatTag("Meow", 114.0f), new FloatTag("Meow", 514.0f));
        assertContentNotEquals(new FloatTag("Meow", 114.0f), new FloatTag("MeowMeow", 514.0f));

        assertContentEquals(new DoubleTag("Meow", 114.0), new DoubleTag("Meow", 114.0));
        assertContentEquals(new DoubleTag("Meow", 114.0), new DoubleTag("MeowMeow", 114.0));
        assertContentEquals(new DoubleTag("Meow", Double.NaN), new DoubleTag("Meow", Double.NaN));
        assertContentEquals(new DoubleTag("Meow", Double.NaN), new DoubleTag("Meow", Double.longBitsToDouble(0x7FF800000000DEADL)));
        assertContentEquals(new DoubleTag("Meow", Double.POSITIVE_INFINITY), new DoubleTag("Meow", Double.POSITIVE_INFINITY));
        assertContentEquals(new DoubleTag("Meow", Double.NEGATIVE_INFINITY), new DoubleTag("Meow", Double.NEGATIVE_INFINITY));
        assertContentNotEquals(new DoubleTag("Meow", 114.0), new DoubleTag("Meow", 514.0));
        assertContentNotEquals(new DoubleTag("Meow", 114.0), new DoubleTag("MeowMeow", 514.0));

        assertContentEquals(new StringTag("Meow", "Hello"), new StringTag("Meow", "Hello"));
        assertContentEquals(new StringTag("Meow", "Hello"), new StringTag("MeowMeow", "Hello"));
        assertContentNotEquals(new StringTag("Meow", "Hello"), new StringTag("Meow", "World"));
        assertContentNotEquals(new StringTag("Meow", "Hello"), new StringTag("MeowMeow", "World"));


        List<ValueTag<?>> tags = List.of(
                new ByteTag("Meow", (byte) 1),
                new ShortTag("Meow", (short) 2),
                new IntTag("Meow", 3),
                new LongTag("Meow", 4L),
                new FloatTag("Meow", 5.0f),
                new DoubleTag("Meow", 6.0),
                new StringTag("Meow", "Hello")
        );

        for (ValueTag<?> tag1 : tags) {
            for (ValueTag<?> tag2 : tags) {
                if (tag1 == tag2) {
                    assertContentEquals(tag1, tag2);
                } else {
                    assertContentNotEquals(tag1, tag2);
                }
            }
        }
    }

    private static void assertToString(String expected, ValueTag<?> tag) {
        assertEquals(expected, tag.toString());
    }

    @Test
    void testToString() {
        assertToString("0B", new ByteTag());
        assertToString("114B", new ByteTag("", (byte) 114));
        assertToString("Meow: 114B", new ByteTag("Meow", (byte) 114));

        assertToString("0S", new ShortTag());
        assertToString("114S", new ShortTag("", (short) 114));
        assertToString("Meow: 114S", new ShortTag("Meow", (short) 114));

        assertToString("0I", new IntTag());
        assertToString("114I", new IntTag("", 114));
        assertToString("Meow: 114I", new IntTag("Meow", 114));

        assertToString("0L", new LongTag());
        assertToString("114L", new LongTag("", 114L));
        assertToString("Meow: 114L", new LongTag("Meow", 114L));

        assertToString("0.0F", new FloatTag());
        assertToString("114.0F", new FloatTag("", 114.0f));
        assertToString("Meow: 114.0F", new FloatTag("Meow", 114.0f));

        assertToString("0.0D", new DoubleTag());
        assertToString("114.0D", new DoubleTag("", 114.0));
        assertToString("Meow: 114.0D", new DoubleTag("Meow", 114.0));

        assertToString("\"\"", new StringTag());
        assertToString("\"Hello\"", new StringTag("", "Hello"));
        assertToString("Meow: \"Hello\"", new StringTag("Meow", "Hello"));
        assertToString("Meow: \"\\u0000\\b\\t\\n\\f\\r\\n \\\"ABC你好世界\\U0001F604\"", new StringTag("Meow", "\0\b\t\n\f\r\n \"ABC你好世界😄"));
    }
}
