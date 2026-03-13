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

import org.glavo.nbt.NBTPath;
import org.glavo.nbt.internal.snbt.SNBTWriter;
import org.glavo.nbt.io.SNBTCodec;
import org.glavo.nbt.tag.CompoundTag;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;

public sealed interface NBTPathNode extends NBTPath {

    @Unmodifiable
    CompoundTag EMPTY_COMPOUND_TAG = new CompoundTag();

    private static String toString(NBTPathNode node) {
        var writer = new SNBTWriter<>(SNBTCodec.ofCompact(), new StringBuilder());
        try {
            node.appendTo(writer);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return writer.getAppendable().toString();
    }

    default boolean needDot() {
        return this instanceof NamedSubTag || this instanceof NamedSubCompoundTag;
    }

    void appendTo(SNBTWriter<StringBuilder> writer) throws IOException;

    record Root(@Unmodifiable CompoundTag tags) implements NBTPathNode {
        public static final Root EMPTY = new Root(EMPTY_COMPOUND_TAG);

        @Override
        public void appendTo(SNBTWriter<StringBuilder> writer) throws IOException {
            writer.writeTag(tags);
        }

        @Override
        public String toString() {
            return tags.isEmpty() ? "{}" : NBTPathNode.toString(this);
        }
    }

    record NamedSubTag(String name) implements NBTPathNode {
        @Override
        public void appendTo(SNBTWriter<StringBuilder> writer) throws IOException {
            writer.writeTagName(name);
        }

        @Override
        public String toString() {
            return NBTPathNode.toString(this);
        }
    }

    record NamedSubCompoundTag(String name, @Unmodifiable CompoundTag tags) implements NBTPathNode {
        @Override
        public void appendTo(SNBTWriter<StringBuilder> writer) throws IOException {
            writer.writeTagName(name);
            writer.writeTag(tags);
        }

        @Override
        public String toString() {
            return NBTPathNode.toString(this);
        }
    }

    record AllElements() implements NBTPathNode {
        public static final AllElements INSTANCE = new AllElements();

        @Override
        public void appendTo(SNBTWriter<StringBuilder> writer) throws IOException {
            writer.getAppendable().append("[]");
        }

        @Override
        public String toString() {
            return "[]";
        }
    }

    record Index(long index) implements NBTPathNode {
        @Override
        public void appendTo(SNBTWriter<StringBuilder> writer) throws IOException {
            writer.getAppendable().append("[").append(index).append(']');
        }

        @Override
        public String toString() {
            return "[" + index + "]";
        }
    }

    record CompoundElements(@Unmodifiable CompoundTag tags) implements NBTPathNode {
        public static final CompoundElements EMPTY = new CompoundElements(EMPTY_COMPOUND_TAG);

        @Override
        public void appendTo(SNBTWriter<StringBuilder> writer) throws IOException {
            writer.getAppendable().append('[');
            writer.writeTag(tags);
            writer.getAppendable().append(']');
        }

        @Override
        public String toString() {
            return NBTPathNode.toString(this);
        }
    }
}
