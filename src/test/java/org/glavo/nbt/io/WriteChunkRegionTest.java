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

import org.glavo.nbt.TestResources;
import org.glavo.nbt.chunk.ChunkRegion;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class WriteChunkRegionTest {
    @ParameterizedTest
    @ValueSource(strings = {"/assets/region/zlib.mca", "/assets/region/lz4.mca"})
    public void testWriteRegion(String path) throws IOException {
        Path resource = TestResources.getResource(path);

        NBTCodec codec = NBTCodec.of();
        ChunkRegion expected = codec.readRegion(resource);

        var buffer = new ByteArrayOutputStream();
        codec.writeRegion(buffer, expected);

        ChunkRegion actual = codec.readRegion(new ByteArrayInputStream(buffer.toByteArray()));
        assertEquals(expected, actual);
    }
}
