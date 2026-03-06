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

import org.glavo.nbt.chunk.ChunkRegion;
import org.glavo.nbt.internal.NBTCodecImpl;
import org.glavo.nbt.tag.Tag;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;
import java.util.function.Function;

/// The codec for reading and writing NBT data.
public sealed interface NBTCodec permits NBTCodecImpl {

    /// Returns the default [NBTCodec].
    @Contract(pure = true)
    static NBTCodec of() {
        return NBTCodecImpl.JE;
    }

    @Contract(pure = true)
    static NBTCodec of(MinecraftEdition edition) {
        return edition == MinecraftEdition.JAVA_EDITION
                ? NBTCodecImpl.JE
                : NBTCodecImpl.BE;
    }

    /// Returns the Minecraft edition of the NBT data.
    ///
    /// The default edition is [MinecraftEdition#JAVA_EDITION].
    @Contract(pure = true)
    MinecraftEdition getEdition();

    /// Returns a new [NBTCodec] with the specified edition.
    @Contract(pure = true)
    NBTCodec withEdition(MinecraftEdition edition);

    /// Returns the factory for creating [OversizedChunkAccessor] for an Anvil file.
    @Contract(pure = true)
    Function<Path, OversizedChunkAccessor> getOversizedChunkAccessorFactory();

    /// Returns a new [NBTCodec] with the specified factory for creating [OversizedChunkAccessor].
    @Contract(pure = true)
    NBTCodec withOversizedChunkAccessorFactory(Function<Path, OversizedChunkAccessor> factory);

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

    /// Reads a NBT tag from a byte array.
    @Contract(pure = true)
    Tag readTag(byte[] array) throws IOException;

    /// Reads the specified NBT tag from a byte array.
    @Contract(pure = true)
    default <T extends Tag> T readTag(byte[] array, Class<T> tagClass) throws IOException {
        return check(readTag(array), tagClass);
    }

    /// Reads a NBT tag from a byte array with the specified offset and length.
    @Contract(pure = true)
    default Tag readTag(byte[] array, int offset, int length) throws IOException {
        return readTag(ByteBuffer.wrap(array, offset, length));
    }

    /// Reads the specified NBT tag from a byte array with the specified offset and length.
    @Contract(pure = true)
    default <T extends Tag> T readTag(byte[] array, int offset, int length, Class<T> tagClass) throws IOException {
        return check(readTag(array, offset, length), tagClass);
    }

    /// Reads a NBT tag from a byte buffer.
    ///
    /// This method does not change the position and the limit of the buffer.
    @Contract(pure = true)
    Tag readTag(ByteBuffer buffer) throws IOException;

    /// Reads the specified NBT tag from a byte buffer.
    ///
    /// This method does not change the position and the limit of the buffer.
    @Contract(pure = true)
    default <T extends Tag> T readTag(ByteBuffer buffer, Class<T> tagClass) throws IOException {
        return check(readTag(buffer), tagClass);
    }

    /// Reads a NBT tag from an input stream.
    ///
    /// After this method is called, the state of the `inputStream` is undefined.
    @Contract(mutates = "param1")
    Tag readTag(InputStream inputStream) throws IOException;

    /// Reads the specified NBT tag from an input stream.
    ///
    /// After this method is called, the state of the `inputStream` is undefined.
    @Contract(mutates = "param1")
    default <T extends Tag> T readTag(InputStream inputStream, Class<T> tagClass) throws IOException {
        return check(readTag(inputStream), tagClass);
    }

    /// Reads a NBT tag from a readable byte channel.
    ///
    /// After this method is called, the state of the `channel` is undefined.
    @Contract(mutates = "param1")
    Tag readTag(ReadableByteChannel channel) throws IOException;

    /// Reads the specified NBT tag from a readable byte channel.
    ///
    /// After this method is called, the state of the `channel` is undefined.
    @Contract(mutates = "param1")
    default <T extends Tag> T readTag(ReadableByteChannel channel, Class<T> tagClass) throws IOException {
        return check(readTag(channel), tagClass);
    }

    /// Reads a NBT tag from a file.
    Tag readTag(Path path) throws IOException;

    /// Reads the specified NBT tag from a file.
    default <T extends Tag> T readTag(Path path, Class<T> tagClass) throws IOException {
        return check(readTag(path), tagClass);
    }

    /// Writes a NBT tag to the output stream.
    @Contract(mutates = "param1")
    void writeTag(Tag tag, OutputStream outputStream) throws IOException;

    /// Writes a NBT tag to the byte channel.
    @Contract(mutates = "param1")
    void writeTag(Tag tag, WritableByteChannel channel) throws IOException;

    /// Reads a chunk region from a file.
    ///
    /// @see #getOversizedChunkAccessorFactory()
    /// @see #withOversizedChunkAccessorFactory(Function)
    default ChunkRegion readRegion(Path path) throws IOException {
        return readRegion(path, getOversizedChunkAccessorFactory().apply(path));
    }

    /// Reads a chunk region from a file.
    ///
    /// @see #getOversizedChunkAccessorFactory()
    /// @see #withOversizedChunkAccessorFactory(Function)
    ChunkRegion readRegion(Path path, OversizedChunkAccessor accessor) throws IOException;

    /// Reads a chunk region from an input stream.
    default ChunkRegion readRegion(InputStream inputStream) throws IOException {
        return readRegion(inputStream, OversizedChunkAccessor.emptyAccessor());
    }

    /// Reads a chunk region from an input stream.
    ChunkRegion readRegion(InputStream inputStream, OversizedChunkAccessor accessor) throws IOException;

    /// Reads a chunk region from a readable byte channel.
    default ChunkRegion readRegion(ReadableByteChannel channel) throws IOException {
        return readRegion(channel, OversizedChunkAccessor.emptyAccessor());
    }

    /// Reads a chunk region from a readable byte channel.
    ChunkRegion readRegion(ReadableByteChannel channel, OversizedChunkAccessor accessor) throws IOException;

    /// Writes a chunk region to an output stream.
    default void writeRegion(OutputStream outputStream, ChunkRegion region) throws IOException {
        writeRegion(outputStream, region, OversizedChunkAccessor.emptyAccessor());
    }

    /// Writes a chunk region to an output stream.
    void writeRegion(OutputStream outputStream, ChunkRegion region, OversizedChunkAccessor accessor) throws IOException;
}
