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
import org.glavo.nbt.tag.CompoundTag;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;

public final class NBTSelectorParser {
    private final String input;
    private final int beginIndex;
    private final int endIndex;
    private int cursor;
    private final List<NBTSelectorImpl.Node> nodes = new ArrayList<>();
    private @Nullable StringBuilder buffer;

    public NBTSelectorParser(String input) {
        this.input = input;
        this.beginIndex = 0;
        this.endIndex = input.length();

        if (beginIndex == endIndex) {
            throw new IllegalArgumentException("Empty input");
        }

        //noinspection DataFlowIssue
        this.cursor = beginIndex;
    }

    private void ensureNotEOF() {
        if (cursor >= endIndex) {
            throw new IllegalArgumentException("Unexpected end of input");
        }
    }

    private void skipWhiteSpace() {
        while (cursor < endIndex) {
            int ch = input.codePointAt(cursor);
            if (Character.isWhitespace(ch)) {
                cursor += Character.charCount(ch);
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

//    private String scanName() {
//        skipWhiteSpace();
//        ensureNotEOF();
//
//        int ch = input.codePointAt(cursor);
//        if (ch == '\'') {
//            // TODO
//        } else if (ch == '"') {
//
//        } else {
//
//        }
//    }
//
//    private @Unmodifiable CompoundTag scanTags() {
//        skipWhiteSpace();
//        ensureNotEOF();
//
//        int ch = input.codePointAt(cursor);
//        if (ch == '}') {
//            cursor += 1;
//            return NBTSelectorImpl.EMPTY_COMPOUND_TAG;
//        }
//
//    }

    public NBTSelector parse() {
        skipWhiteSpace();
        if (cursor == endIndex) {
            return finish();
        }

//        int ch = input.codePointAt(cursor);
//        if (ch == '{') {
//            if (!nodes.isEmpty()) {
//                throw new IllegalArgumentException("Unexpected root node at index " + cursor);
//            }
//
//            cursor += Character.charCount(ch);
//            skipWhiteSpace();
//            ensureNotEOF();
//
//            CompoundTag tags = scanTags();
//            if (tags.isEmpty()) {
//                nodes.add(NBTSelectorImpl.RootNode.EMPTY);
//            } else {
//                nodes.add(new NBTSelectorImpl.RootNode(tags));
//            }
//        } else if (ch == '[') {
//            cursor += Character.charCount(ch);
//            ensureNotEOF();
//
//        } else if (ch == '.') {
//            cursor += Character.charCount(ch);
//            ensureNotEOF();
//
//        } else {
//            // TODO
//        }

        throw new UnsupportedOperationException("Not implemented"); // TODO
    }
}
