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
import org.glavo.nbt.internal.input.InputContext;

import java.io.IOException;
import java.util.Objects;

/// @see <a href="https://minecraft.wiki/w/Region_file_format">Region file format - Minecraft Wiki</a>
/// @see <a href="https://minecraft.wiki/w/Anvil_file_format">Anvil file format - Minecraft Wiki</a>
public final class Region implements NBTElement {
    private static final int CHUNKS_PER_REGION_SIDE = 32;

    static Region readRegion(InputContext context) throws IOException {
        if (context.edition != MinecraftEdition.JAVA_EDITION) {
            throw new IllegalArgumentException("Only Java Edition supports region file format");
        }

        int[] diskInfo = context.rawReader.readIntArray(CHUNKS_PER_REGION_SIDE * CHUNKS_PER_REGION_SIDE);
        int[] timestamps = context.rawReader.readIntArray(CHUNKS_PER_REGION_SIDE * CHUNKS_PER_REGION_SIDE);

        for (int y = 0; y < CHUNKS_PER_REGION_SIDE; y++) {
            for (int x = 0; x < CHUNKS_PER_REGION_SIDE; x++) {
                int index = x + y * CHUNKS_PER_REGION_SIDE;

                int info = diskInfo[index];
                int sectorOffset = info >>> 8;
                int sectorCount = info & 0xFF;
                int timestamp = timestamps[index];

            }
        }

        throw new AssertionError("Not implemented yet");
    }

    private final Chunk[] chunks = new Chunk[CHUNKS_PER_REGION_SIDE * CHUNKS_PER_REGION_SIDE];

    public Chunk getChunk(int x, int z) {
        Objects.checkIndex(x, CHUNKS_PER_REGION_SIDE);
        Objects.checkIndex(z, CHUNKS_PER_REGION_SIDE);

        return chunks[x + z * CHUNKS_PER_REGION_SIDE];
    }
}
