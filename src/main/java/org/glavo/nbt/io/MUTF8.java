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
package org.glavo.nbt.io;

/// Helper class for Modified UTF-8 encoding and decoding
final class MUTF8 {

    public static int encodedLength(String value, int asciiLength) {
        int length = value.length();

        for (int i = asciiLength; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch >= 0x80 || ch == 0) {
                length += ch >= 0x800 ? 2 : 1;
            }
        }

        return length;
    }

    private MUTF8() {
    }
}
