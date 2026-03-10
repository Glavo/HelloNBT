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
package org.glavo.nbt.internal.selector;

import org.glavo.nbt.tag.CompoundTag;
import org.jetbrains.annotations.Unmodifiable;

public sealed interface NBTSelectorNode {
    @Unmodifiable
    CompoundTag EMPTY_COMPOUND_TAG = new CompoundTag();

    final class RootNode implements NBTSelectorNode {
        public static final RootNode EMPTY = new RootNode(EMPTY_COMPOUND_TAG);

        private final @Unmodifiable CompoundTag tags;

        public RootNode(@Unmodifiable CompoundTag tags) {
            this.tags = tags;
        }
    }

    final class NamedSubTagNode implements NBTSelectorNode {
        private final String name;

        public NamedSubTagNode(String name) {
            this.name = name;
        }
    }

    final class NamedSubCompoundTagNode implements NBTSelectorNode {
        private final String name;
        private final @Unmodifiable CompoundTag tags;

        public NamedSubCompoundTagNode(String name, @Unmodifiable CompoundTag tags) {
            this.name = name;
            this.tags = tags;
        }
    }

    final class AllElements implements NBTSelectorNode {
        public static final AllElements INSTANCE = new AllElements();
    }

    final class IndexNode implements NBTSelectorNode {
        private final int index;

        public IndexNode(int index) {
            this.index = index;
        }
    }

    final class CompoundElementsNode implements NBTSelectorNode {
        public static final CompoundElementsNode EMPTY = new CompoundElementsNode(EMPTY_COMPOUND_TAG);

        private final @Unmodifiable CompoundTag tags;

        public CompoundElementsNode(@Unmodifiable CompoundTag tags) {
            this.tags = tags;
        }
    }
}
