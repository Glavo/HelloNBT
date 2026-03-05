/*
 * Hello Minecraft! Launcher
 * Copyright (C) 2026 huangyuhui <huanghongxun2008@126.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.glavo.nbt.io;

import org.glavo.nbt.internal.ChunkUtils;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@FunctionalInterface
public interface OversizedChunkLocator<T> {
    static <T> OversizedChunkLocator<T> emptyLocator() {
        return (source, localX, localZ) -> null;
    }

    static OversizedChunkLocator<Path> defaultLocator() {
        final class Holder {
            static final Pattern FILE_NAME_PATTERN = Pattern.compile("r\\.(?<regionX>-?\\d+)\\.(?<regionZ>-?\\d+)\\.mca");
        }

        return (source, chunkLocalX, chunkLocalZ) -> {
            Objects.checkIndex(chunkLocalX, ChunkUtils.CHUNKS_PER_REGION_SIDE);
            Objects.checkIndex(chunkLocalZ, ChunkUtils.CHUNKS_PER_REGION_SIDE);


            Path fileName = source.getFileName();
            if (fileName == null) {
                return null;
            }

            String name = fileName.toString();
            Matcher matcher = Holder.FILE_NAME_PATTERN.matcher(name);
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
        };
    }

    @Nullable T locate(T source, int chunkLocalX, int chunkLocalZ);
}
