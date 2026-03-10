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
package org.glavo.nbt.internal;

public final class ArrayUtils {
    public static byte[] EMPTY_BYTE_ARRAY = new byte[0];
    public static int[] EMPTY_INT_ARRAY = new int[0];
    public static long[] EMPTY_LONG_ARRAY = new long[0];

    public static int nextCapacity(int currentCapacity) {
        if (currentCapacity < 4) {
            return 12;
        } else {
            return currentCapacity + (currentCapacity >> 1);
        }
    }

    private ArrayUtils() {
    }
}
