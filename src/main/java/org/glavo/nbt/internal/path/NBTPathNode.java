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
package org.glavo.nbt.internal.path;

import org.glavo.nbt.tag.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public sealed interface NBTPathNode {
    @Unmodifiable
    CompoundTag EMPTY_COMPOUND_TAG = new CompoundTag();

    default boolean needDot() {
        return this instanceof NamedSubTag || this instanceof NamedSubCompoundTag;
    }

    void appendTo(StringBuilder builder);

    record Root(@Unmodifiable CompoundTag tags) implements NBTPathNode {
        public static final Root EMPTY = new Root(EMPTY_COMPOUND_TAG);

        @Override
        public void appendTo(StringBuilder builder) {
            builder.append(tags); // TODO: SNBT
        }
    }

    record NamedSubTag(String name) implements NBTPathNode {
        @Override
        public void appendTo(StringBuilder builder) {
            builder.append(name); // TODO: SNBT
        }
    }

    record NamedSubCompoundTag(String name, @Unmodifiable CompoundTag tags) implements NBTPathNode {
        @Override
        public void appendTo(StringBuilder builder) {
            builder.append(name).append(tags); // TODO: SNBT
        }
    }

    final class AllElements implements NBTPathNode {
        public static final AllElements INSTANCE = new AllElements();

        @Override
        public void appendTo(StringBuilder builder) {
            builder.append("[]");
        }
    }

    record Index(int index) implements NBTPathNode {
        @Override
        public void appendTo(StringBuilder builder) {
            builder.append('[').append(index).append(']');
        }
    }

    record CompoundElements(@Unmodifiable CompoundTag tags) implements NBTPathNode {
        public static final CompoundElements EMPTY = new CompoundElements(EMPTY_COMPOUND_TAG);

        @Override
        public void appendTo(StringBuilder builder) {
            builder.append("[{").append(tags).append("}]"); // TODO: SNBT
        }
    }
}
