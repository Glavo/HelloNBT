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
package org.glavo.nbt.io;

import org.glavo.nbt.internal.OversizedChunkLocators;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

/// Locator for oversized chunk files.
///
/// Since Minecraft 19w34a, if a chunk is larger than 1 MiB, it will be stored in a separate file named `c.<chunkX>.<chunkZ>.mcc` in the same directory as the region file.
/// This interface provides a way to locate the oversized chunk file for a given chunk in an Anvil file.
public interface OversizedChunkLocator {

    /// Returns a locator that not support reading or writing oversized chunk files.
    static OversizedChunkLocator emptyLocator() {
        return OversizedChunkLocators.EMPTY;
    }

    /// Returns a locator that locates the oversized chunk file based on the region file name and chunk local coordinates.
    static OversizedChunkLocator defaultLocator() {
        return OversizedChunkLocators.DEFAULT;
    }

    /// Opens an input stream for reading the oversized chunk file for a given chunk in an Anvil file.
    ///
    /// If the locator does not support reading, this method returns `null`.
    ///
    /// @param source      The source file path of the Anvil file.
    /// @param chunkLocalX The local X coordinate of the chunk in the region file.
    /// @param chunkLocalZ The local Z coordinate of the chunk in the region file.
    @Nullable InputStream openInputStream(Path source, int chunkLocalX, int chunkLocalZ) throws IOException;

    /// Opens an output stream for writing the oversized chunk file for a given chunk in an Anvil file.
    ///
    /// If the locator does not support writing, this method returns `null`.
    ///
    /// @param source      The source file path of the Anvil file.
    /// @param chunkLocalX The local X coordinate of the chunk in the region file.
    /// @param chunkLocalZ The local Z coordinate of the chunk in the region file.
    @Nullable OutputStream openOutputStream(Path source, int chunkLocalX, int chunkLocalZ) throws IOException;
}
