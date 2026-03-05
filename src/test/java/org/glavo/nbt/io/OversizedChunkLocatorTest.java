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

import org.glavo.nbt.internal.ChunkUtils;
import org.glavo.nbt.internal.OversizedChunkLocators;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public final class OversizedChunkLocatorTest {

    @Test
    void testEmptyLocator() {
        var locator = assertInstanceOf(OversizedChunkLocators.class, OversizedChunkLocator.emptyLocator());

        Path source = Path.of("r.0.0.mca");
        for (int i = 0; i < ChunkUtils.CHUNKS_PER_REGION_SIDE; i++) {
            for (int j = 0; j < ChunkUtils.CHUNKS_PER_REGION_SIDE; j++) {
                assertNull(locator.locate(source, i, j));
            }
        }

        assertThrows(IndexOutOfBoundsException.class, () -> locator.locate(source, -1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> locator.locate(source, 0, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> locator.locate(source, ChunkUtils.CHUNKS_PER_REGION_SIDE, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> locator.locate(source, 32, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> locator.locate(source, 0, 32));
    }

    @Test
    void testDefaultLocator() {
        var locator = assertInstanceOf(OversizedChunkLocators.class, OversizedChunkLocator.defaultLocator());

        assertEquals(Path.of("c.0.0.mcc"), locator.locate(Path.of("r.0.0.mca"), 0, 0));
        assertEquals(Path.of("c.31.31.mcc"), locator.locate(Path.of("r.0.0.mca"), 31, 31));
        assertEquals(Path.of("c.37.70.mcc"), locator.locate(Path.of("r.1.2.mca"), 5, 6));
        assertEquals(Path.of("c.-27.70.mcc"), locator.locate(Path.of("r.-1.2.mca"), 5, 6));
    }
}
