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
import org.glavo.nbt.internal.snbt.SNBTWriter;
import org.glavo.nbt.io.*;
import org.glavo.nbt.tag.Tag;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.Objects;

public record SNBTCodecImpl(
        LineBreakStrategy lineBreakStrategy,
        String indentation,
        SurroundingSpaces surroundingSpaces,
        EscapeStrategy escapeStrategy,
        QuoteStrategy nameQuoteStrategy,
        QuoteStrategy valueQuoteStrategy
) implements SNBTCodec {
    public static final SNBTCodecImpl COMPACT = new SNBTCodecImpl(
            LineBreakStrategy.never(),
            "", // No indentation
            SurroundingSpaces.COMPACT,
            EscapeStrategy.defaultStrategy(),
            QuoteStrategy.defaultNameStrategy(),
            QuoteStrategy.defaultValueStrategy()
    );

    public static final SNBTCodecImpl PRETTY = new SNBTCodecImpl(
            LineBreakStrategy.defaultStrategy(),
            "    ", // 4 spaces
            SurroundingSpaces.PRETTY,
            EscapeStrategy.defaultStrategy(),
            QuoteStrategy.defaultNameStrategy(),
            QuoteStrategy.defaultValueStrategy()
    );


    @Override
    public LineBreakStrategy getLineBreakStrategy() {
        return lineBreakStrategy;
    }

    @Override
    public SNBTCodec withLineBreakStrategy(LineBreakStrategy strategy) {
        Objects.requireNonNull(strategy, "strategy");
        return new SNBTCodecImpl(strategy, indentation, surroundingSpaces, escapeStrategy, nameQuoteStrategy, valueQuoteStrategy);
    }

    @Override
    public String getIndentation() {
        return indentation;
    }

    @Override
    public SNBTCodec withIndentation(String indentation) {
        for (int i = 0; i < indentation.length(); i++) { // implicit null check of indentation
            char ch = indentation.charAt(i);
            if (ch != ' ' && ch != '\t') {
                throw new IllegalArgumentException("Indentation must be a sequence of spaces or tabs");
            }
        }

        return new SNBTCodecImpl(lineBreakStrategy, indentation, surroundingSpaces, escapeStrategy, nameQuoteStrategy, valueQuoteStrategy);
    }

    @Override
    public SNBTCodec withIndentation(int spaces) {
        return new SNBTCodecImpl(lineBreakStrategy, " ".repeat(spaces), surroundingSpaces, escapeStrategy, nameQuoteStrategy, valueQuoteStrategy);
    }

    @Override
    public SurroundingSpaces getSurroundingSpaces() {
        return surroundingSpaces;
    }

    @Override
    public SNBTCodec withSurroundingSpaces(SurroundingSpaces surroundingSpaces) {
        Objects.requireNonNull(surroundingSpaces, "surroundingSpaces");
        return new SNBTCodecImpl(lineBreakStrategy, indentation, surroundingSpaces, escapeStrategy, nameQuoteStrategy, valueQuoteStrategy);
    }

    @Override
    public EscapeStrategy getEscapeStrategy() {
        return escapeStrategy;
    }

    @Override
    public SNBTCodec withEscapeStrategy(EscapeStrategy escapeStrategy) {
        Objects.requireNonNull(escapeStrategy, "escapeStrategy");
        return new SNBTCodecImpl(lineBreakStrategy, indentation, surroundingSpaces, escapeStrategy, nameQuoteStrategy, valueQuoteStrategy);
    }

    @Override
    public QuoteStrategy getNameQuoteStrategy() {
        return nameQuoteStrategy;
    }

    @Override
    public SNBTCodec withNameQuoteStrategy(QuoteStrategy quoteStrategy) {
        Objects.requireNonNull(quoteStrategy, "quoteStrategy");
        return new SNBTCodecImpl(lineBreakStrategy, indentation, surroundingSpaces, escapeStrategy, quoteStrategy, valueQuoteStrategy);
    }

    @Override
    public QuoteStrategy getValueQuoteStrategy() {
        return valueQuoteStrategy;
    }

    @Override
    public SNBTCodec withValueQuoteStrategy(QuoteStrategy quoteStrategy) {
        Objects.requireNonNull(quoteStrategy, "quoteStrategy");
        return new SNBTCodecImpl(lineBreakStrategy, indentation, surroundingSpaces, escapeStrategy, nameQuoteStrategy, quoteStrategy);
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

    @Override
    public void writeTag(Appendable appendable, Tag tag) throws IOException {
        new SNBTWriter(this, appendable).writeTag(tag);
    }
}
