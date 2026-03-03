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
import org.glavo.nbt.TestResources;
import org.glavo.nbt.internal.ChunkRegionHeader;
import org.glavo.nbt.internal.input.InputSource;
import org.glavo.nbt.internal.input.RawDataReader;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.glavo.nbt.internal.ChunkUtils.CHUNKS_PRE_REGION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public final class ChunkRegionTest {

    @Test
    public void testReadHeader() throws Exception {
        Path resource = TestResources.getResource("/assets/r.-1.-1.mca");

        int[] sectorOffsets = new int[CHUNKS_PRE_REGION];
        int[] sectorLengths = new int[CHUNKS_PRE_REGION];
        int[] timestamps = new int[CHUNKS_PRE_REGION];

        try (var input = new DataInputStream(new BufferedInputStream(Files.newInputStream(resource)))) {
            for (int localIndex = 0; localIndex < CHUNKS_PRE_REGION; localIndex++) {
                int b0 = input.readUnsignedByte();
                int b1 = input.readUnsignedByte();
                int b2 = input.readUnsignedByte();
                int b3 = input.readUnsignedByte();

                sectorOffsets[localIndex] = (b0 << 16) + (b1 << 8) + b2;
                sectorLengths[localIndex] = b3;
            }

            for (int localIndex = 0; localIndex < CHUNKS_PRE_REGION; localIndex++) {
                timestamps[localIndex] = input.readInt();
            }
        }

        ChunkRegionHeader header;
        try (var input = new RawDataReader(new InputSource.OfInputStream(Files.newInputStream(resource), true), MinecraftEdition.JAVA_EDITION)) {
            header = ChunkRegionHeader.readHeader(input);
        }

        for (int localIndex = 0; localIndex < CHUNKS_PRE_REGION; localIndex++) {
            assertEquals(sectorOffsets[localIndex], header.getSectorOffset(localIndex), "Sector offset mismatch for local index " + localIndex);
            assertEquals(sectorLengths[localIndex], header.getSectorLength(localIndex), "Sector length mismatch for local index " + localIndex);
            assertEquals(timestamps[localIndex], header.timestamps[localIndex], "Timestamp mismatch for local index " + localIndex);
        }

        int currentSectorOffset = 0;
        for (int localIndex : header.localIndexesSortedByOffset) {
            if (header.getSectorOffset(localIndex) < currentSectorOffset) {
                fail("Sector offset is not sorted for local index %d: %d < %d".formatted(localIndex, header.getSectorOffset(localIndex), currentSectorOffset));
            }

            currentSectorOffset = header.getSectorOffset(localIndex) + header.getSectorLength(localIndex);
        }
    }

    @Test
    @Disabled
    public void testReadRegion() throws IOException {
        ChunkRegion.readRegion(TestResources.getResource("/assets/r.-1.-1.mca"));
    }
}
