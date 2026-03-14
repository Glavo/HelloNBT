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
/// Each SNBTCodec instance is immutable and thread-safe.
///
/// # Getting SNBTCodec Instances
///
/// SNBTCodec provides two factory methods to obtain SNBTCodec instances:
///
/// - [SNBTCodec#of()] returns the default [SNBTCodec] with pretty formatting.
/// - [SNBTCodec#ofCompact()] returns a [SNBTCodec] with compact formatting.
///
/// They are identical when reading SNBT, but when writing SNBT, the former adds line breaks and indentation, while the latter does not.
///
/// SNBTCodec also provides many options to control the output format, such as indentation, line breaks, quotes, etc.
/// After obtaining an SNBTCodec instance, you can use various `withXxx` methods to modify these options:
///
/// - [SNBTCodec#withLineBreakStrategy(LineBreakStrategy)]:
/// - [SNBTCodec#withIndentation(String)]
/// - [SNBTCodec#withSurroundingSpaces(SurroundingSpaces)]
/// - [SNBTCodec#withEscapeStrategy(EscapeStrategy)]
/// - [SNBTCodec#withNameQuoteStrategy(QuoteStrategy)]
/// - [SNBTCodec#withValueQuoteStrategy(QuoteStrategy)]
///
/// # Reading and Writing SNBT
///
/// SNBTCodec supports reading and writing Stringified NBT data:
///
/// ```java
/// var codec = SNBTCodec.of();
///
/// String snbt;
///
/// // Read from a CharSequence
/// snbt = codec.readTag("...");
///
/// // Read from a Readable
/// try (var reader = Files.newBufferedReader(Path.of("/path/to/file"))) {
///     snbt = codec.readTag(reader);
/// }
///
/// // Write to a Appendable
/// codec.writeTag(new StringBuilder(), tag);
/// ```
///
/// You can also convert a Tag to an SNBT formatted string like this:
///
/// ```java
/// String snbt = codec.toString(tag);
/// ```
///
/// @see <a href="https://minecraft.wiki/w/NBT_format#SNBT_format">NBT format - Minecraft Wiki</a>
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
    ///
    /// @see #withLineBreakStrategy(LineBreakStrategy)
    /// @see LineBreakStrategy
    @Contract(pure = true)
    LineBreakStrategy getLineBreakStrategy();

    /// Returns a new codec with the specified line break strategy for all parent tags.
    ///
    /// This method is a shortcut for calling [withCompoundTagLineBreakStrategy][#withCompoundTagLineBreakStrategy()],
    /// [withListTagLineBreakStrategy][#withListTagLineBreakStrategy()], and [withArrayTagLineBreakStrategy][#withArrayTagLineBreakStrategy()]
    /// with the same strategy.
    ///
    /// @see #getLineBreakStrategy()
    /// @see LineBreakStrategy
    @Contract(value = "_ -> new", pure = true)
    SNBTCodec withLineBreakStrategy(LineBreakStrategy strategy);

    /// Returns the indentation string before each line.
    @Contract(pure = true)
    String getIndentation();

    /// Returns a new codec with the specified indentation string.
    ///
    /// The indentation string must be a sequence of spaces or tabs.
    ///
    /// @throws IllegalArgumentException if the indentation string is not a sequence of spaces or tabs.
    /// @see #getIndentation()
    @Contract(value = "_ -> new", pure = true)
    SNBTCodec withIndentation(String indentation);

    /// Returns a new codec with the specified indentation.
    ///
    /// @param spaces The number of spaces for indentation.
    /// @throws IllegalArgumentException if the indentation is not a positive integer.
    /// @see #getIndentation()
    /// @see #withIndentation(String)
    @Contract(value = "_ -> new", pure = true)
    SNBTCodec withIndentation(int spaces);

    /// Returns the surrounding spaces for SNBT.
    @Contract(pure = true)
    SurroundingSpaces getSurroundingSpaces();

    /// Returns a new codec with the specified surrounding spaces.
    @Contract(value = "_ -> new", pure = true)
    SNBTCodec withSurroundingSpaces(SurroundingSpaces surroundingSpaces);

    /// Returns the escape strategy for SNBT.
    ///
    /// @see #withEscapeStrategy(EscapeStrategy)
    /// @see EscapeStrategy
    @Contract(pure = true)
    EscapeStrategy getEscapeStrategy();

    /// Returns a new codec with the specified escape strategy.
    ///
    /// @see #getEscapeStrategy()
    /// @see EscapeStrategy
    @Contract(value = "_ -> new", pure = true)
    SNBTCodec withEscapeStrategy(EscapeStrategy escapeStrategy);

    /// Returns the quote strategy for SNBT tag names.
    ///
    /// @see #withNameQuoteStrategy(QuoteStrategy)
    /// @see QuoteStrategy
    @Contract(pure = true)
    QuoteStrategy getNameQuoteStrategy();

    /// Returns a new codec with the specified quote strategy for tag names.
    ///
    /// @see #getNameQuoteStrategy()
    /// @see QuoteStrategy
    @Contract(value = "_ -> new", pure = true)
    SNBTCodec withNameQuoteStrategy(QuoteStrategy quoteStrategy);

    /// Returns the quote strategy for SNBT tag values.
    ///
    /// @see #withValueQuoteStrategy(QuoteStrategy)
    /// @see QuoteStrategy
    @Contract(pure = true)
    QuoteStrategy getValueQuoteStrategy();

    /// Returns a new codec with the specified quote strategy for tag values.
    ///
    /// @see #getValueQuoteStrategy()
    /// @see QuoteStrategy
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
