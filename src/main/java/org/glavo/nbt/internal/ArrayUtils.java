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

    public static int nextCapacity(int currentCapacity, int minCapacity) {
        int growCap;
        if (currentCapacity < 4) {
            growCap = 12;
        } else {
            growCap = currentCapacity + (currentCapacity >> 1);
        }
        return Math.max(growCap, minCapacity);
    }

    private ArrayUtils() {
    }
}
