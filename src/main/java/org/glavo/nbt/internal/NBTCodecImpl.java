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
package org.glavo.nbt.internal;

import org.glavo.nbt.chunk.Chunk;
import org.glavo.nbt.chunk.ChunkRegion;
import org.glavo.nbt.internal.input.*;
import org.glavo.nbt.internal.output.DataWriter;
import org.glavo.nbt.internal.output.OutputTarget;
import org.glavo.nbt.internal.output.RawDataWriter;
import org.glavo.nbt.io.MinecraftEdition;
import org.glavo.nbt.io.OversizedChunkAccessor;
import org.glavo.nbt.tag.CompoundTag;
import org.glavo.nbt.tag.Tag;
import org.glavo.nbt.io.NBTCodec;
import org.glavo.nbt.tag.TagType;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Objects;
import java.util.function.Function;

public record NBTCodecImpl(MinecraftEdition edition,
                           Function<Path, OversizedChunkAccessor> oversizedChunkAccessorFactory) implements NBTCodec {

    public static final NBTCodecImpl JE = new NBTCodecImpl(MinecraftEdition.JAVA_EDITION, OversizedChunkAccessor.defaultFactory());
    public static final NBTCodecImpl BE = new NBTCodecImpl(MinecraftEdition.BEDROCK_EDITION, OversizedChunkAccessor.defaultFactory());

    public static @Nullable Tag readTag(DataReader reader) throws IOException {
        byte tagByte = reader.readByte();
        if (tagByte == 0) {
            return null;
        }

        var type = TagType.getById(tagByte);
        if (type == null) {
            throw new IOException("Invalid tag type: %02x".formatted(Byte.toUnsignedInt(tagByte)));
        }

        Tag tag = type.createTag();
        tag.setName(reader.readString());
        Access.TAG.readContent(tag, reader);
        return tag;
    }

    public static @Nullable Tag readTagAutoDecompress(RawDataReader reader) throws IOException {
        byte tagByte = reader.lookAheadByte();

        // GZip Magic Number: 0x1F 0x8B 0x08
        if (tagByte == 0x1F) {
            try (var decompressReader = DecompressStreamDataReader.newGZipDataReader(reader, -1)) {
                return readTag(decompressReader);
            }
        }

        // LZ4 Magic Number: "LZ4Block"
        if (tagByte == 'L') {
            try (var decompressReader = DecompressStreamDataReader.newLZ4DataReader(reader, -1)) {
                return readTag(decompressReader);
            }
        }

        return readTag(reader);
    }

    public static void writeTag(DataWriter writer, Tag tag) throws IOException {
        writer.writeByte(tag.getType().id()); // implicit null check
        writer.writeString(tag.getName());
        Access.TAG.writeContent(tag, writer);
    }

    public static ChunkRegion readRegion(RawDataReader rawReader, OversizedChunkAccessor accessor) throws IOException {
        if (rawReader.edition != MinecraftEdition.JAVA_EDITION) {
            throw new IllegalArgumentException("Only Java Edition supports region file format");
        }

        final long fileStart = rawReader.position();

        var header = ChunkRegionHeader.readHeader(rawReader);
        var region = new ChunkRegion();

        assert rawReader.position() == fileStart + 2 * ChunkUtils.SECTOR_BYTES;

        for (int localIndex : header.localIndexesSortedByOffset) {
            if (header.getSectorLength(localIndex) == 0) {
                if (header.getTimestampEpochSeconds(localIndex) != 0L) {
                    region.setChunk(
                            localIndex,
                            new Chunk(Instant.ofEpochSecond(header.getTimestampEpochSeconds(localIndex))));
                }

                continue;
            }

            long sectorStart = fileStart + header.getSectorOffsetBytes(localIndex);
            long position = rawReader.position();
            if (position != sectorStart) {
                if (position < sectorStart) {
                    rawReader.skip(sectorStart - position);
                } else {
                    throw new IOException("Invalid chunk metadata: sector offset points to a position before the current position");
                }
            }

            assert rawReader.position() == sectorStart;

            long chunkRawLength = rawReader.readUnsignedInt();
            if (chunkRawLength < 1) {
                throw new IOException("Invalid chunk data length: " + chunkRawLength);
            }

            if (chunkRawLength + 4L > header.getSectorLengthBytes(localIndex)) {
                throw new IOException("Invalid chunk data length: " + chunkRawLength + " (expected <= " + header.getSectorLengthBytes(localIndex) + " - 4)");
            }

            long chunkRawContentLength = chunkRawLength - 1L;

            int compressType = rawReader.readUnsignedByte();
            boolean external = compressType > 128;

            RawDataReader oversizedReader;
            if (external) {
                if (chunkRawContentLength != 0L) {
                    throw new IOException("Invalid chunk content length: %d (expected 0 for compression type %d)".formatted(chunkRawContentLength, compressType));
                }

                compressType -= 128;

                InputStream oversizedChunkInputStream = accessor.openInputStream(ChunkUtils.getLocalX(localIndex), ChunkUtils.getLocalZ(localIndex));
                if (oversizedChunkInputStream == null) {
                    throw new IOException("Failed to open oversized chunk file for chunk (%d, %d)".formatted(ChunkUtils.getLocalX(localIndex), ChunkUtils.getLocalZ(localIndex)));
                }
                oversizedReader = new RawDataReader(new InputSource.OfInputStream(oversizedChunkInputStream, true), MinecraftEdition.JAVA_EDITION);
            } else {
                oversizedReader = null;
            }

            try (oversizedReader) {
                RawDataReader actualRawReader;

                if (external) {
                    actualRawReader = oversizedReader;
                    oversizedReader.skip(5L);
                } else {
                    actualRawReader = rawReader;
                }

                BoundedDataReader reader = switch (compressType) {
                    case 1 -> DecompressStreamDataReader.newGZipDataReader(actualRawReader, chunkRawContentLength);
                    case 2 -> new ZlibDataReader(actualRawReader, chunkRawContentLength);
                    case 3 -> new UncompressedDataReader(actualRawReader, chunkRawContentLength);
                    case 4 -> DecompressStreamDataReader.newLZ4DataReader(actualRawReader, chunkRawContentLength);
                    default -> throw new IOException("Unsupported compression type: " + compressType);
                };

                try (reader) {
                    var tag = NBTCodecImpl.readTag(reader);
                    if (tag instanceof CompoundTag rootTag) {
                        region.setChunk(localIndex, new Chunk(
                                Instant.ofEpochSecond(header.getTimestampEpochSeconds(localIndex)),
                                rootTag)
                        );
                    } else {
                        throw new IOException("Unexpected tag type: " + tag);
                    }
                }
            }
        }

        return region;
    }

    @Override
    public MinecraftEdition getEdition() {
        return edition;
    }

    @Override
    public NBTCodec withEdition(MinecraftEdition edition) {
        Objects.requireNonNull(edition, "edition");
        return edition == this.edition ? this : new NBTCodecImpl(edition, oversizedChunkAccessorFactory);
    }

    @Override
    public Function<Path, OversizedChunkAccessor> getOversizedChunkAccessorFactory() {
        return oversizedChunkAccessorFactory;
    }

    @Override
    public NBTCodec withOversizedChunkAccessorFactory(Function<Path, OversizedChunkAccessor> factory) {
        Objects.requireNonNull(factory, "factory");
        return factory == this.oversizedChunkAccessorFactory ? this : new NBTCodecImpl(edition, factory);
    }

    private Tag check(@Nullable Tag tag) throws IOException {
        if (tag == null) {
            throw new IOException("Unexpected TAG_END");
        }
        return tag;
    }

    public Tag readTag(byte[] array) throws IOException {
        try (var reader = new RawDataReader(new InputSource.OfByteBuffer(array), edition)) {
            return check(readTagAutoDecompress(reader));
        }
    }

    @Override
    public Tag readTag(ByteBuffer buffer) throws IOException {
        try (var reader = new RawDataReader(new InputSource.OfByteBuffer(buffer), edition)) {
            return check(readTagAutoDecompress(reader));
        }
    }

    @Override
    public Tag readTag(InputStream inputStream) throws IOException {
        try (var reader = new RawDataReader(new InputSource.OfInputStream(inputStream, false), edition)) {
            return check(readTagAutoDecompress(reader));
        }
    }

    @Override
    public Tag readTag(ReadableByteChannel channel) throws IOException {
        try (var reader = new RawDataReader(new InputSource.OfByteChannel(channel, false), edition)) {
            return check(readTagAutoDecompress(reader));
        }
    }

    @Override
    public Tag readTag(Path path) throws IOException {
        try (var channel = Files.newByteChannel(path, StandardOpenOption.READ);
             var reader = new RawDataReader(new InputSource.OfByteChannel(channel, false), edition)) {
            return check(readTagAutoDecompress(reader));
        }
    }

    @Override
    public void writeTag(Tag tag, OutputStream outputStream) throws IOException {
        try (var writer = new RawDataWriter(new OutputTarget.OfOutputStream(outputStream, false), edition)) {
            writeTag(writer, tag);
        }
    }

    @Override
    public void writeTag(Tag tag, WritableByteChannel channel) throws IOException {
        try (var writer = new RawDataWriter(new OutputTarget.OfByteChannel(channel, false), edition)) {
            writeTag(writer, tag);
        }
    }

    @Override
    public ChunkRegion readRegion(Path path) throws IOException {
        try (var channel = FileChannel.open(path, StandardOpenOption.READ);
             var reader = new RawDataReader(new InputSource.OfByteChannel(channel, true), MinecraftEdition.JAVA_EDITION)) {
            return readRegion(reader, oversizedChunkAccessorFactory.apply(path));
        }
    }

    @Override
    public ChunkRegion readRegion(InputStream inputStream, OversizedChunkAccessor accessor) throws IOException {
        Objects.requireNonNull(inputStream, "inputStream");
        try (var reader = new RawDataReader(new InputSource.OfInputStream(inputStream, false), MinecraftEdition.JAVA_EDITION)) {
            return readRegion(reader, accessor);
        }
    }

    @Override
    public ChunkRegion readRegion(ReadableByteChannel channel, OversizedChunkAccessor accessor) throws IOException {
        Objects.requireNonNull(channel, "channel");
        try (var reader = new RawDataReader(new InputSource.OfByteChannel(channel, false), MinecraftEdition.JAVA_EDITION)) {
            return readRegion(reader, accessor);
        }
    }

    @Override
    public String toString() {
        return "NBTCodec[edition=%s, oversizedChunkAccessorFactory=%s]".formatted(edition, oversizedChunkAccessorFactory);
    }
}
