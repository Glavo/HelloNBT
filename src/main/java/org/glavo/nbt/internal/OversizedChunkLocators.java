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

import org.glavo.nbt.io.OversizedChunkLocator;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum OversizedChunkLocators implements OversizedChunkLocator {
    DEFAULT {
        private static final Pattern FILE_NAME_PATTERN = Pattern.compile("r\\.(?<regionX>-?\\d+)\\.(?<regionZ>-?\\d+)\\.mca");

        public @Nullable Path locate(Path source, int chunkLocalX, int chunkLocalZ) {
            Objects.checkIndex(chunkLocalX, ChunkUtils.CHUNKS_PER_REGION_SIDE);
            Objects.checkIndex(chunkLocalZ, ChunkUtils.CHUNKS_PER_REGION_SIDE);

            Path fileName = source.getFileName();
            if (fileName == null) {
                return null;
            }

            String name = fileName.toString();
            Matcher matcher = FILE_NAME_PATTERN.matcher(name);
            if (matcher.matches()) {
                int regionX;
                int regionZ;

                try {
                    regionX = Integer.parseInt(matcher.group("regionX"));
                    regionZ = Integer.parseInt(matcher.group("regionZ"));
                } catch (NumberFormatException e) {
                    return null;
                }

                return source.resolveSibling("c.%d.%d.mcc".formatted(
                        (regionX << ChunkUtils.CHUNKS_PER_REGION_SIDE_SHIFT) + chunkLocalX,
                        (regionZ << ChunkUtils.CHUNKS_PER_REGION_SIDE_SHIFT) + chunkLocalZ
                ));
            } else {
                return null;
            }
        }
    },
    EMPTY {
        @Override
        public @Nullable Path locate(Path source, int chunkLocalX, int chunkLocalZ) {
            Objects.requireNonNull(source);
            Objects.checkIndex(chunkLocalX, ChunkUtils.CHUNKS_PER_REGION_SIDE);
            Objects.checkIndex(chunkLocalZ, ChunkUtils.CHUNKS_PER_REGION_SIDE);
            return null;
        }
    };

    public abstract @Nullable Path locate(Path source, int chunkLocalX, int chunkLocalZ);

    @Override
    public @Nullable InputStream openInputStream(Path source, int chunkLocalX, int chunkLocalZ) throws IOException {
        Path file = locate(source, chunkLocalX, chunkLocalZ);
        return file != null ? Files.newInputStream(file) : null;
    }

    @Override
    public @Nullable OutputStream openOutputStream(Path source, int chunkLocalX, int chunkLocalZ) throws IOException {
        Path file = locate(source, chunkLocalX, chunkLocalZ);
        if (file != null) {
            Files.createDirectories(file.toAbsolutePath().normalize().getParent());
            return Files.newOutputStream(file, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } else {
            return null;
        }
    }

    OversizedChunkLocators() {
    }
}
