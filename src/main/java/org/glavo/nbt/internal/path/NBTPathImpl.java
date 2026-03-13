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
package org.glavo.nbt.internal.path;

import org.glavo.nbt.NBTParent;
import org.glavo.nbt.NBTPath;
import org.glavo.nbt.chunk.Chunk;
import org.glavo.nbt.chunk.ChunkRegion;
import org.glavo.nbt.internal.snbt.SNBTWriter;
import org.glavo.nbt.io.SNBTCodec;
import org.glavo.nbt.tag.CompoundTag;
import org.glavo.nbt.tag.Tag;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public final class NBTPathImpl implements NBTPath {
    private final NBTPathNode @Unmodifiable [] nodes;
    private @Nullable String cachedString;

    public NBTPathImpl(NBTPathNode @Unmodifiable [] nodes) {
        assert nodes.length > 0;
        this.nodes = nodes;
    }

    public NBTPathNode @Unmodifiable [] getNodes() {
        return nodes;
    }

    @SuppressWarnings("unchecked")
    private Stream<Tag> select(NBTParent<?> parent) {
        Stream<? extends Tag> tags;
        if (parent instanceof CompoundTag compoundTag) {
            tags = Stream.of(compoundTag);
        } else if (parent instanceof ChunkRegion chunkRegion) {
            tags = chunkRegion.stream()
                    .flatMap(chunk -> Stream.<Tag>ofNullable(chunk.getRootTag()));
        } else if (parent instanceof Chunk chunk) {
            tags = chunk.getRootTag() != null ? Stream.of(chunk.getRootTag()) : Stream.empty();
        } else {
            tags = Stream.empty();
        }

        for (NBTPathNode node : nodes) {
            tags = node.operate(tags);
        }
        return (Stream<Tag>) tags;
    }

    @Override
    public List<Tag> selectAll(NBTParent<?> parent) {
        return select(parent).toList();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof NBTPathImpl that
                && Arrays.equals(nodes, that.nodes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(nodes);
    }

    @Override
    public String toString() {
        if (cachedString == null) {
            var writer = new SNBTWriter<>(SNBTCodec.ofCompact(), new StringBuilder());

            boolean first = true;
            for (NBTPathNode node : nodes) {
                if (first) {
                    first = false;
                } else if (node.needDot()) {
                    writer.getAppendable().append('.');
                }

                try {
                    node.appendTo(writer);
                } catch (IOException e) {
                    throw new AssertionError(e);
                }
            }
            cachedString = writer.getAppendable().toString();
        }

        return cachedString;
    }
}


