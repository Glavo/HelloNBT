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

import org.jetbrains.annotations.Contract;

/// Base class for tags that hold a single value.
public sealed abstract class ValueTag<V> extends Tag
        permits ByteTag, ShortTag, IntTag, LongTag, FloatTag, DoubleTag, StringTag, ArrayTag {
    protected ValueTag(String name) {
        super(name);
    }

    /// Returns the value of the tag.
    @Contract(pure = true)
    public abstract V getValue();

    /// Returns the value of the tag as a string.
    @Contract(pure = true)
    public abstract String getAsString();

    /// Sets the value of the tag.
    @Contract(mutates = "this")
    public abstract void setValue(V value);

    @Override
    @Contract(pure = true)
    public abstract ValueTag<V> clone();
}
