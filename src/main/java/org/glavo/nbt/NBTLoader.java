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

import java.io.IOException;

/// The interface for loading NBT elements.
///
/// @param <E> The type of NBT element to load.
/// @param <S> The type of the source of the NBT element.
@FunctionalInterface
public
interface NBTLoader<E extends NBTElement, S> {

    /// Loads an NBT element from the given source.
    E load(S source) throws IOException;

    /// The interface for building loaders for NBT elements.
    ///
    /// @param <E> The type of NBT element to load.
    /// @param <S> The type of the source of the NBT element.
    @FunctionalInterface
    interface Builder<E extends NBTElement, S> {
        NBTLoader<E, S> build();
    }
}
