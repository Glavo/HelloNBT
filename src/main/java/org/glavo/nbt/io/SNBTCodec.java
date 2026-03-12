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
        return SNBTCodecImpl.DEFAULT;
    }

    /// Parses a Stringified NBT data.
    ///
    /// @throws IOException if the input is not a valid Stringified NBT data.
    @Contract(pure = true)
    default Tag readTag(CharSequence input) throws IOException {
        return readTag(input, 0, input.length());
    }

    /// Parses a Stringified NBT data.
    ///
    /// @throws IndexOutOfBoundsException if the range is out of bounds.
    /// @throws IOException               if the input is not a valid Stringified NBT data.
    @Contract(pure = true)
    Tag readTag(CharSequence input, int startInclusive, int endExclusive) throws IOException;

    /// Reads a Stringified NBT data from a readable source.
    ///
    /// @throws IOException if an I/O error occurs.
    @Contract(mutates = "param1")
    Tag readTag(Readable readable) throws IOException;
}
