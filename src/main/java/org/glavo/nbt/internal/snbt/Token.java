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

import org.jetbrains.annotations.Nullable;

sealed interface Token {
    enum SimpleToken implements Token {
        LEFT_BRACE,     // {
        RIGHT_BRACE,    // }
        LEFT_BRACKET,   // [
        RIGHT_BRACKET,  // ]
        COMMA,          // ,
        COLON,          // :
        DOT,            // .
        EOF
    }

    enum ArrayBeginToken implements Token {
        BYTE_ARRAY,
        INT_ARRAY,
        LONG_ARRAY,
    }

    enum BooleanToken implements Token {
        TRUE(true),
        FALSE(false);

        private final boolean value;

        BooleanToken(boolean value) {
            this.value = value;
        }
    }

    record StringToken(String value) implements Token {
    }


    sealed interface NumberToken extends Token {
        static NumberToken parse(String value) {
            if (value.endsWith("_")) {
                throw new IllegalArgumentException("Invalid number literal: " + value);
            }

            String clean = value.replaceAll("_", ""); // Remove underscores
            if (clean.isEmpty()) {
                throw new IllegalArgumentException("Invalid number literal: " + value);
            }

            if (clean.indexOf('.') >= 0 || clean.indexOf('e') >= 0 || clean.indexOf('E') >= 0) {
                int endIndex = clean.length();

                FloatingType floatingType;
                if (clean.endsWith("f") || clean.endsWith("F")) {
                    floatingType = FloatingType.FLOAT;
                    endIndex -= 1;
                } else if (clean.endsWith("d") || clean.endsWith("D")) {
                    floatingType = FloatingType.DOUBLE;
                    endIndex -= 1;
                } else {
                    floatingType = FloatingType.DOUBLE;
                }

                return new Token.FloatingToken(Double.parseDouble(clean.substring(0, endIndex)), floatingType);
            } else {
                int beginIndex;
                int radix;
                if (clean.startsWith("0x") || clean.startsWith("0X")) {
                    beginIndex = 2;
                    radix = 16;
                } else if (clean.startsWith("0b") || clean.startsWith("0B")) {
                    beginIndex = 2;
                    radix = 2;
                } else {
                    beginIndex = 0;
                    radix = 10;
                }

                int endIndex = clean.length();

                @Nullable
                Boolean unsignedSuffixChar = endIndex - beginIndex > 2 ? switch (clean.charAt(endIndex - 2)) {
                    case 's', 'S' -> false;
                    case 'u', 'U' -> true;
                    default -> null;
                } : null;

                @Nullable
                IntegralType typeSuffixChar = endIndex - beginIndex > 1 ? switch (clean.charAt(endIndex - 1)) {
                    case 'b', 'B' -> radix != 16 || unsignedSuffixChar != null ? IntegralType.BYTE : null;
                    case 's', 'S' -> IntegralType.SHORT;
                    case 'i', 'I' -> IntegralType.INT;
                    case 'l', 'L' -> IntegralType.LONG;
                    default -> null;
                } : null;

                if (typeSuffixChar != null) {
                    endIndex -= 1;
                }
                if (unsignedSuffixChar != null) {
                    endIndex -= 1;
                }

                boolean unsigned = unsignedSuffixChar != null ? unsignedSuffixChar : radix != 10;
                IntegralType type = typeSuffixChar != null ? typeSuffixChar : IntegralType.INT;

                try {
                    long parsed = unsigned
                            ? Long.parseUnsignedLong(clean, beginIndex, endIndex, radix)
                            : Long.parseLong(clean, beginIndex, endIndex, radix);
                    type.check(parsed, unsigned);
                    return new Token.IntegralToken(parsed, type, unsigned);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid number literal: " + value, e);
                }
            }

        }

    }

    record IntegralToken(long value,
                         IntegralType type,
                         boolean unsigned) implements NumberToken {

    }

    record FloatingToken(double value, FloatingType type) implements NumberToken {

    }
}
