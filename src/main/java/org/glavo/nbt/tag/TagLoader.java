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
import org.glavo.nbt.NBTLoader;
import org.glavo.nbt.internal.input.TagLoaderImpl;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;

/// The loader for reading NBT tags.
@FunctionalInterface
public interface TagLoader<T extends Tag, S> extends NBTLoader<T, S> {

    /// The loader for reading NBT tags from an [InputStream].
    static TagLoader<Tag, InputStream> ofInputStream() {
        return TagLoaderImpl.OfInputStream.DEFAULT;
    }

    /// The loader for reading NBT tags from an [InputStream].
    static <T extends Tag> TagLoader<T, InputStream> ofInputStream(Class<T> tagClass) {
        return new TagLoaderImpl.OfInputStream<>(tagClass, MinecraftEdition.JAVA_EDITION, true);
    }

    /// The loader for reading NBT tags from a [ReadableByteChannel].
    static TagLoader<Tag, ReadableByteChannel> ofByteChannel() {
        return TagLoaderImpl.OfByteChannel.DEFAULT;
    }

    /// The loader for reading NBT tags from a [ReadableByteChannel].
    static <T extends Tag> TagLoader<T, ReadableByteChannel> ofByteChannel(Class<T> tagClass) {
        return new TagLoaderImpl.OfByteChannel<>(tagClass, MinecraftEdition.JAVA_EDITION, true);
    }

    /// The loader for reading NBT tags from a [Path].
    static TagLoader<Tag, Path> ofPath() {
        return TagLoaderImpl.OfPath.DEFAULT;
    }

    /// The loader for reading NBT tags from a [Path].
    static <T extends Tag> TagLoader<T, Path> ofPath(Class<T> tagClass) {
        return new TagLoaderImpl.OfPath<>(tagClass, MinecraftEdition.JAVA_EDITION, true);
    }

    @Override
    T load(S source) throws IOException;

    /// The builder for creating a [TagLoader].
    interface Builder<T extends Tag, S>
            extends NBTLoader.Builder<T, S> {

        /// Creates a builder for creating a [TagLoader] for reading NBT tags from an [InputStream].
        @Contract(value = "-> new")
        static Builder<Tag, InputStream> ofInputStream() {
            return Builder.ofInputStream(Tag.class);
        }

        /// Creates a builder for creating a [TagLoader] for reading NBT tags from an [InputStream].
        @Contract(value = "_ -> new")
        static <T extends Tag> Builder<T, InputStream> ofInputStream(Class<T> tagClass) {
            return new TagLoaderImpl.OfInputStream.Builder<>(tagClass);
        }

        /// Creates a builder for creating a [TagLoader] for reading NBT tags from a [ReadableByteChannel].
        @Contract(value = "-> new")
        static Builder<Tag, ReadableByteChannel> ofByteChannel() {
            return Builder.ofByteChannel(Tag.class);
        }

        /// Creates a builder for creating a [TagLoader] for reading NBT tags from a [ReadableByteChannel].
        @Contract(value = "_ -> new")
        static <T extends Tag> Builder<T, ReadableByteChannel> ofByteChannel(Class<T> tagClass) {
            return new TagLoaderImpl.OfByteChannel.Builder<>(tagClass);
        }

        /// Creates a builder for creating a [TagLoader] for reading NBT tags from a [Path].
        @Contract(value = "-> new")
        static Builder<Tag, Path> ofPath() {
            return Builder.ofPath(Tag.class);
        }

        /// Creates a builder for creating a [TagLoader] for reading NBT tags from a [Path].
        @Contract(value = "_ -> new")
        static <T extends Tag> Builder<T, Path> ofPath(Class<T> tagClass) {
            return new TagLoaderImpl.OfPath.Builder<>(tagClass);
        }

        /// Sets the Minecraft edition of the NBT data.
        ///
        /// The default edition is [MinecraftEdition#JAVA_EDITION].
        @Contract(value = "_ -> this", mutates = "this")
        Builder<T, S> setEdition(MinecraftEdition edition);

        /// Sets whether to automatically decompress the NBT data.
        ///
        /// If set to `true`, the loader will automatically decompress the NBT data if it is compressed by gzip or LZ4;
        /// otherwise, it will throw an exception if the data is compressed.
        ///
        /// The default value is `true`.
        @ApiStatus.Experimental
        @Contract(value = "_ -> this", mutates = "this")
        Builder<T, S> setAutoDecompress(boolean autoDecompress);

        @Override
        TagLoader<T, S> build();
    }
}
