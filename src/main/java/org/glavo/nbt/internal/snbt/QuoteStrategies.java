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
package org.glavo.nbt.internal.snbt;

import org.glavo.nbt.io.QuoteStrategy;

public final class QuoteStrategies {

    public static final QuoteStrategy DEFAULT = new Smart(false, '"');

    static char getAnotherQuoteChar(char quoteChar) {
        assert quoteChar == '"' || quoteChar == '\'';

        return quoteChar == '"' ? '\'' : '"';
    }

    public record Always(char quoteChar) implements QuoteStrategy {
        public static final Always DOUBLE_QUOTE = new Always('"');
        public static final Always SINGLE_QUOTE = new Always('\'');

        @Override
        public char getQuoteChar(String value) {
            return quoteChar;
        }
    }

    public record Smart(boolean quoteByDefault, char preferredQuoteChar) implements QuoteStrategy {
        private static boolean isSimpleChar(boolean isFirst, char c) {
            return isFirst
                    ? c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_'
                    : c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '_' || c == '+' || c == '-';
        }

        @Override
        public char getQuoteChar(String value) {
            if (value.isEmpty()) {
                return preferredQuoteChar;
            }

            int scanBegin = 0;
            if (!quoteByDefault) {
                int i = 0;
                while (i < value.length()) {
                    char ch = value.charAt(i);
                    if (!isSimpleChar(i == 0, ch)) {
                        break;
                    }
                    i++;
                }

                if (i == value.length()) {
                    return '\0';
                }

                scanBegin = i;
            }

            char anotherQuoteChar = getAnotherQuoteChar(preferredQuoteChar);

            boolean containsPreferred = false;
            boolean containsAnother = false;

            for (int i = scanBegin; i < value.length(); i++) {
                char ch = value.charAt(i);
                if (ch == preferredQuoteChar) {
                    containsPreferred = true;
                } else if (ch == anotherQuoteChar) {
                    containsAnother = true;
                    break;
                }
            }

            if (!containsPreferred || containsAnother) {
                return preferredQuoteChar;
            } else {
                return anotherQuoteChar;
            }
        }
    }

    private QuoteStrategies() {
    }
}
