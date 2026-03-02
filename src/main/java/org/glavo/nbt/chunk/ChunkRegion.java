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
import org.glavo.nbt.internal.ChunkMetadata;
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

        var metadata = new ChunkMetadata[ChunkUtils.CHUNKS_PRE_REGION];

        int[] diskInfo = rawReader.readIntArray(ChunkUtils.CHUNKS_PRE_REGION);
        int[] timestamps = rawReader.readIntArray(ChunkUtils.CHUNKS_PRE_REGION);
        for (int z = 0; z < ChunkUtils.CHUNKS_PER_REGION_SIDE; z++) {
            for (int x = 0; x < ChunkUtils.CHUNKS_PER_REGION_SIDE; x++) {
                int index = ChunkUtils.toLocalIndex(x, z);

                int info = diskInfo[index];
                int sectorOffset = info >>> 8;
                int sectorCount = info & 0xFF;
                int timestamp = timestamps[index];

                metadata[index] = new ChunkMetadata(index, sectorOffset, sectorCount, timestamp);
            }
        }

        var region = new ChunkRegion();

        for (ChunkMetadata chunkMetadata : ChunkMetadata.sortedByOffset(metadata)) {
            long sectorStart = fileStart + (long) chunkMetadata.sectorOffset() * ChunkUtils.SECTOR_BYTES;
            long position = rawReader.position();
            if (position != sectorStart) {
                if (position < sectorStart) {
                    rawReader.skip(sectorStart - position);
                } else {
                    throw new IOException("Invalid chunk metadata: sector offset points to a position before the current position");
                }
            }

            long chunkRawDataLength = rawReader.readUnsignedInt();
            if (chunkRawDataLength < 1) {
                throw new IOException("Invalid chunk data length: " + chunkRawDataLength);
            }

            long chunkRawContentLength = chunkRawDataLength - 1L;

            int compressType = rawReader.readUnsignedByte();
            if (compressType > 128) {
                if (chunkRawContentLength != 0L) {
                    throw new IOException("Invalid chunk data length: %d (expected 1 for compression type %d)".formatted(chunkRawDataLength, compressType));
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
                    region.getChunk(chunkMetadata.localIndex()).rootTag = rootTag;
                } else {
                    throw new IOException("Unexpected tag type: " + tag);
                }
            }
        }

        return region;
    }

    public static ChunkRegion readRegion(Path file) throws IOException {
        try (var reader = new RawDataReader(
                new InputSource.OfInputStream(Files.newInputStream(file), true), MinecraftEdition
                .JAVA_EDITION)) {
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
