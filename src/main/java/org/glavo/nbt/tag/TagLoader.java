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
package org.glavo.nbt.tag;

import org.glavo.nbt.MinecraftEdition;
import org.glavo.nbt.internal.input.TagLoaderImpl;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;

/// The loader for reading NBT tags.
public sealed interface TagLoader permits TagLoaderImpl {

    /// Returns the default [TagLoader].
    @Contract(pure = true)
    static TagLoader getDefault() {
        return TagLoaderImpl.DEFAULT;
    }

    /// Returns a new [TagLoader.Builder].
    @Contract("-> new")
    static TagLoader.Builder newBuilder() {
        return new TagLoaderImpl.BuilderImpl();
    }

    /// Returns whether the loader automatically decompresses the NBT data.
    ///
    /// If set to `true`, the loader will automatically decompress the NBT data if it is compressed by gzip or LZ4;
    /// otherwise, it will throw an exception if the data is compressed.
    ///
    /// The default value is `true`.
    @ApiStatus.Experimental
    @Contract(pure = true)
    boolean isAutoDecompress();

    /// Returns the Minecraft edition of the NBT data.
    ///
    /// The default edition is [MinecraftEdition#JAVA_EDITION].
    @Contract(pure = true)
    MinecraftEdition getEdition();

    /// Loads a NBT tag from a byte array.
    @Contract(pure = true)
    Tag load(byte[] array) throws IOException;

    /// Loads the specified NBT tag from a byte array.
    @Contract(pure = true)
    default <T extends Tag> T load(byte[] array, Class<T> tagClass) throws IOException {
        return check(load(array), tagClass);
    }

    /// Loads a NBT tag from a byte array with the specified offset and length.
    @Contract(pure = true)
    default Tag load(byte[] array, int offset, int length) throws IOException {
        return load(ByteBuffer.wrap(array, offset, length));
    }

    /// Loads the specified NBT tag from a byte array with the specified offset and length.
    @Contract(pure = true)
    default <T extends Tag> T load(byte[] array, int offset, int length, Class<T> tagClass) throws IOException {
        return check(load(array, offset, length), tagClass);
    }

    /// Loads a NBT tag from a byte buffer.
    ///
    /// This method does not change the position and the limit of the buffer.
    @Contract(pure = true)
    Tag load(ByteBuffer buffer) throws IOException;

    /// Loads the specified NBT tag from a byte buffer.
    ///
    /// This method does not change the position and the limit of the buffer.
    @Contract(pure = true)
    default <T extends Tag> T load(ByteBuffer buffer, Class<T> tagClass) throws IOException {
        return check(load(buffer), tagClass);
    }

    /// Loads a NBT tag from an input stream.
    ///
    /// After this method is called, the state of the `inputStream` is undefined.
    @Contract(mutates = "param1")
    Tag load(InputStream inputStream) throws IOException;

    /// Loads the specified NBT tag from an input stream.
    ///
    /// After this method is called, the state of the `inputStream` is undefined.
    @Contract(mutates = "param1")
    default <T extends Tag> T load(InputStream inputStream, Class<T> tagClass) throws IOException {
        return check(load(inputStream), tagClass);
    }

    /// Loads a NBT tag from a readable byte channel.
    ///
    /// After this method is called, the state of the `channel` is undefined.
    @Contract(mutates = "param1")
    Tag load(ReadableByteChannel channel) throws IOException;

    /// Loads the specified NBT tag from a readable byte channel.
    ///
    /// After this method is called, the state of the `channel` is undefined.
    @Contract(mutates = "param1")
    default <T extends Tag> T load(ReadableByteChannel channel, Class<T> tagClass) throws IOException {
        return check(load(channel), tagClass);
    }

    /// Loads a NBT tag from a file.
    Tag load(Path path) throws IOException;

    /// Loads the specified NBT tag from a file.
    default <T extends Tag> T load(Path path, Class<T> tagClass) throws IOException {
        return check(load(path), tagClass);
    }

    private static <T extends Tag> T check(@Nullable Tag tag, Class<T> tagClass) throws IOException {
        if (tag == null) {
            throw new IOException("Unexpected TAG_END");
        }
        try {
            return tagClass.cast(tag);
        } catch (ClassCastException e) {
            throw new IOException("Unexpected tag type: " + tag);
        }
    }

    /// The builder for creating a [TagLoader].
    sealed interface Builder permits TagLoaderImpl.BuilderImpl {

        /// Sets the Minecraft edition of the NBT data.
        ///
        /// The default edition is [MinecraftEdition#JAVA_EDITION].
        @Contract(value = "_ -> this", mutates = "this")
        Builder setEdition(MinecraftEdition edition);

        /// Sets whether to automatically decompress the NBT data.
        ///
        /// If set to `true`, the loader will automatically decompress the NBT data if it is compressed by gzip or LZ4;
        /// otherwise, it will throw an exception if the data is compressed.
        ///
        /// The default value is `true`.
        @ApiStatus.Experimental
        @Contract(value = "_ -> this", mutates = "this")
        Builder setAutoDecompress(boolean autoDecompress);

        /// Builds a new [TagLoader] with the specified configuration.
        TagLoader build();
    }
}
