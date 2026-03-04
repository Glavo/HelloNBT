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
package org.glavo.nbt.tag;

import org.glavo.nbt.TestResources;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.*;

public final class TagLoaderTest {

    enum Loader {
        BYTE_ARRAY {
            @Override
            <T extends Tag> T load(Path file, Class<T> tagClass) throws IOException {
                return TagLoader.ofByteArray(tagClass).load(Files.readAllBytes(file));
            }
        },
        HEAP_BYTE_BUFFER {
            @Override
            <T extends Tag> T load(Path file, Class<T> tagClass) throws IOException {
                return TagLoader.ofByteBuffer(tagClass).load(ByteBuffer.wrap(Files.readAllBytes(file)));
            }
        },
        DIRECT_BYTE_BUFFER {
            @Override
            <T extends Tag> T load(Path file, Class<T> tagClass) throws IOException {
                return TagLoader.ofByteBuffer(tagClass).load(ByteBuffer.allocateDirect((int) Files.size(file)).put(Files.readAllBytes(file)).flip());
            }
        },
        INPUT_STREAM {
            @Override
            <T extends Tag> T load(Path file, Class<T> tagClass) throws IOException {
                return TagLoader.ofPath(tagClass).load(file);
            }
        },
        FILE_CHANNEL {
            @Override
            <T extends Tag> T load(Path file, Class<T> tagClass) throws IOException {
                try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ)) {
                    return TagLoader.ofByteChannel(tagClass).load(channel);
                }
            }
        },
        READABLE_BYTE_CHANNEL {
            @Override
            <T extends Tag> T load(Path file, Class<T> tagClass) throws IOException {
                try (ReadableByteChannel channel = Channels.newChannel(Files.newInputStream(file))) {
                    return TagLoader.ofByteChannel(tagClass).load(channel);
                }
            }
        },
        PATH {
            @Override
            <T extends Tag> T load(Path file, Class<T> tagClass) throws IOException {
                return TagLoader.ofPath(tagClass).load(file);
            }
        };

        abstract <T extends Tag> T load(Path file, Class<T> tagClass) throws IOException;
    }

    /// Loads a level.dat file (compressed with gzip)
    @ParameterizedTest
    @EnumSource
    void testLoadLevelDat(Loader loader) throws IOException {
        Path levelDatPath = TestResources.getResource("/assets/nbt/level.dat");
        CompoundTag levelDat = loader.load(levelDatPath, CompoundTag.class);
        assertEquals("", levelDat.getName());
        assertEquals(1, levelDat.size());

        Tag dataTag = levelDat.get("Data");
        assertNotNull(dataTag);
        assertInstanceOf(CompoundTag.class, dataTag);
    }

    /// Loads an uncompressed level.dat file
    @ParameterizedTest
    @EnumSource
    void testLoadLevelDatRaw(Loader loader) throws IOException {
        Path levelDatPath = TestResources.getResource("/assets/nbt/level.dat.raw");
        CompoundTag levelDat = loader.load(levelDatPath, CompoundTag.class);
        assertEquals("", levelDat.getName());
        assertEquals(1, levelDat.size());

        Tag dataTag = levelDat.get("Data");
        assertNotNull(dataTag);
        assertInstanceOf(CompoundTag.class, dataTag);
    }
}
