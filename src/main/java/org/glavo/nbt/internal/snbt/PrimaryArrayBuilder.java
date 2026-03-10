/*
 * Hello Minecraft! Launcher
 * Copyright (C) 2026 huangyuhui <huanghongxun2008@126.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.glavo.nbt.internal.snbt;

import org.glavo.nbt.tag.ArrayTag;
import org.glavo.nbt.tag.ByteArrayTag;
import org.glavo.nbt.tag.IntArrayTag;
import org.glavo.nbt.tag.LongArrayTag;

sealed abstract class PrimaryArrayBuilder<T extends ArrayTag<?, ?, A, ?>, A> {
    protected final T tag = newTag();

    protected abstract IntegralType getIntegralType();

    protected abstract T newTag();

    protected abstract void add(long value);

    public void add(Token.IntegralToken token) {
        long value = token.value();
        getIntegralType().check(value, token.unsigned());
        add(value);
    }

    public T build() {
        return tag;
    }

    static final class OfByte extends PrimaryArrayBuilder<ByteArrayTag, byte[]> {

        @Override
        protected IntegralType getIntegralType() {
            return IntegralType.BYTE;
        }

        @Override
        protected ByteArrayTag newTag() {
            return new ByteArrayTag();
        }

        @Override
        protected void add(long value) {
            tag.add((byte) value);
        }
    }

    static final class OfInt extends PrimaryArrayBuilder<IntArrayTag, int[]> {

        @Override
        protected IntegralType getIntegralType() {
            return IntegralType.INT;

        }

        @Override
        protected IntArrayTag newTag() {
            return new IntArrayTag();
        }

        @Override
        protected void add(long value) {
            tag.add((int) value);
        }
    }

    static final class OfLong extends PrimaryArrayBuilder<LongArrayTag, long[]> {
        @Override
        protected IntegralType getIntegralType() {
            return IntegralType.LONG;
        }

        @Override
        protected LongArrayTag newTag() {
            return new LongArrayTag();
        }

        @Override
        protected void add(long value) {
            tag.add(value);
        }
    }
}
