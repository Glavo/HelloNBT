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

/// 4 byte floating point tag type.
public final class FloatTag extends Tag {
    private float value;

    public FloatTag() {
        this("", 0.0f);
    }

    public FloatTag(String name) {
        this(name, 0.0f);
    }

    public FloatTag(String name, float value) {
        super(name);
        this.value = value;
    }

    /// Returns the value of the tag.
    public float get() {
        return value;
    }

    /// Sets the value of the tag.
    public void set(float value) {
        this.value = value;
    }
    
    @Override
    public TagType getType() {
        return TagType.FLOAT;
    }
}
