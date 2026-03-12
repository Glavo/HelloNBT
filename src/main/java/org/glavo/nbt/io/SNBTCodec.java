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

import org.glavo.nbt.internal.SNBTCodecImpl;
import org.glavo.nbt.tag.Tag;
import org.jetbrains.annotations.Contract;

import java.io.IOException;

/// The codec for reading and writing Stringified NBT data.
///
/// Each SNBTCodec instance is immutable, thread-safe, and can be safely used by multiple threads.
public sealed interface SNBTCodec permits SNBTCodecImpl {

    /// Returns the default [SNBTCodec].
    static SNBTCodec of() {
        return SNBTCodecImpl.PRETTY;
    }

    /// Returns a [SNBTCodec] with compact formatting.
    static SNBTCodec ofCompact() {
        return SNBTCodecImpl.COMPACT;
    }

    /// Returns the line break strategy for all parent tags.
    @Contract(pure = true)
    LineBreakStrategy getLineBreakStrategy();

    /// Returns a new codec with the specified line break strategy for all parent tags.
    ///
    /// This method is a shortcut for calling [withCompoundTagLineBreakStrategy][#withCompoundTagLineBreakStrategy()],
    /// [withListTagLineBreakStrategy][#withListTagLineBreakStrategy()], and [withArrayTagLineBreakStrategy][#withArrayTagLineBreakStrategy()]
    /// with the same strategy.
    @Contract(value = "_ -> new", pure = true)
    SNBTCodec withLineBreakStrategy(LineBreakStrategy strategy);

    /// Returns the indentation string before each line.
    @Contract(pure = true)
    String getIndentation();

    /// Returns a new codec with the specified indentation string.
    ///
    /// The indentation string must be a sequence of spaces or tabs.
    ///
    /// @see IllegalArgumentException if the indentation string is not a sequence of spaces or tabs.
    @Contract(value = "_ -> new", pure = true)
    SNBTCodec withIndentation(String indentation);

    /// Returns a new codec with the specified indentation.
    ///
    /// The indentation is a sequence of spaces.
    ///
    /// @see IllegalArgumentException if the indentation is not a positive integer.
    @Contract(value = "_ -> new", pure = true)
    SNBTCodec withIndentation(int spaces);

    /// Returns the surrounding spaces for SNBT.
    @Contract(pure = true)
    SurroundingSpaces getSurroundingSpaces();

    /// Returns a new codec with the specified surrounding spaces.
    @Contract(value = "_ -> new", pure = true)
    SNBTCodec withSurroundingSpaces(SurroundingSpaces surroundingSpaces);


    /// Returns the escape strategy for SNBT.
    @Contract(pure = true)
    EscapeStrategy getEscapeStrategy();

    /// Returns a new codec with the specified escape strategy.
    @Contract(value = "_ -> new", pure = true)
    SNBTCodec withEscapeStrategy(EscapeStrategy escapeStrategy);

    /// Returns the quote strategy for SNBT tag names.
    @Contract(pure = true)
    QuoteStrategy getNameQuoteStrategy();

    /// Returns a new codec with the specified quote strategy for tag names.
    @Contract(value = "_ -> new", pure = true)
    SNBTCodec withNameQuoteStrategy(QuoteStrategy quoteStrategy);

    /// Returns the quote strategy for SNBT tag values.
    @Contract(pure = true)
    QuoteStrategy getValueQuoteStrategy();

    /// Returns a new codec with the specified quote strategy for tag values.
    @Contract(value = "_ -> new", pure = true)
    SNBTCodec withValueQuoteStrategy(QuoteStrategy quoteStrategy);

    /// Reads a NBT tag from the Stringified NBT data.
    ///
    /// @throws IOException if the input is not a valid Stringified NBT data.
    @Contract(pure = true)
    default Tag readTag(CharSequence input) throws IOException {
        return readTag(input, 0, input.length());
    }

    /// Reads a NBT tag from the Stringified NBT data.
    ///
    /// @throws IndexOutOfBoundsException if the range is out of bounds.
    /// @throws IOException               if the input is not a valid Stringified NBT data.
    @Contract(pure = true)
    Tag readTag(CharSequence input, int startInclusive, int endExclusive) throws IOException;

    /// Reads a NBT tag from the Stringified NBT data.
    ///
    /// @throws IOException if an I/O error occurs.
    @Contract(mutates = "param1")
    Tag readTag(Readable readable) throws IOException;

    /// Writes a NBT tag as Stringified NBT to the given [Appendable].
    ///
    /// @throws IOException if an I/O error occurs.
    @Contract(mutates = "param1")
    void writeTag(Appendable appendable, Tag tag) throws IOException;

    /// Writes a NBT tag as Stringified NBT to the given [StringBuilder].
    @Contract(mutates = "param1")
    default void writeTag(StringBuilder builder, Tag tag) {
        try {
            writeTag((Appendable) builder, tag);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /// Returns the Stringified NBT of the tag.
    default String toString(Tag tag) {
        var builder = new StringBuilder();
        writeTag(builder, tag);
        return builder.toString();
    }
}
