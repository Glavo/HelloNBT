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

import org.glavo.nbt.tag.Tag;

import java.lang.invoke.MethodHandles;

public final class NBTIO {

    static final Tag.Unsafe TAG_UNSAFE;

    static {
        try {
            TAG_UNSAFE = Tag.Unsafe.getInstance(MethodHandles.lookup());
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    private NBTIO() {
    }
}
