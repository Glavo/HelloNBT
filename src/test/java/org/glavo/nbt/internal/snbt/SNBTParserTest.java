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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class SNBTParserTest {

    private static void assertIntegral(long expectedValue, SNBTParser.IntegralType expectedType, boolean expectedUnsigned, String value) {
        assertEquals(new SNBTParser.Token.IntegralToken(expectedValue, expectedType, expectedUnsigned), SNBTParser.parseNumberToken(value));
    }

    @ParameterizedTest
    @EnumSource
    void testParseNumberToken(SNBTParser.IntegralType type) {
        String typeChar = type.name().substring(0, 1);
        String typeCharLower = typeChar.toLowerCase(Locale.ROOT);

        List<String> signedSuffixes = List.of(
                typeChar,
                typeCharLower,
                "s" + typeChar,
                "s" + typeCharLower,
                "S" + typeChar,
                "S" + typeCharLower
        );

        List<String> unsignedSuffixes = List.of(
                "u" + typeChar,
                "u" + typeCharLower,
                "U" + typeChar,
                "U" + typeCharLower
        );


        for (String signedSuffix : signedSuffixes) {
            if (signedSuffix.equalsIgnoreCase("b")) {
                // 0b is binary integer prefix
                continue;
            }

            assertIntegral(0L, type, false, "0" + signedSuffix);
        }

        for (String unsignedSuffix : unsignedSuffixes) {
            assertIntegral(0L, type, true, "0" + unsignedSuffix);
        }
    }
}
