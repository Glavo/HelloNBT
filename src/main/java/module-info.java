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

/// Base module for the [HelloNBT](https://github.com/Glavo/HelloNBT).
module org.glavo.nbt {
    requires static org.jetbrains.annotations;
    requires static org.lz4.java;

    exports org.glavo.nbt;
    exports org.glavo.nbt.chunk;
    exports org.glavo.nbt.tag;
    exports org.glavo.nbt.io;
}