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
package org.glavo.nbt;

import org.glavo.nbt.internal.path.NBTPathImpl;
import org.glavo.nbt.internal.snbt.SNBTParser;
import org.glavo.nbt.tag.Tag;
import org.jetbrains.annotations.Contract;

import java.util.stream.Stream;

/// @see <a href="https://minecraft.wiki/w/NBT_path">NBT Path - Minecraft Wiki</a>
public sealed interface NBTPath<T extends Tag> permits NBTPathImpl {

    /// Parse a NBT path from a string.
    @Contract(pure = true)
    static NBTPath<Tag> of(String path) throws IllegalArgumentException {
        return new SNBTParser(path, 0, path.length()).nextPath();
    }

    /// Selects the tags that match the path from the given parent.
    @Contract(pure = true)
    Stream<Tag> select(NBTParent<?> parent);

}
