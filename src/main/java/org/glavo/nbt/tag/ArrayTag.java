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

/// An abstract tag that contains an ordered list of values.
public sealed abstract class ArrayTag<E extends Number> extends Tag implements Iterable<E>
        permits ByteArrayTag, IntArrayTag, LongArrayTag {

    protected ArrayTag(String name) {
        super(name);
    }

    /// Returns `true` if the array is empty; otherwise, returns `false`.
    public final boolean isEmpty() {
        return size() == 0;
    }

    /// Returns the size of the array.
    public abstract int size();
}
