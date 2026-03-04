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
package org.glavo.nbt.internal.input;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class InputSourceTest {
    enum SourceType {
        INPUT_STREAM {
            InputSource createSource(byte[] bytes) {
                return new InputSource.OfInputStream(new ByteArrayInputStream(bytes), false);
            }
        },
        BYTE_CHANNEL {
            static final FileSystem memoryFS = Jimfs.newFileSystem(Configuration.unix());
            static final Path tempDir = memoryFS.getPath("/tmp");

            InputSource createSource(byte[] bytes) throws IOException {
                Files.createDirectories(tempDir);
                Path tempFile = Files.createTempFile(tempDir, "test", ".tmp");
                Files.write(tempFile, bytes);
                return new InputSource.OfByteChannel(FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.DELETE_ON_CLOSE), true);
            }
        };

        abstract InputSource createSource(byte[] bytes) throws IOException;
    }


    @ParameterizedTest
    @EnumSource
    void testReadEmpty(SourceType sourceType) throws IOException {
        byte[] bytes = new byte[0];

        try (InputSource source = sourceType.createSource(bytes)) {
            InputBuffer buffer = InputBuffer.allocate(1024, false, ByteOrder.LITTLE_ENDIAN);

            assertThrows(EOFException.class, () -> source.fillBuffer(buffer, 1));
        }
    }
}
