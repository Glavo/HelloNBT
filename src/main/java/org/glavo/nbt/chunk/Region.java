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
import org.glavo.nbt.internal.ChunkMetadata;
import org.glavo.nbt.internal.ChunkMetadataTable;
import org.glavo.nbt.internal.ChunkUtils;
import org.glavo.nbt.internal.input.InputContext;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/// @see <a href="https://minecraft.wiki/w/Region_file_format">Region file format - Minecraft Wiki</a>
/// @see <a href="https://minecraft.wiki/w/Anvil_file_format">Anvil file format - Minecraft Wiki</a>
public final class Region implements NBTElement {
    static Region readRegion(InputContext context) throws IOException {
        if (context.edition != MinecraftEdition.JAVA_EDITION) {
            throw new IllegalArgumentException("Only Java Edition supports region file format");
        }

        var metadata = new ChunkMetadata[ChunkUtils.CHUNKS_PRE_REGION];

        int[] diskInfo = context.rawReader.readIntArray(ChunkUtils.CHUNKS_PRE_REGION);
        int[] timestamps = context.rawReader.readIntArray(ChunkUtils.CHUNKS_PRE_REGION);
        for (int z = 0; z < ChunkUtils.CHUNKS_PER_REGION_SIDE; z++) {
            for (int x = 0; x < ChunkUtils.CHUNKS_PER_REGION_SIDE; x++) {
                int index = x + z * ChunkUtils.CHUNKS_PER_REGION_SIDE;

                int info = diskInfo[index];
                int sectorOffset = info >>> 8;
                int sectorCount = info & 0xFF;
                int timestamp = timestamps[index];

                metadata[index] = new ChunkMetadata(index, sectorOffset, sectorCount, timestamp);
            }
        }

        var table = new ChunkMetadataTable(List.of(metadata));

        List<ChunkMetadata> sortedBySectorOffset = table.getSortedBySectorOffset();

        long contentStart = context.source.position();
        for (ChunkMetadata chunkMetadata : sortedBySectorOffset) {
            long sectorStart = contentStart + (long) chunkMetadata.sectorOffset() * ChunkUtils.SECTOR_BYTES;

        }

        // TODO
        throw new AssertionError("Not implemented yet");
    }

    private final Chunk[] chunks = new Chunk[ChunkUtils.CHUNKS_PRE_REGION];

    public Chunk getChunk(int x, int z) {
        Objects.checkIndex(x, ChunkUtils.CHUNKS_PER_REGION_SIDE);
        Objects.checkIndex(z, ChunkUtils.CHUNKS_PER_REGION_SIDE);

        return chunks[x + z * ChunkUtils.CHUNKS_PER_REGION_SIDE];
    }
}
