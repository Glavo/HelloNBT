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

import java.util.Comparator;
import java.util.List;

public final class ChunkMetadataTable {
    private static final Comparator<ChunkMetadata> BY_OFFSET = Comparator.comparingInt(ChunkMetadata::sectorOffset).thenComparingInt(ChunkMetadata::localIndex);

    private final List<ChunkMetadata> sortedByLocalIndex;
    private final List<ChunkMetadata> sortedBySectorOffset;

    public ChunkMetadataTable(List<ChunkMetadata> sortedByLocalIndex) {
        if (sortedByLocalIndex.size() != ChunkUtils.CHUNKS_PRE_REGION) {
            throw new IllegalArgumentException("Invalid chunk metadata array length: " + sortedByLocalIndex.size());
        }

        this.sortedByLocalIndex = List.copyOf(sortedByLocalIndex);
        this.sortedBySectorOffset = sortedByLocalIndex.stream().sorted(BY_OFFSET).toList();


        int prevSectorEnd = 0;
        for (ChunkMetadata chunkMetadata : sortedBySectorOffset) {
            if (chunkMetadata.sectorOffset() < prevSectorEnd) {
                throw new IllegalArgumentException("Overlapping chunk metadata: " + chunkMetadata);
            }

            prevSectorEnd = chunkMetadata.sectorOffset() + chunkMetadata.sectorLength();
        }
    }

    public List<ChunkMetadata> getSortedByLocalIndex() {
        return sortedByLocalIndex;
    }

    public List<ChunkMetadata> getSortedBySectorOffset() {
        return sortedBySectorOffset;
    }
}
