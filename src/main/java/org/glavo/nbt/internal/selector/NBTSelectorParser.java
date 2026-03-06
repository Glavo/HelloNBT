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
package org.glavo.nbt.internal.selector;

import org.glavo.nbt.NBTSelector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class NBTSelectorParser {
    private final CharSequence input;
    private final int beginIndex;
    private final int endIndex;
    private int cursor;
    private final List<NBTSelector.Node> nodes = new ArrayList<>();

    public NBTSelectorParser(CharSequence input, int beginIndex, int endIndex) {
        Objects.checkFromToIndex(beginIndex, endIndex, input.length());

        if (beginIndex == endIndex) {
            throw new IllegalArgumentException("Empty input");
        }

        this.input = input;
        this.beginIndex = beginIndex;
        this.endIndex = endIndex;
        this.cursor = beginIndex;
    }

    private void skipWhiteSpace() {
        while (cursor < endIndex) {
            if (Character.isWhitespace(input.charAt(cursor))) {
                cursor++;
            } else {
                break;
            }
        }
    }

    private NBTSelector finish() {
        if (nodes.isEmpty()) {
            throw new IllegalArgumentException("Empty input");
        }

        throw new UnsupportedOperationException("Not implemented"); // TODO
    }

    public NBTSelector parse() {
        skipWhiteSpace();
        if (cursor == endIndex) {
            return finish();
        }


        throw new UnsupportedOperationException("Not implemented"); // TODO
    }
}
