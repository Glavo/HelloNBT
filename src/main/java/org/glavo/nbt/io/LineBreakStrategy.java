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

import org.glavo.nbt.tag.ParentTag;
import org.jetbrains.annotations.ApiStatus;

/// Line break strategy for SNBT.
public final class LineBreakStrategy {

    /// Always break lines.
    public static final LineBreakStrategy ALWAYS = new LineBreakStrategy(0);

    /// Break lines if the size is greater than 0.
    public static final LineBreakStrategy NOT_EMPTY = new LineBreakStrategy(1);

    /// Never break lines.
    public static final LineBreakStrategy NEVER = new LineBreakStrategy(Long.MAX_VALUE);

    /// Break lines if the size is at least the given threshold.
    public static LineBreakStrategy atLeast(long threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException("threshold must be non-negative");
        }

        if (threshold == 0) {
            return ALWAYS;
        } else if (threshold == Long.MAX_VALUE) {
            return NEVER;
        } else {
            return new LineBreakStrategy(threshold);
        }
    }

    private final long threshold;

    private LineBreakStrategy(long threshold) {
        this.threshold = threshold;
    }

    /// Returns true if the elements of the tag need to be broken into separate lines.
    public boolean shouldBreak(ParentTag<?> tag) {
        return tag.size() >= threshold;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(threshold);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof LineBreakStrategy that && threshold == that.threshold;
    }

    @Override
    public String toString() {
        if (threshold == Long.MAX_VALUE) {
            return "LineBreakStrategy.NEVER";
        } else if (threshold == 0) {
            return "LineBreakStrategy.ALWAYS";
        } else {
            return "LineBreakStrategy.atLeast(" + threshold + ")";
        }
    }
}
