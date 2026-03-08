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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SNBTParserTest {

    private static void assertIntegral(long expectedValue, SNBTParser.IntegralType expectedType, boolean expectedUnsigned, String value) {
        assertEquals(new SNBTParser.Token.IntegralToken(expectedValue, expectedType, expectedUnsigned), SNBTParser.parseNumberToken(value), value);
    }

    @Test
    void testParseNumberToken() {
        assertThrows(IllegalArgumentException.class, () -> SNBTParser.parseNumberToken(""));
        assertThrows(IllegalArgumentException.class, () -> SNBTParser.parseNumberToken("_"));
        assertThrows(IllegalArgumentException.class, () -> SNBTParser.parseNumberToken("123_"));
        assertThrows(IllegalArgumentException.class, () -> SNBTParser.parseNumberToken("0b"));
        assertThrows(IllegalArgumentException.class, () -> SNBTParser.parseNumberToken("0x"));

        assertIntegral(0x0bL, SNBTParser.IntegralType.INT, true, "0x0b");
        assertIntegral(0x0bL, SNBTParser.IntegralType.INT, true, "0X0b");

        assertIntegral(123L, SNBTParser.IntegralType.INT, false, "123");
        assertIntegral(123L, SNBTParser.IntegralType.INT, false, "1_2_3");
    }

    @ParameterizedTest
    @EnumSource
    void testParseNumberToken(SNBTParser.IntegralType type) {
        List<String> defaultSuffixes = List.of(type.name().substring(0, 1), type.name().substring(0, 1).toLowerCase(Locale.ROOT));
        List<String> signedSuffixes = defaultSuffixes.stream().flatMap(suffix -> Stream.of("s" + suffix, "S" + suffix)).toList();
        List<String> unsignedSuffixes = defaultSuffixes.stream().flatMap(suffix -> Stream.of("u" + suffix, "U" + suffix)).toList();

        for (String suffix : defaultSuffixes) {
            if (type != SNBTParser.IntegralType.BYTE) { // 0b is binary integer prefix, and 'b' is a hex digit
                assertIntegral(0L, type, false, "0" + suffix);
                assertIntegral(0L, type, true, "0x0" + suffix);
                assertIntegral(0L, type, true, "0X0" + suffix);
            }

            assertIntegral(0L, type, true, "0b0" + suffix);
            assertIntegral(0L, type, true, "0B0" + suffix);

        }

        for (String signedSuffix : signedSuffixes) {
            assertIntegral(0L, type, false, "0" + signedSuffix);
            assertIntegral(0L, type, false, "0b0" + signedSuffix);
            assertIntegral(0L, type, false, "0B0" + signedSuffix);
            assertIntegral(0L, type, false, "0x0" + signedSuffix);
            assertIntegral(0L, type, false, "0X0" + signedSuffix);
        }

        for (String unsignedSuffix : unsignedSuffixes) {
            assertIntegral(0L, type, true, "0" + unsignedSuffix);
            assertIntegral(0L, type, true, "0b0" + unsignedSuffix);
            assertIntegral(0L, type, true, "0B0" + unsignedSuffix);
            assertIntegral(0L, type, true, "0x0" + unsignedSuffix);
            assertIntegral(0L, type, true, "0X0" + unsignedSuffix);
        }
    }
}
