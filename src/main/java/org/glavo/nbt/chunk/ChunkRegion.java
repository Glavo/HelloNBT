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
package org.glavo.nbt.chunk;

import org.glavo.nbt.MinecraftEdition;
import org.glavo.nbt.NBTElement;
import org.glavo.nbt.NBTParent;
import org.glavo.nbt.internal.ChunkRegionHeader;
import org.glavo.nbt.internal.ChunkUtils;
import org.glavo.nbt.internal.input.*;
import org.glavo.nbt.tag.CompoundTag;
import org.glavo.nbt.tag.Tag;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/// @see <a href="https://minecraft.wiki/w/Region_file_format">Region file format - Minecraft Wiki</a>
/// @see <a href="https://minecraft.wiki/w/Anvil_file_format">Anvil file format - Minecraft Wiki</a>
public final class ChunkRegion implements NBTParent<Chunk>, NBTElement {
    static ChunkRegion readRegion(RawDataReader rawReader) throws IOException {
        if (rawReader.edition != MinecraftEdition.JAVA_EDITION) {
            throw new IllegalArgumentException("Only Java Edition supports region file format");
        }

        final long fileStart = rawReader.position();

        var header = ChunkRegionHeader.readHeader(rawReader);
        var region = new ChunkRegion();

        assert rawReader.position() == fileStart + 2 * ChunkUtils.SECTOR_BYTES;

        for (int localIndex : header.localIndexesSortedByOffset) {
            if (header.getSectorLengthBytes(localIndex) == 0) {
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
            if (compressType > 128) {
                if (chunkRawContentLength != 0L) {
                    throw new IOException("Invalid chunk content length: %d (expected 0 for compression type %d)".formatted(chunkRawContentLength, compressType));
                }

                throw new IOException("The chunk data is stored externally, and reading this data is not currently supported.");
            }

            BoundedDataReader reader = switch (compressType) {
                case 1 -> throw new IOException("GZip compression is not supported yet.");
                case 2 -> new ZlibDataReader(rawReader, chunkRawContentLength);
                case 3 -> new UncompressedDataReader(rawReader, chunkRawContentLength);
                case 4 -> throw new IOException("LZ4 compression is not supported yet.");
                default -> throw new IOException("Unsupported compression type: " + compressType);
            };

            try (reader) {
                var tag = Tag.readTag(reader);
                if (tag instanceof CompoundTag rootTag) {
                    region.getChunk(localIndex).rootTag = rootTag;
                } else {
                    throw new IOException("Unexpected tag type: " + tag);
                }
            }


        }

        return region;
    }

    public static ChunkRegion readRegion(Path file) throws IOException {
        try (var reader = new RawDataReader(
                new InputSource.OfInputStream(Files.newInputStream(file), true),
                MinecraftEdition.JAVA_EDITION)) {
            return readRegion(reader);
        }
    }

    private final Chunk[] chunks;

    public ChunkRegion() {
        this.chunks = new Chunk[ChunkUtils.CHUNKS_PRE_REGION];
        for (int i = 0; i < ChunkUtils.CHUNKS_PRE_REGION; i++) {
            chunks[i] = new Chunk(this, i);
        }
    }

    public Chunk getChunk(int localIndex) {
        Objects.checkIndex(localIndex, ChunkUtils.CHUNKS_PRE_REGION);

        return chunks[localIndex];
    }

    public Chunk getChunk(int x, int z) {
        Objects.checkIndex(x, ChunkUtils.CHUNKS_PER_REGION_SIDE);
        Objects.checkIndex(z, ChunkUtils.CHUNKS_PER_REGION_SIDE);

        return chunks[ChunkUtils.toLocalIndex(x, z)];
    }
}
