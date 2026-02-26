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
package org.glavo.nbt.tag;

/// 8 byte floating point tag type.
public final class DoubleTag extends Tag {
    private double value;

    public DoubleTag() {
    }

    public DoubleTag(String name) {
        this.name = name;
    }

    public DoubleTag(String name, double value) {
        this.name = name;
        this.value = value;
    }

    /// Returns the value of the tag.
    public double get() {
        return value;
    }

    /// Sets the value of the tag.
    public void set(double value) {
        this.value = value;
    }

    @Override
    public TagType getType() {
        return TagType.DOUBLE;
    }
}
