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

import org.glavo.nbt.tag.Tag;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.ByteOrder;

public final class NBTIO {

    static final Tag.Unsafe TAG_UNSAFE;

    static {
        try {
            TAG_UNSAFE = Tag.Unsafe.getInstance(MethodHandles.lookup());
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    /// Read a tag from the input stream.
    ///
    /// After calling this method, the state of `inputStream` is undefined, so it should no longer be used.
    ///
    /// @param inputStream The input stream.
    /// @param byteOrder   The byte order of the input stream.
    /// @return The tag read from the input stream.
    /// @throws IOException If an I/O error occurs.
    public static Tag readTag(InputStream inputStream, ByteOrder byteOrder) throws IOException {
        try (var reader = new NBTReader(byteOrder, inputStream)) {
            Tag tag = reader.readTag();
            if (tag == null) {
                throw new IOException("Unexpected end of stream");
            }
            return tag;
        }
    }

    private NBTIO() {
    }
}
