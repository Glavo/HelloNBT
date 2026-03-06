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

import java.util.List;

/// Represents a selector for NBT tags.
///
/// @see <a href="https://minecraft.wiki/w/NBT_path">NBT Path - Minecraft Wiki</a>
public interface NBTSelector {

    <E extends NBTElement> List<E> selectAll(NBTParent<E> parent);

    <E extends NBTElement> E selectFirst(NBTParent<E> parent);

    default <E extends NBTElement> E selectSingle(NBTParent<E> parent) {
        List<E> all = selectAll(parent);
        if (all.size() != 1) {
            throw new IllegalArgumentException("Expected exactly one element, but got " + all.size());
        }
        return all.get(0);
    }
}
