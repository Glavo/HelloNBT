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
package org.glavo.nbt.internal;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public final class StringCacheTest {
    @Test
    public void test() {
        var strings = new ArrayList<String>();
        for (int i = 0; i < 10000; i++) {
            strings.add("str" + i);
        }

        var cache = new StringCache(strings.toArray(new String[0]));

        for (String string : strings) {
            byte[] bytes = string.getBytes(StandardCharsets.UTF_8);

            assertSame(string, cache.get(bytes, 0, bytes.length));

            assertNull(cache.get(bytes, 1, bytes.length - 1));

            // Test with a different offset
            byte[] newBytes = new byte[bytes.length + 1];
            System.arraycopy(bytes, 0, newBytes, 1, bytes.length);
            assertSame(string, cache.get(newBytes, 1, bytes.length));

            // Test with leading zeros
            Arrays.fill(newBytes, (byte) 0);
            System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
            assertSame(string, cache.get(newBytes, 0, bytes.length));
        }

        assertNull(cache.get("ABC".getBytes(StandardCharsets.UTF_8)));

        // Test long strings
        assertNull(cache.get("A".repeat(20).getBytes(StandardCharsets.UTF_8)));

        // Test non-ASCII characters
        assertNull(cache.get("喵".getBytes(StandardCharsets.UTF_8)));
    }
}
