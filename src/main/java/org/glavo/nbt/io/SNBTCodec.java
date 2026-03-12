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

    /// Returns a new codec with the specified line break strategy for all parent tags.
    ///
    /// This method is a shortcut for calling [withCompoundTagLineBreakStrategy][#withCompoundTagLineBreakStrategy()],
    /// [withListTagLineBreakStrategy][#withListTagLineBreakStrategy()], and [withArrayTagLineBreakStrategy][#withArrayTagLineBreakStrategy()]
    /// with the same strategy.
    @Contract(value = "_ -> new", pure = true)
    SNBTCodec withLineBreakStrategy(LineBreakStrategy strategy);

    /// Returns the line break strategy for compound tags.
    ///
    /// If not specified, [the default line break strategy][#getDefaultLineBreakStrategy()] is used.
    @Contract(pure = true)
    LineBreakStrategy getCompoundTagLineBreakStrategy();

    /// Returns a new codec with the specified line break strategy for compound tags.
    @Contract(value = "_ -> new", pure = true)
    SNBTCodec withCompoundTagLineBreakStrategy(LineBreakStrategy strategy);

    /// Returns the line break strategy for list tags.
    ///
    /// If not specified, [the default line break strategy][#getDefaultLineBreakStrategy()] is used.
    @Contract(pure = true)
    LineBreakStrategy getListTagLineBreakStrategy();

    /// Returns a new codec with the specified line break strategy for list tags.
    @Contract(value = "_ -> new", pure = true)
    SNBTCodec withListTagLineBreakStrategy(LineBreakStrategy strategy);

    /// Returns the line break strategy for array tags.
    ///
    /// If not specified, [the default line break strategy][#getDefaultLineBreakStrategy()] is used.
    @Contract(pure = true)
    LineBreakStrategy getArrayTagLineBreakStrategy();

    /// Returns a new codec with the specified line break strategy for array tags.
    @Contract(value = "_ -> new", pure = true)
    SNBTCodec withArrayTagLineBreakStrategy(LineBreakStrategy strategy);

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

    /// Returns the quote strategy for SNBT.
    @Contract(pure = true)
    QuoteStrategy getQuoteStrategy();

    /// Returns a new codec with the specified quote strategy.
    @Contract(value = "_ -> new", pure = true)
    SNBTCodec withQuoteStrategy(QuoteStrategy quoteStrategy);

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
}
