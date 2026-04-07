package org.glavo.nbt.io;

import org.glavo.nbt.TestResources;
import org.glavo.nbt.tag.CompoundTag;
import org.glavo.nbt.tag.ListTag;
import org.glavo.nbt.tag.TagType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public final class SNBTCodecTest {

    @Test
    void testNewLineSeparatedSNBT() throws IOException {
        Path resource = TestResources.getResource("/assets/nbt/applied_energistics.snbt");
        // expected failure
        // the vanilla-flavored SNBT should not split compounds and lists with '\n'.
        Assertions.assertThrows(IOException.class, () -> {
            try(var reader = Files.newBufferedReader(resource, StandardCharsets.UTF_8)) {
                SNBTCodec.of().readTag(reader);
            }
        });

        try(var reader = Files.newBufferedReader(resource, StandardCharsets.UTF_8)) {
            var tag = SNBTCodec.of().withAllowNewLineAsSeparator(true).readTag(reader);
            var compound = assertInstanceOf(CompoundTag.class, tag);
            assertEquals("28726466D987C725", compound.getString("group"));
            var quests = assertInstanceOf(ListTag.class, compound.get("quests"));
            assert quests != null : "ensured by assertInstanceOf";
            assertEquals(TagType.COMPOUND, quests.getElementType());
        }
    }

}
