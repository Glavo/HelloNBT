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
import static org.junit.jupiter.api.Assertions.assertSame;

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

    @Test
    void testDefaultConstructor() {
        {
            var tag = new ByteTag();
            assertEquals("", tag.getName());
            assertEquals((byte) 0, tag.get());
            assertEquals(0, tag.getUnsigned());
            assertEquals(Byte.valueOf((byte) 0), tag.getValue());
            assertEquals("0", tag.getAsString());
        }

        {
            var tag = new ShortTag();
            assertEquals("", tag.getName());
            assertEquals((short) 0, tag.get());
            assertEquals(0, tag.getUnsigned());
            assertEquals(Short.valueOf((short) 0), tag.getValue());
            assertEquals("0", tag.getAsString());
        }

        {
            var tag = new IntTag();
            assertEquals("", tag.getName());
            assertEquals(0, tag.get());
            assertEquals(0L, tag.getUnsigned());
            assertEquals(Integer.valueOf(0), tag.getValue());
            assertEquals("0", tag.getAsString());
        }

        {
            var tag = new LongTag();
            assertEquals("", tag.getName());
            assertEquals(0L, tag.get());
            assertEquals(Long.valueOf(0L), tag.getValue());
            assertEquals("0", tag.getAsString());
        }

        {
            var tag = new FloatTag();
            assertEquals("", tag.getName());
            assertEquals(0.0f, tag.get());
            assertEquals(Float.valueOf(0.0f), tag.getValue());
            assertEquals("0.0", tag.getAsString());
        }

        {
            var tag = new DoubleTag();
            assertEquals("", tag.getName());
            assertEquals(0.0, tag.get());
            assertEquals(Double.valueOf(0.0), tag.getValue());
            assertEquals("0.0", tag.getAsString());
        }

        {
            var tag = new StringTag();
            assertEquals("", tag.getName());
            assertEquals("", tag.get());
            assertEquals("", tag.getValue());
            assertEquals("", tag.getAsString());
        }
    }
}
