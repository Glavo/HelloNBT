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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public final class UUIDTest {
    @Test
    void test() {
        var tag = new IntArrayTag();

        {
            var uuid = UUID.fromString("f81d4fae-7dec-11d0-a765-00a0c91e6bf6");

            tag.setUUID(uuid);
            assertArrayEquals(new int[]{-132296786, 2112623056, -1486552928, -920753162}, tag.getValue());
            assertEquals(uuid, tag.getUUID());
        }
    }
}
