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

import org.glavo.nbt.NBTPath;
import org.glavo.nbt.internal.snbt.SNBTWriter;
import org.glavo.nbt.io.SNBTCodec;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.util.Arrays;

public final class NBTCompositePath implements NBTPath {
    private final NBTPathNode @Unmodifiable [] nodes;
    private @Nullable String cachedString;

    public NBTCompositePath(NBTPathNode @Unmodifiable [] nodes) {
        assert nodes.length > 1;
        this.nodes = nodes;
    }

    public NBTPathNode @Unmodifiable [] getNodes() {
        return nodes;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof NBTCompositePath that
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


