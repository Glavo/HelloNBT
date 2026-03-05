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
package org.glavo.nbt.internal;

import org.glavo.nbt.io.MinecraftEdition;
import org.glavo.nbt.internal.input.DataReader;
import org.glavo.nbt.internal.input.DecompressStreamDataReader;
import org.glavo.nbt.internal.input.InputSource;
import org.glavo.nbt.internal.input.RawDataReader;
import org.glavo.nbt.tag.Tag;
import org.glavo.nbt.io.NBTCodec;
import org.glavo.nbt.tag.TagType;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

public record NBTCodecImpl(MinecraftEdition edition) implements NBTCodec {

    public NBTCodecImpl {
        Objects.requireNonNull(edition, "edition");
    }

    public static final NBTCodecImpl DEFAULT = new NBTCodecImpl(MinecraftEdition.JAVA_EDITION);

    public static @Nullable Tag readTag(DataReader reader) throws IOException {
        byte tagByte = reader.readByte();
        if (tagByte == 0) {
            return null;
        }

        var type = TagType.getById(tagByte);
        if (type == null) {
            throw new IOException("Invalid tag type: %02x".formatted(Byte.toUnsignedInt(tagByte)));
        }

        Tag tag = type.createTag();
        tag.setName(reader.readString());
        Access.TAG.readContent(tag, reader);
        return tag;
    }

    public static @Nullable Tag readTagAutoDecompress(RawDataReader reader) throws IOException {
        byte tagByte = reader.lookAheadByte();

        // GZip Magic Number: 0x1F 0x8B 0x08
        if (tagByte == 0x1F) {
            try (var decompressReader = DecompressStreamDataReader.newGZipDataReader(reader, -1)) {
                return readTag(decompressReader);
            }
        }

        // LZ4 Magic Number: "LZ4Block"
        if (tagByte == 'L') {
            try (var decompressReader = DecompressStreamDataReader.newLZ4DataReader(reader, -1)) {
                return readTag(decompressReader);
            }
        }

        return readTag(reader);
    }

    @Override
    public MinecraftEdition getEdition() {
        return edition;
    }

    private Tag check(@Nullable Tag tag) throws IOException {
        if (tag == null) {
            throw new IOException("Unexpected TAG_END");
        }
        return tag;
    }

    public Tag readTag(byte[] array) throws IOException {
        try (var reader = new RawDataReader(new InputSource.OfByteBuffer(array), edition)) {
            return check(readTagAutoDecompress(reader));
        }
    }

    @Override
    public Tag readTag(ByteBuffer buffer) throws IOException {
        try (var reader = new RawDataReader(new InputSource.OfByteBuffer(buffer), edition)) {
            return check(readTagAutoDecompress(reader));
        }
    }

    @Override
    public Tag readTag(InputStream inputStream) throws IOException {
        try (var reader = new RawDataReader(new InputSource.OfInputStream(inputStream, false), edition)) {
            return check(readTagAutoDecompress(reader));
        }
    }

    @Override
    public Tag readTag(ReadableByteChannel channel) throws IOException {
        try (var reader = new RawDataReader(new InputSource.OfByteChannel(channel, false), edition)) {
            return check(readTagAutoDecompress(reader));
        }
    }

    @Override
    public Tag readTag(Path path) throws IOException {
        try (var channel = Files.newByteChannel(path, StandardOpenOption.READ);
             var reader = new RawDataReader(new InputSource.OfByteChannel(channel, false), edition)) {
            return check(readTagAutoDecompress(reader));
        }
    }

    public static final class BuilderImpl implements Builder {
        private MinecraftEdition edition = MinecraftEdition.JAVA_EDITION;

        @Override
        public BuilderImpl setEdition(MinecraftEdition edition) {
            this.edition = Objects.requireNonNull(edition, "edition");
            return this;
        }

        @Override
        public NBTCodec build() {
            return new NBTCodecImpl(edition);
        }
    }
}
