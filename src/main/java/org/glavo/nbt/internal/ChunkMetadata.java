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

public record ChunkMetadata(int localIndex, int sectorOffset, int sectorLength, int timestamp) {

    public ChunkMetadata {
        assert localIndex >= 0 && localIndex < ChunkUtils.CHUNKS_PRE_REGION : "Invalid local index: " + localIndex;
        assert sectorOffset >= 0 : "Sector offset must be non-negative: " + sectorOffset;
        assert sectorLength >= 0 : "Size must be non-negative: " + sectorLength;
    }
}
