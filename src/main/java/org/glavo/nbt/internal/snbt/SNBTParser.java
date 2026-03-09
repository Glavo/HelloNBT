/*
 * Hello Minecraft! Launcher
 * Copyright (C) 2026 huangyuhui <huanghongxun2008@126.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.glavo.nbt.internal.snbt;

import org.glavo.nbt.internal.TextUtils;
import org.glavo.nbt.tag.CompoundTag;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Objects;

public final class SNBTParser {
    public static final @Unmodifiable CompoundTag EMPTY_COMPOUND_TAG = new CompoundTag();

    private final CharSequence input;
    private final int beginIndex;
    private final int endIndex;

    private int cursor;

    private @Nullable StringBuilder buffer;

    public SNBTParser(CharSequence input, int beginIndex, int endIndex) {
        Objects.checkFromToIndex(beginIndex, endIndex, input.length());
        this.input = input;
        this.beginIndex = beginIndex;
        this.endIndex = endIndex;
    }

    private int getCodePoint() {
        if (input instanceof String string) {
            return string.codePointAt(cursor);
        } else {
            char c0 = input.charAt(cursor);
            if (Character.isHighSurrogate(c0) && cursor < endIndex - 1) {
                char c1 = input.charAt(cursor + 1);
                if (Character.isLowSurrogate(c1)) {
                    return Character.toCodePoint(c0, c1);
                }
            }
            return c0;
        }
    }

    private int indexOf(char ch) {
        for (int i = cursor; i < endIndex; i++) {
            if (input.charAt(i) == ch) {
                return i;
            }
        }
        return -1;
    }

    private StringBuilder getBuilder() {
        if (buffer == null) {
            buffer = new StringBuilder();
        } else {
            buffer.setLength(0);
        }
        return buffer;
    }

    private void skipWhiteSpace() {
        while (cursor < endIndex) {
            int ch = getCodePoint();
            if (Character.isWhitespace(ch)) {
                cursor += Character.charCount(ch);
            } else {
                break;
            }
        }
    }

    private void ensureNotEOF() {
        if (cursor >= endIndex) {
            throw new IllegalArgumentException("Unexpected end of input");
        }
    }

    private void ensureNotEOF(int expected) {
        if (cursor >= endIndex - expected) {
            throw new IllegalArgumentException("Unexpected end of input");
        }
    }

    private Token nextToken() {
        skipWhiteSpace();

        if (cursor >= endIndex) {
            return Token.SimpleToken.EOF;
        }

        int firstChar = getCodePoint();
        int firstCharCursor = cursor;

        cursor += Character.charCount(firstChar);

        Token token = switch (firstChar) {
            case '{' -> Token.SimpleToken.LEFT_BRACE;
            case '}' -> Token.SimpleToken.RIGHT_BRACE;
            case '[' -> {
                if (cursor < endIndex - 1 && input.charAt(cursor + 1) == ';') {
                    Token.ArrayBeginToken arrayBeginToken = switch (input.charAt(cursor)) {
                        case 'B' -> Token.ArrayBeginToken.BYTE_ARRAY;
                        case 'I' -> Token.ArrayBeginToken.INT_ARRAY;
                        case 'L' -> Token.ArrayBeginToken.LONG_ARRAY;
                        default -> null;
                    };
                    if (arrayBeginToken != null) {
                        cursor += 2;
                        yield arrayBeginToken;
                    }
                }

                yield Token.SimpleToken.LEFT_BRACKET;
            }
            case ']' -> Token.SimpleToken.RIGHT_BRACKET;
            case ',' -> Token.SimpleToken.COMMA;
            case ':' -> Token.SimpleToken.COLON;
            default -> null;
        };

        if (token != null) {
            return token;
        }

        if (firstChar == '\'' || firstChar == '"') {
            // Scan string prefix without escape
            while (cursor < endIndex) {
                int ch = getCodePoint();
                int chCount = Character.charCount(ch);
                if (ch == firstChar) {
                    cursor += chCount;

                    return new Token.StringToken(input.subSequence(firstCharCursor, cursor - chCount).toString(), true);
                } else if (ch == '\\') {
                    break;
                } else {
                    cursor += Character.charCount(ch);
                }
            }

            StringBuilder builder = getBuilder();
            builder.append(input, firstCharCursor + 1, cursor);

            while (cursor < endIndex) {
                int ch = getCodePoint();
                if (ch == firstChar) {
                    cursor += Character.charCount(ch);
                    return new Token.StringToken(builder.toString(), true);
                } else if (ch == '\\') {
                    cursor += 1;
                    ensureNotEOF();

                    int ch0 = getCodePoint();
                    cursor += Character.charCount(ch0);
                    switch (ch0) {
                        case 'b' -> builder.append('\b');
                        case 'f' -> builder.append('\f');
                        case 'n' -> builder.append('\n');
                        case 'r' -> builder.append('\r');
                        case 's' -> builder.append(' ');
                        case 't' -> builder.append('\t');
                        case 'x' -> {
                            ensureNotEOF(2);

                            try {
                                builder.append((char) Integer.parseUnsignedInt(input, cursor, cursor + 2, 16));
                            } catch (NumberFormatException e) {
                                throw new IllegalArgumentException("Invalid hex escape sequence: \\x" + input.subSequence(cursor, cursor + 2), e);
                            }

                            cursor += 2;
                        }
                        case 'u' -> {
                            ensureNotEOF(4);
                            try {
                                builder.append((char) Integer.parseUnsignedInt(input, cursor, cursor + 4, 16));
                            } catch (NumberFormatException e) {
                                throw new IllegalArgumentException("Invalid Unicode escape sequence: \\u" + input.subSequence(cursor, cursor + 4), e);
                            }
                            cursor += 4;
                        }
                        case 'U' -> {
                            ensureNotEOF(8);
                            try {
                                builder.appendCodePoint(Integer.parseUnsignedInt(input, cursor, cursor + 8, 16));
                            } catch (NumberFormatException e) {
                                throw new IllegalArgumentException("Invalid Unicode escape sequence: \\U" + input.subSequence(cursor, cursor + 8), e);
                            }
                            cursor += 8;
                        }
                        case 'N' -> {
                            ensureNotEOF(2);
                            if (input.charAt(cursor) != '{') {
                                throw new IllegalArgumentException("Invalid named Unicode escape sequence: \\N" + input.charAt(cursor));
                            }

                            cursor += 1;

                            int end = indexOf('}');
                            if (end < 0) {
                                throw new IllegalArgumentException("Unexpected end of named Unicode escape sequence: \\N");
                            }
                            if (end == cursor + 1) {
                                throw new IllegalArgumentException("Empty named Unicode escape sequence at " + (cursor - 2));
                            }

                            String name = input.subSequence(cursor, end).toString();
                            int cp;
                            try {
                                cp = Character.codePointOf(name);
                            } catch (IllegalArgumentException e) {
                                throw new IllegalArgumentException("Invalid named Unicode escape sequence: \\N{" + name + "}", e);
                            }
                            builder.appendCodePoint(cp);
                            cursor = end + 1;
                        }
                        default -> builder.appendCodePoint(ch);

                    }
                } else {
                    cursor += Character.charCount(ch);
                    builder.appendCodePoint(ch);
                }
            }
        } else if (TextUtils.isAsciiDigit(firstChar)
                || firstChar == '-' || firstChar == '+'
                || (firstChar == '.' && cursor < endIndex && TextUtils.isAsciiDigit(input.charAt(cursor)))) {
            // Scan number
            while (cursor < endIndex) {
                char ch = input.charAt(cursor);
                if (TextUtils.isAsciiDigit(ch)
                        || ch >= 'a' && ch <= 'z'
                        || ch >= 'A' && ch <= 'Z'
                        || ch == '.' || ch == '+' || ch == '-' || ch == '_') {
                    cursor += 1;
                } else {
                    break;
                }
            }

            return Token.NumberToken.parse(input, firstCharCursor, cursor);
        }

        throw new UnsupportedOperationException("Not implemented"); // TODO
    }

//    public @Nullable Tag nextTag(boolean immutable) {
//        skipWhiteSpace();
//
//        if (cursor >= endIndex) {
//            return null;
//        }
//
//        int ch = getCodePoint();
//        if (ch == '{') {
//            cursor += 1;
//            skipWhiteSpace();
//
//            if (immutable && getCodePoint() == '}') {
//                cursor += 1;
//                return EMPTY_COMPOUND_TAG;
//            }
//
//
//        }
//    }

}