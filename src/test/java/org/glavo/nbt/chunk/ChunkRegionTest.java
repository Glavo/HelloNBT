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

import org.apache.commons.io.IOUtils;
import org.glavo.nbt.MinecraftEdition;
import org.glavo.nbt.TestResources;
import org.glavo.nbt.internal.ChunkRegionHeader;
import org.glavo.nbt.internal.input.InputSource;
import org.glavo.nbt.internal.input.RawDataReader;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ChunkRegionTest {

    @Test
    public void testReadHeader() throws IOException {
        Path resource = TestResources.getResource("/assets/r.-1.-1.mca");

        ChunkRegionHeader header;
        try (var input = new RawDataReader(new InputSource.OfInputStream(Files.newInputStream(resource), true), MinecraftEdition.JAVA_EDITION)) {
            header = ChunkRegionHeader.readHeader(input);
        }

        // TODO
    }

    @Test
    @Disabled
    public void testReadRegion() throws IOException {
        ChunkRegion.readRegion(Path.of("/home/glavo/Projects/HelloNBT/src/test/resources/region/r.-1.-1.mca"));
    }
}
