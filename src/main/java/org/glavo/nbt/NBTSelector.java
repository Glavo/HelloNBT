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

import org.glavo.nbt.internal.selector.NBTSelectorParser;

/// Represents a selector for NBT tags.
///
/// @see <a href="https://minecraft.wiki/w/NBT_path">NBT Path - Minecraft Wiki</a>
public final class NBTSelector {


    /// @throws IllegalArgumentException if the input is not a valid NBT selector
    public static NBTSelector parse(String input) {
        return new NBTSelectorParser(input).parse();
    }
}
