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
import org.glavo.nbt.tag.*;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Objects;
import java.util.UUID;

public final class SNBTParser {
    public static final @Unmodifiable CompoundTag EMPTY_COMPOUND_TAG = new CompoundTag();

    private final CharSequence input;
    private final int endIndex;

    @VisibleForTesting
    int cursor;

    private @Nullable StringBuilder buffer;

    private @Nullable Token lookahead;

    public SNBTParser(CharSequence input, int beginIndex, int endIndex) {
        Objects.checkFromToIndex(beginIndex, endIndex, input.length());
        this.input = input;
        this.endIndex = endIndex;

        this.cursor = beginIndex;
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

    private static boolean isUnquotedStringPart(int ch) {
        return TextUtils.isAsciiDigit(ch)
                || ch >= 'a' && ch <= 'z'
                || ch >= 'A' && ch <= 'Z'
                || ch == '_'
                || ch == '-' || ch == '+'
                || ch == '.';
    }

    Token readNextToken() {
        skipWhiteSpace();

        if (cursor >= endIndex) {
            return Token.SimpleToken.EOF;
        }

        int firstChar = getCodePoint();
        int firstCharCursor = cursor;

        cursor += Character.charCount(firstChar);

        Token token = switch (firstChar) {
            case '(' -> Token.SimpleToken.LEFT_PARENTHESES;
            case ')' -> Token.SimpleToken.RIGHT_PARENTHESES;
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
            case '.' -> cursor >= endIndex || !TextUtils.isAsciiDigit(input.charAt(cursor))
                    ? Token.SimpleToken.DOT
                    : null;  // Floating point number
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

                    return new Token.StringToken(input.subSequence(firstCharCursor + 1, cursor - chCount).toString(), true);
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

                            int end = TextUtils.indexOf(input, cursor, endIndex, '}');
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
                        default -> builder.appendCodePoint(ch0);

                    }
                } else {
                    cursor += Character.charCount(ch);
                    builder.appendCodePoint(ch);
                }
            }

            throw new IllegalArgumentException("Unexpected end of string literal: " + input.subSequence(firstCharCursor, cursor));
        } else if (isUnquotedStringPart(firstChar)) {
            // Scan number
            while (cursor < endIndex) {
                char ch = input.charAt(cursor);
                if (isUnquotedStringPart(ch)) {
                    cursor += 1;
                } else {
                    break;
                }
            }

            if (TextUtils.isAsciiDigit(firstChar)
                    || firstChar == '-' || firstChar == '+' || firstChar == '.') {
                return Token.NumberToken.parse(input, firstCharCursor, cursor);
            } else {
                String value = input.subSequence(firstCharCursor, cursor).toString();
                return switch (value) {
                    case "true" -> Token.BooleanToken.TRUE;
                    case "false" -> Token.BooleanToken.FALSE;
                    default -> new Token.StringToken(value, false);
                };
            }
        } else {
            throw new IllegalArgumentException("Unexpected character: " + new String(Character.toChars(firstChar)));
        }
    }

    Token nextToken() {
        if (lookahead != null) {
            Token token = lookahead;
            lookahead = null;
            return token;
        }
        return readNextToken();
    }

    void nextToken(Token expected) {
        Token token = nextToken();
        if (token != expected) {
            throw new IllegalArgumentException("Expected " + expected + ", but got " + token);
        }
    }

    <T extends Token> T nextToken(Class<T> expected) {
        Token token = nextToken();
        try {
            return expected.cast(token);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Expected " + expected + ", but got " + token, e);
        }
    }

    Token peekToken() {
        if (lookahead == null) {
            lookahead = readNextToken();
        }
        return lookahead;
    }

    void discardPeekedToken(Token token) {
        if (lookahead != token) {
            throw new AssertionError("Expected " + token + ", but got " + lookahead);
        }
        lookahead = null;
    }

    public @Nullable Tag nextTag() throws IllegalArgumentException {
        Token next = peekToken();
        if (next == Token.SimpleToken.EOF) {
            return null;
        }

        if (next == Token.SimpleToken.LEFT_BRACE) {
            return nextCompoundTag();
        } else if (next == Token.SimpleToken.LEFT_BRACKET) {
            return nextListTag();
        } else if (next instanceof Token.ArrayBeginToken) {
            return nextArrayTag();
        } else if (next instanceof Token.NumberToken numberToken) {
            discardPeekedToken(numberToken);
            return numberToken.toTag();
        } else if (next instanceof Token.StringToken stringToken) {
            discardPeekedToken(stringToken);
            if (!stringToken.quoted() && peekToken() == Token.SimpleToken.LEFT_PARENTHESES) {
                discardPeekedToken(Token.SimpleToken.LEFT_PARENTHESES);

                Token valueToken = nextToken();

                Tag tag;
                if (stringToken.equals(Token.StringToken.OP_UUID)) {
                    if (valueToken instanceof Token.StringToken valueStringToken) {
                        tag = new IntArrayTag("", UUID.fromString(valueStringToken.value()));
                    } else {
                        throw new IllegalArgumentException("Unexpected token: " + valueToken);
                    }
                } else if (stringToken.equals(Token.StringToken.OP_BOOL)) {
                    if (valueToken instanceof Token.BooleanToken booleanToken) {
                        tag = new ByteTag("", booleanToken.value);
                    } else if (valueToken instanceof Token.IntegralToken integralToken) {
                        tag = new ByteTag("", integralToken.value() != 0L);
                    } else {
                        throw new IllegalArgumentException("Unexpected token: " + valueToken);
                    }
                } else {
                    throw new IllegalArgumentException("Unsupported operator: " + stringToken.value());
                }

                nextToken(Token.SimpleToken.RIGHT_PARENTHESES);

                return tag;
            }

            return new StringTag("", stringToken.value());
        } else if (next instanceof Token.BooleanToken booleanToken) {
            discardPeekedToken(booleanToken);
            return new ByteTag("", booleanToken.value);
        } else {
            throw new IllegalArgumentException("Unexpected token: " + next);
        }
    }

    private CompoundTag nextCompoundTag() throws IllegalArgumentException {
        nextToken(Token.SimpleToken.LEFT_BRACE);

        var tag = new CompoundTag();
        while (true) {
            if (peekToken() == Token.SimpleToken.RIGHT_BRACE) {
                discardPeekedToken(Token.SimpleToken.RIGHT_BRACE);
                break;
            }

            Token.StringToken nameToken = nextToken(Token.StringToken.class);
            nextToken(Token.SimpleToken.COLON);

            Tag value = nextTag();
            if (value == null) {
                throw new IllegalArgumentException("Unexpected end of input");
            }

            tag.put(nameToken.value(), value);

            Token peek = peekToken();
            if (peek == Token.SimpleToken.COMMA) {
                discardPeekedToken(peek);
            } else if (peek == Token.SimpleToken.RIGHT_BRACE) {
                discardPeekedToken(peek);
                break;
            } else {
                throw new IllegalArgumentException("Unexpected token: " + peek);
            }
        }

        return tag;
    }

    private ListTag<?> nextListTag() throws IllegalArgumentException {
        nextToken(Token.SimpleToken.LEFT_BRACKET);

        var tag = new ListTag<>();

        while (true) {
            Token peek = peekToken();
            if (peek == Token.SimpleToken.RIGHT_BRACKET) {
                discardPeekedToken(peek);
                return tag;
            }

            Tag value = nextTag();
            if (value == null) {
                throw new IllegalArgumentException("Unexpected end of input");
            }
            tag.addAny(value);

            peek = peekToken();
            if (peek == Token.SimpleToken.COMMA) {
                discardPeekedToken(peek);
            } else if (peek == Token.SimpleToken.RIGHT_BRACKET) {
                discardPeekedToken(peek);
                break;
            } else {
                throw new IllegalArgumentException("Unexpected token: " + peek);
            }
        }

        return tag;
    }

    private ArrayTag<?, ?, ?> nextArrayTag() throws IllegalArgumentException {
        Token firstToken = nextToken();
        if (!(firstToken instanceof Token.ArrayBeginToken arrayBeginToken)) {
            throw new IllegalArgumentException("Unexpected token: " + firstToken);
        }

        var builder = switch (arrayBeginToken) {
            case BYTE_ARRAY -> new PrimaryArrayBuilder.OfByte();
            case INT_ARRAY -> new PrimaryArrayBuilder.OfInt();
            case LONG_ARRAY -> new PrimaryArrayBuilder.OfLong();
        };

        while (true) {
            Token peek = peekToken();
            if (peek == Token.SimpleToken.RIGHT_BRACKET) {
                discardPeekedToken(peek);
                break;
            } else if (peek instanceof Token.IntegralToken integralToken) {
                discardPeekedToken(integralToken);
                builder.add(integralToken);

                peek = peekToken();
                if (peek == Token.SimpleToken.COMMA) {
                    discardPeekedToken(peek);
                } else if (peek == Token.SimpleToken.RIGHT_BRACKET) {
                    discardPeekedToken(peek);
                    break;
                } else {
                    throw new IllegalArgumentException("Unexpected token: " + peek);
                }
            } else {
                throw new IllegalArgumentException("Unexpected token: " + peek);
            }
        }

        return builder.build();
    }
}