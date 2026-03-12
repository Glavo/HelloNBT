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
package org.glavo.nbt.io;

import java.util.Objects;

/// Represents the surrounding spaces for SNBT.
public final class SurroundingSpaces {
    /// Surrounding spaces for compact SNBT.
    public static final SurroundingSpaces COMPACT = new SurroundingSpaces(0, 0, 0, 0);

    /// Surrounding spaces for pretty SNBT.
    public static final SurroundingSpaces PRETTY = new SurroundingSpaces(0, 1, 0, 1);

    private final int spacesBeforeColon;
    private final int spacesAfterColon;
    private final int spacesBeforeComma;
    private final int spacesAfterComma;

    private SurroundingSpaces(int spacesBeforeColon, int spacesAfterColon, int spacesBeforeComma, int spacesAfterComma) {
        this.spacesBeforeColon = spacesBeforeColon;
        this.spacesAfterColon = spacesAfterColon;
        this.spacesBeforeComma = spacesBeforeComma;
        this.spacesAfterComma = spacesAfterComma;
    }

    /// The number of spaces before the colon.
    public int getSpacesBeforeColon() {
        return spacesBeforeColon;
    }

    /// The number of spaces after the colon.
    public int getSpacesAfterColon() {
        return spacesAfterColon;
    }

    /// The number of spaces before the comma.
    public int getSpacesBeforeComma() {
        return spacesBeforeComma;
    }

    /// The number of spaces after the comma.
    public int getSpacesAfterComma() {
        return spacesAfterComma;
    }

    @Override
    public int hashCode() {
        return Objects.hash(spacesBeforeColon, spacesAfterColon, spacesBeforeComma, spacesAfterComma);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof SurroundingSpaces that
                && spacesBeforeColon == that.spacesBeforeColon
                && spacesAfterColon == that.spacesAfterColon
                && spacesBeforeComma == that.spacesBeforeComma
                && spacesAfterComma == that.spacesAfterComma;
    }

    @Override
    public String toString() {
        return "Surrounding[spacesBeforeColon=%d, spacesAfterColon=%d, spacesBeforeComma=%d, spacesAfterComma=%d]".formatted(
                spacesBeforeColon, spacesAfterColon, spacesBeforeComma, spacesAfterComma);
    }
}
