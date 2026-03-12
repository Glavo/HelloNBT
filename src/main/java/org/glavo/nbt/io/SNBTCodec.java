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
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/// The codec for reading and writing Stringified NBT data.
///
/// Each SNBTCodec instance is immutable, thread-safe, and can be safely used by multiple threads.
public sealed interface SNBTCodec permits SNBTCodecImpl {

    /// Returns the default [SNBTCodec].
    static SNBTCodec of() {
        return SNBTCodecImpl.COMPACT;
    }

    /// Returns the compact [SNBTCodec].
    static SNBTCodec compact() {
        return SNBTCodecImpl.COMPACT;
    }

    /// Returns the default line break strategy for this codec.
    @Contract(pure = true)
    LineBreakStrategy getDefaultLineBreakStrategy();

    /// Returns a new codec with the specified default line break strategy.
    @Contract(value = "_ -> new", pure = true)
    SNBTCodec withDefaultLineBreakStrategy(LineBreakStrategy strategy);

    /// Returns the line break strategy for compound tags.
    ///
    /// If not specified, [the default line break strategy][#getDefaultLineBreakStrategy()] is used.
    @Contract(pure = true)
    @Nullable LineBreakStrategy getCompoundTagLineBreakStrategy();

    /// Returns a new codec with the specified line break strategy for compound tags.
    @Contract(value = "_ -> new", pure = true)
    SNBTCodec withCompoundTagLineBreakStrategy(@Nullable LineBreakStrategy strategy);

    /// Returns the line break strategy for list tags.
    ///
    /// If not specified, [the default line break strategy][#getDefaultLineBreakStrategy()] is used.
    @Contract(pure = true)
    @Nullable LineBreakStrategy getListTagLineBreakStrategy();

    /// Returns a new codec with the specified line break strategy for list tags.
    @Contract(value = "_ -> new", pure = true)
    SNBTCodec withListTagLineBreakStrategy(@Nullable LineBreakStrategy strategy);

    /// Returns the line break strategy for array tags.
    ///
    /// If not specified, [the default line break strategy][#getDefaultLineBreakStrategy()] is used.
    @Contract(pure = true)
    @Nullable LineBreakStrategy getArrayTagLineBreakStrategy();

    /// Returns a new codec with the specified line break strategy for array tags.
    @Contract(value = "_ -> new", pure = true)
    SNBTCodec withArrayTagLineBreakStrategy(@Nullable LineBreakStrategy strategy);

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
