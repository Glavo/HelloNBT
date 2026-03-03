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
package org.glavo.nbt.internal;

import org.glavo.nbt.internal.input.DataReader;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.util.Comparator;
import java.util.stream.IntStream;

import static org.glavo.nbt.internal.ChunkUtils.CHUNKS_PRE_REGION;
import static org.glavo.nbt.internal.ChunkUtils.SECTOR_BYTES;

public final class ChunkRegionHeader {
    public static ChunkRegionHeader readHeader(DataReader reader) throws IOException {


        int[] sectorInfo = reader.readIntArray(CHUNKS_PRE_REGION);
        int[] timestamps = reader.readIntArray(CHUNKS_PRE_REGION);

        return new ChunkRegionHeader(sectorInfo, timestamps);
    }

    public final int @Unmodifiable [] sectorInfo;
    public final int @Unmodifiable [] timestamps;
    public final int @Unmodifiable [] localIndexesSortedByOffset;

    public ChunkRegionHeader(int @Unmodifiable [] sectorInfo, int @Unmodifiable [] timestamps) {
        assert sectorInfo.length == CHUNKS_PRE_REGION;
        assert timestamps.length == CHUNKS_PRE_REGION;

        this.sectorInfo = sectorInfo;
        this.timestamps = timestamps;
        this.localIndexesSortedByOffset = IntStream.range(0, CHUNKS_PRE_REGION)
                .boxed()
                .sorted(Comparator.comparingInt(this::getSectorOffset).thenComparingInt(Integer::intValue))
                .mapToInt(Integer::intValue)
                .toArray();
    }

    public int getSectorOffset(int index) {
        return ChunkUtils.getSectorOffset(sectorInfo[index]);
    }

    public int getSectorLength(int index) {
        return ChunkUtils.getSectorLength(sectorInfo[index]);
    }

    public long getSectorOffsetBytes(int index) {
        return (long) getSectorOffset(index) * SECTOR_BYTES;
    }

    public long getSectorLengthBytes(int index) {
        return (long) getSectorLength(index) * SECTOR_BYTES;
    }
}
