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
import org.glavo.nbt.tag.TagType;
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
///
/// Each NBTCodec instance is immutable and thread-safe.
///
/// # Getting NBTCodec Instances
///
/// NBTCodec provides two factory methods to obtain NBTCodec instances:
///
/// - [NBTCodec#of()] returns the default [NBTCodec] with big-endian byte order for reading and writing NBT.
/// - [NBTCodec#of(MinecraftEdition)] returns a [NBTCodec] for the specified [MinecraftEdition].
///   If the edition is [MinecraftEdition#JAVA_EDITION], it uses big-endian byte order;
///   If the edition is [MinecraftEdition#BEDROCK_EDITION], it uses little-endian byte order.
///
/// Beyond just [MinecraftEdition], NBTCodec offers additional configuration options.
/// To adjust these settings, you can obtain an instance via a factory method and then call
/// a `withXxx` method (such as [#withEdition(MinecraftEdition)]) to create a new [NBTCodec] instance.
///
/// # Reading and Writing NBT Data
///
/// NBTCodec supports reading NBT data from multiple sources:
///
/// ```java
/// var codec = NBTCodec.of();
///
/// Tag tag;
///
/// // Read from a byte array
/// tag = codec.readTag(new byte[]{...});
///
/// // Read from a byte buffer
/// tag = codec.readTag(ByteBuffer.wrap(new byte[]{...}));
///
/// // Read from an input stream
/// tag = codec.readTag(new ByteArrayInputStream(new byte[]{...}));
///
/// // Read from a readable byte channel
/// tag = codec.readTag(Channels.newChannel(...));
///
/// // Read from a file
/// tag = codec.readTag(Path.of("/path/to/file"));
/// ```
///
/// When reading NBT data, NBTCodec automatically detects whether the data is compressed with GZip or LZ4 and decompresses it transparently.
///
/// When reading a `Tag`, you can pass in a `TagType` to specify the expected `Tag` type.
/// An `IOException` will be thrown if the data does not meet expectations:
///
/// ```java
/// CompoundTag levelDat = codec.readTag(Path.of("level.dat"), TagType.COMPOUND);
/// ```
///
/// You can also easily write a `Tag` to an output stream or a byte channel:
///
/// ```java
/// try (var outputStream = new FileOutputStream("/path/to/file")) {
///     codec.writeTag(outputStream, tag);
/// }
///
/// // or
/// try (var channel = FileChannel.open(Path.of("/path/to/file"), StandardOpenOption.WRITE)) {
///     codec.writeTag(channel, tag);
/// }
/// ```
///
/// Currently, NBTCodec does not support automatic data compression.
/// When data needs to be compressed, the output stream should be wrapped with `GZipOutputStream` or `LZ4BlockOutputStream` before being passed to the `writeTag` method.
///
/// # Reading Anvil files and region files
///
/// NBTCodec also supports reading and writing chunk regions from Anvil files or region files:
///
/// ```java
/// // Read a chunk region from a file
/// ChunkRegion region = codec.readRegion(Path.of("/path/to/region"));
///
/// try (var outputStream = new FileOutputStream("/path/to/region")) {
///     codec.writeRegion(outputStream, region);
/// }
/// ```
///
/// Starting from Minecraft 1.15 (19w34a), if the chunk data exceeds 1020KiB, it will be split and saved to another file.
/// NBTCodec uses [ExternalChunkAccessor] to read and write external chunk files.
///
/// For [#readRegion(Path)], it will use the [#getExternalChunkAccessorFactory()] to get the external chunk accessor for the file.
/// By default, it is [ExternalChunkAccessor#defaultFactory()], which returns an accessor for external chunk files if the file name matches the pattern `r.<regionX>.<regionZ>.mca`.
///
/// For other variants (like [#readRegion(InputStream)]), the default behavior is not to support external chunk files.
/// They will throw an exception when trying to access external chunk files.
/// However, you can use [#readRegion(InputStream, ExternalChunkAccessor)] or [#readRegion(ReadableByteChannel, ExternalChunkAccessor)] to manually specify the external chunk accessor.
///
public sealed interface NBTCodec permits NBTCodecImpl {

    /// Returns the default [NBTCodec].
    ///
    /// The default edition is [MinecraftEdition#JAVA_EDITION].
    @Contract(pure = true)
    static NBTCodec of() {
        return NBTCodecImpl.JE;
    }

    /// Returns a [NBTCodec] for the specified [MinecraftEdition].
    @Contract(pure = true)
    static NBTCodec of(MinecraftEdition edition) {
        return edition == MinecraftEdition.JAVA_EDITION
                ? NBTCodecImpl.JE
                : NBTCodecImpl.BE;
    }

    /// Returns the Minecraft edition of the NBT data.
    ///
    /// The default edition is [MinecraftEdition#JAVA_EDITION].
    ///
    /// @see #withEdition(MinecraftEdition)
    /// @see MinecraftEdition
    @Contract(pure = true)
    MinecraftEdition getEdition();

    /// Returns a new [NBTCodec] with the specified edition.
    ///
    /// @see #getEdition()
    /// @see MinecraftEdition
    @Contract(pure = true)
    NBTCodec withEdition(MinecraftEdition edition);

    /// Returns the factory for getting [ExternalChunkAccessor] for an Anvil file.
    ///
    /// The default factory is [ExternalChunkAccessor#defaultFactory()].
    ///
    /// @see #withExternalChunkAccessorFactory(Function)
    /// @see ExternalChunkAccessor
    @Contract(pure = true)
    Function<Path, ExternalChunkAccessor> getExternalChunkAccessorFactory();

    /// Returns a new [NBTCodec] with the specified factory for getting [ExternalChunkAccessor].
    ///
    /// @see #getExternalChunkAccessorFactory()
    /// @see ExternalChunkAccessor
    @Contract(pure = true)
    NBTCodec withExternalChunkAccessorFactory(Function<Path, ExternalChunkAccessor> factory);

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
    default <T extends Tag> T readTag(byte[] array, TagType<T> tagType) throws IOException {
        return check(readTag(array), tagType.tagClass());
    }

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
    default <T extends Tag> T readTag(byte[] array, int offset, int length, TagType<T> tagType) throws IOException {
        return check(readTag(array, offset, length), tagType.tagClass());
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
    default <T extends Tag> T readTag(ByteBuffer buffer, TagType<T> tagType) throws IOException {
        return check(readTag(buffer), tagType.tagClass());
    }

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
    default <T extends Tag> T readTag(InputStream inputStream, TagType<T> tagType) throws IOException {
        return check(readTag(inputStream), tagType.tagClass());
    }

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
    default <T extends Tag> T readTag(ReadableByteChannel channel, TagType<T> tagType) throws IOException {
        return check(readTag(channel), tagType.tagClass());
    }

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
    default <T extends Tag> T readTag(Path path, TagType<T> tagType) throws IOException {
        return check(readTag(path), tagType.tagClass());
    }

    /// Reads the specified NBT tag from a file.
    default <T extends Tag> T readTag(Path path, Class<T> tagClass) throws IOException {
        return check(readTag(path), tagClass);
    }

    /// Writes a NBT tag to the output stream.
    @Contract(mutates = "param1")
    void writeTag(OutputStream outputStream, Tag tag) throws IOException;

    /// Writes a NBT tag to the byte channel.
    @Contract(mutates = "param1")
    void writeTag(WritableByteChannel channel, Tag tag) throws IOException;

    /// Reads a chunk region from a file.
    ///
    /// @see #getExternalChunkAccessorFactory()
    /// @see #withExternalChunkAccessorFactory(Function)
    default ChunkRegion readRegion(Path path) throws IOException {
        return readRegion(path, getExternalChunkAccessorFactory().apply(path));
    }

    /// Reads a chunk region from a file.
    ///
    /// @see #getExternalChunkAccessorFactory()
    /// @see #withExternalChunkAccessorFactory(Function)
    ChunkRegion readRegion(Path path, ExternalChunkAccessor accessor) throws IOException;

    /// Reads a chunk region from an input stream.
    default ChunkRegion readRegion(InputStream inputStream) throws IOException {
        return readRegion(inputStream, ExternalChunkAccessor.emptyAccessor());
    }

    /// Reads a chunk region from an input stream.
    ChunkRegion readRegion(InputStream inputStream, ExternalChunkAccessor accessor) throws IOException;

    /// Reads a chunk region from a readable byte channel.
    default ChunkRegion readRegion(ReadableByteChannel channel) throws IOException {
        return readRegion(channel, ExternalChunkAccessor.emptyAccessor());
    }

    /// Reads a chunk region from a readable byte channel.
    ChunkRegion readRegion(ReadableByteChannel channel, ExternalChunkAccessor accessor) throws IOException;

    /// Writes a chunk region to an output stream.
    default void writeRegion(OutputStream outputStream, ChunkRegion region) throws IOException {
        writeRegion(outputStream, region, ExternalChunkAccessor.emptyAccessor());
    }

    /// Writes a chunk region to an output stream.
    void writeRegion(OutputStream outputStream, ChunkRegion region, ExternalChunkAccessor accessor) throws IOException;
}
