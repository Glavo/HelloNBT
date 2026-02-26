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

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/// A simple ASCII string cache pool.
public final class StringCache {

    private final Object[] pool; // (String | String[])[]
    private int maxLength = 0;

    private StringCache(String... strings) {
        this.pool = new String[strings.length << 1];

        for (String str : strings) {
            add(str);
        }
    }

    private void add(String str) {
        int idx = Math.floorMod(str.hashCode(), pool.length);

        Object old = pool[idx];
        if (old == null) {
            pool[idx] = str;
        } else if (old instanceof String oldStr) {
            pool[idx] = new String[]{oldStr, str};
        } else if (old instanceof String[] array) {
            String[] newArray = Arrays.copyOf(array, array.length + 1);
            newArray[array.length] = str;
            pool[idx] = newArray;
        } else {
            throw new AssertionError("Unexpected object in pool: " + old);
        }

        maxLength = Math.max(maxLength, str.length());
    }

    private static boolean equals(String str, byte[] array, int offset, int length, int hash) {
        if (length == str.length() && hash == str.hashCode()) {
            for (int i = offset, j = 0; i < offset + length; i++, j++) {
                if (array[i] != str.charAt(j)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private @Nullable String get(byte[] array, int offset, int length) {
        if (length > maxLength) {
            return null;
        }

        int hash = 0;
        for (int i = offset; i < offset + length; i++) {
            byte ch = array[i];

            // Not ASCII String
            if (ch < 0) {
                return null;
            }

            hash = 31 * hash + ch;
        }

        int idx = Math.floorMod(hash, pool.length);
        Object obj = pool[idx];

        if (obj == null) {
            return null;
        } else if (obj instanceof String str) {
            return equals(str, array, offset, length, hash) ? str : null;
        } else if (obj instanceof String[] strings) {
            for (String str : strings) {
                if (equals(str, array, offset, length, hash)) {
                    return str;
                }
            }
            return null;
        } else {
            throw new AssertionError("Unexpected object in pool: " + obj);
        }
    }
}
