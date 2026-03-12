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

import org.glavo.nbt.internal.snbt.SNBTParser;
import org.glavo.nbt.io.EscapeStrategy;
import org.glavo.nbt.io.LineBreakStrategy;
import org.glavo.nbt.io.SNBTCodec;
import org.glavo.nbt.io.SurroundingSpaces;
import org.glavo.nbt.tag.Tag;

import java.io.IOException;
import java.nio.CharBuffer;

public record SNBTCodecImpl(
        LineBreakStrategy compoundTagLineBreakStrategy,
        LineBreakStrategy listTagLineBreakStrategy,
        LineBreakStrategy arrayTagLineBreakStrategy,
        String indentation,
        SurroundingSpaces surroundingSpaces,
        EscapeStrategy escapeStrategy
) implements SNBTCodec {
    public static final SNBTCodecImpl COMPACT = new SNBTCodecImpl(
            LineBreakStrategy.NEVER,
            LineBreakStrategy.NEVER,
            LineBreakStrategy.NEVER,
            "", // No indentation
            SurroundingSpaces.COMPACT,
            EscapeStrategy.defaultStrategy()
    );

    public static final SNBTCodecImpl PRETTY = new SNBTCodecImpl(
            LineBreakStrategy.ALWAYS,
            LineBreakStrategy.ALWAYS,
            LineBreakStrategy.ALWAYS,
            "    ", // 4 spaces
            SurroundingSpaces.PRETTY,
            EscapeStrategy.defaultStrategy()
    );


    @Override
    public SNBTCodec withLineBreakStrategy(LineBreakStrategy strategy) {
        return new SNBTCodecImpl(strategy, strategy, strategy, indentation, surroundingSpaces, escapeStrategy);
    }

    @Override
    public LineBreakStrategy getCompoundTagLineBreakStrategy() {
        return compoundTagLineBreakStrategy;
    }

    @Override
    public SNBTCodec withCompoundTagLineBreakStrategy(LineBreakStrategy strategy) {
        return new SNBTCodecImpl(strategy, listTagLineBreakStrategy, arrayTagLineBreakStrategy, indentation, surroundingSpaces, escapeStrategy);
    }

    @Override
    public LineBreakStrategy getListTagLineBreakStrategy() {
        return listTagLineBreakStrategy;
    }

    @Override
    public SNBTCodec withListTagLineBreakStrategy(LineBreakStrategy strategy) {
        return new SNBTCodecImpl(compoundTagLineBreakStrategy, strategy, arrayTagLineBreakStrategy, indentation, surroundingSpaces, escapeStrategy);
    }

    @Override
    public LineBreakStrategy getArrayTagLineBreakStrategy() {
        return arrayTagLineBreakStrategy;
    }

    @Override
    public SNBTCodec withArrayTagLineBreakStrategy(LineBreakStrategy strategy) {
        return new SNBTCodecImpl(compoundTagLineBreakStrategy, listTagLineBreakStrategy, strategy, indentation, surroundingSpaces, escapeStrategy);
    }

    @Override
    public String getIndentation() {
        return indentation;
    }

    @Override
    public SNBTCodec withIndentation(String indentation) {
        for (int i = 0; i < indentation.length(); i++) {
            char ch = indentation.charAt(i);
            if (ch != ' ' && ch != '\t') {
                throw new IllegalArgumentException("Indentation must be a sequence of spaces or tabs");
            }
        }

        return new SNBTCodecImpl(compoundTagLineBreakStrategy, listTagLineBreakStrategy, arrayTagLineBreakStrategy, indentation, surroundingSpaces, escapeStrategy);
    }

    @Override
    public SNBTCodec withIndentation(int spaces) {
        return new SNBTCodecImpl(compoundTagLineBreakStrategy, listTagLineBreakStrategy, arrayTagLineBreakStrategy, " ".repeat(spaces), surroundingSpaces, escapeStrategy);
    }

    @Override
    public SurroundingSpaces getSurroundingSpaces() {
        return surroundingSpaces;
    }

    @Override
    public SNBTCodec withSurroundingSpaces(SurroundingSpaces surroundingSpaces) {
        return new SNBTCodecImpl(compoundTagLineBreakStrategy, listTagLineBreakStrategy, arrayTagLineBreakStrategy, indentation, surroundingSpaces, escapeStrategy);
    }

    @Override
    public EscapeStrategy getEscapeStrategy() {
        return escapeStrategy;
    }

    @Override
    public SNBTCodec withEscapeStrategy(EscapeStrategy escapeStrategy) {
        return new SNBTCodecImpl(compoundTagLineBreakStrategy, listTagLineBreakStrategy, arrayTagLineBreakStrategy, indentation, surroundingSpaces, escapeStrategy);
    }

    @Override
    public Tag readTag(CharSequence input, int startInclusive, int endExclusive) throws IOException {
        Tag tag;
        try {
            tag = new SNBTParser(input, startInclusive, endExclusive).nextTag();
        } catch (IllegalArgumentException e) {
            throw new IOException(e);
        }
        if (tag == null) {
            throw new IOException("Unexpected end of input");
        }
        return tag;
    }

    @Override
    public Tag readTag(Readable readable) throws IOException {
        var builder = new StringBuilder();

        CharBuffer buffer = CharBuffer.allocate(8192);
        while (readable.read(buffer) != -1) {
            buffer.flip();
            builder.append(buffer);
            buffer.clear();
        }
        try {
            return readTag(builder);
        } catch (IllegalArgumentException e) {
            throw new IOException(e);
        }
    }
}
