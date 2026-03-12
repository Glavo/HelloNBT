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
package org.glavo.nbt.internal.snbt;

import org.glavo.nbt.io.SNBTCodec;
import org.glavo.nbt.tag.*;

import java.io.IOException;

public final class SNBTWriter {
    private final SNBTCodec codec;
    private final Appendable appendable;
    private int identLevel = 0;

    public SNBTWriter(SNBTCodec codec, Appendable appendable) {
        this.codec = codec;
        this.appendable = appendable;
    }

    private void writeStringValue(String value) throws IOException {
        char quoteChar = codec.getQuoteStrategy().getQuoteChar(value);

        if (quoteChar == '\0') {
            appendable.append(value);
        } else {
            appendable.append(quoteChar);
            ((EscapeStrategies) codec.getEscapeStrategy()).appendString(appendable, quoteChar, value, 0, value.length());
            appendable.append(quoteChar);
        }
    }

    private void writeNewLine() throws IOException {
        appendable.append('\n');
    }

    private void writeIndent() throws IOException {
        for (int i = 0; i < identLevel; i++) {
            appendable.append(codec.getIndentation());
        }
    }

    private void writeSpaces(int count) throws IOException {
        for (int i = 0; i < count; i++) {
            appendable.append(' ');
        }
    }

    private void writeCompoundTag(CompoundTag tag) throws IOException {
        boolean shouldBreakLines = codec.getCompoundTagLineBreakStrategy().shouldBreakLines(tag);

        appendable.append('{');

        identLevel++;

        for (int i = 0, end = tag.size(); i < end; i++) {
            Tag subTag = tag.getTag(i);

            if (shouldBreakLines) {
                writeNewLine();
                writeIndent();
            }

            writeStringValue(subTag.getName());

            writeSpaces(codec.getSurroundingSpaces().getSpacesBeforeColon());
            appendable.append(':');
            writeSpaces(codec.getSurroundingSpaces().getSpacesAfterColon());

            writeTag(subTag);

            if (i < end - 1) {
                writeSpaces(codec.getSurroundingSpaces().getSpacesBeforeComma());
                appendable.append(',');
                if (!shouldBreakLines) {
                    writeSpaces(codec.getSurroundingSpaces().getSpacesAfterComma());
                }
            }
        }

        identLevel--;

        if (shouldBreakLines) {
            writeNewLine();
            writeIndent();
        }

        appendable.append('}');
    }

    private void writeListTag(ListTag<?> tag) throws IOException {
        boolean shouldBreakLines = codec.getListTagLineBreakStrategy().shouldBreakLines(tag);

        appendable.append('[');

        identLevel++;

        for (int i = 0, end = tag.size(); i < end; i++) {
            Tag subTag = tag.getTag(i);

            if (shouldBreakLines) {
                writeNewLine();
                writeIndent();
            }

            writeTag(subTag);

            if (i < end - 1) {
                writeSpaces(codec.getSurroundingSpaces().getSpacesBeforeComma());
                appendable.append(',');
                if (!shouldBreakLines) {
                    writeSpaces(codec.getSurroundingSpaces().getSpacesAfterComma());
                }
            }
        }

        identLevel--;

        if (shouldBreakLines) {
            writeNewLine();
            writeIndent();
        }

        appendable.append(']');
    }

    private <E extends Number, T extends ValueTag<E>> void writeArrayTag(ArrayTag<E, T, ?, ?> tag) throws IOException {
        boolean shouldBreakLines = codec.getArrayTagLineBreakStrategy().shouldBreakLines(tag);

        if (tag instanceof ByteArrayTag) {
            appendable.append("[B;");
        } else if (tag instanceof IntArrayTag) {
            appendable.append("[I;");
        } else if (tag instanceof LongArrayTag) {
            appendable.append("[L;");
        } else {
            throw new AssertionError("Unsupported array tag: " + tag);
        }

        identLevel++;

        for (int i = 0, end = tag.size(); i < end; i++) {
            if (shouldBreakLines) {
                writeNewLine();
                writeIndent();
            }

            appendable.append(tag.getAsString(i));

            if (i < end - 1) {
                writeSpaces(codec.getSurroundingSpaces().getSpacesBeforeComma());
                appendable.append(',');
                if (!shouldBreakLines) {
                    writeSpaces(codec.getSurroundingSpaces().getSpacesAfterComma());
                }
            }
        }

        identLevel--;

        if (shouldBreakLines) {
            writeNewLine();
            writeIndent();
        }

        appendable.append(']');
    }

    public void writeTag(Tag tag) throws IOException {
        if (tag instanceof CompoundTag compoundTag) {
            writeCompoundTag(compoundTag);
        } else if (tag instanceof ListTag<?> listTag) {
            writeListTag(listTag);
        } else if (tag instanceof ArrayTag<?, ?, ?, ?> arrayTag) {
            writeArrayTag(arrayTag);
        } else if (tag instanceof StringTag stringTag) {
            writeStringValue(stringTag.getValue());
        } else if (tag instanceof ValueTag<?> valueTag) {
            String string = valueTag.getAsString();
            if (string.equals("-Infinity")) {
                // For Float.NEGATIVE_INFINITY and Double.NEGATIVE_INFINITY,
                // we need to use quotes to avoid writing corrupted string values.
                writeStringValue(string);
            } else {
                appendable.append(string);
            }
        }
    }
}
