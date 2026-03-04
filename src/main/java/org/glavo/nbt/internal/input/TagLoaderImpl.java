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
package org.glavo.nbt.internal.input;

import org.glavo.nbt.MinecraftEdition;
import org.glavo.nbt.internal.Access;
import org.glavo.nbt.tag.Tag;
import org.glavo.nbt.tag.TagLoader;
import org.glavo.nbt.tag.TagType;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

public sealed abstract class TagLoaderImpl<T extends Tag, S> implements TagLoader<T, S> {

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

    protected final Class<T> tagClass;
    protected final MinecraftEdition edition;
    protected final boolean autoDecompress;

    protected TagLoaderImpl(Class<T> tagClass, MinecraftEdition edition, boolean autoDecompress) {
        this.tagClass = Objects.requireNonNull(tagClass, "tagClass");
        this.edition = Objects.requireNonNull(edition, "edition");
        this.autoDecompress = autoDecompress;
    }

    protected final T check(@Nullable Tag tag) throws IOException {
        if (tag == null) {
            throw new IOException("Unexpected TAG_END");
        }
        try {
            return tagClass.cast(tag);
        } catch (ClassCastException e) {
            throw new IOException("Unexpected tag type: " + tag);
        }
    }

    @Override
    public String toString() {
        return "TagLoader.%s[tagClass=%s, edition=%s, autoDecompress=%s]".formatted(
                getClass().getSimpleName(),
                tagClass.getSimpleName(),
                edition,
                autoDecompress
        );
    }

    private static abstract class AbstractBuilder<T extends Tag, S> implements TagLoader.Builder<T, S> {
        protected final Class<T> tagClass;
        protected MinecraftEdition edition = MinecraftEdition.JAVA_EDITION;
        protected boolean autoDecompress = true;

        private AbstractBuilder(Class<T> tagClass) {
            this.tagClass = tagClass;
        }

        @Override
        public AbstractBuilder<T, S> setEdition(MinecraftEdition edition) {
            this.edition = Objects.requireNonNull(edition, "edition");
            return this;
        }

        @Override
        public AbstractBuilder<T, S> setAutoDecompress(boolean autoDecompress) {
            this.autoDecompress = autoDecompress;
            return this;
        }
    }

    public static final class OfInputStream<T extends Tag> extends TagLoaderImpl<T, InputStream> {

        public static final OfInputStream<Tag> DEFAULT = new OfInputStream<>(Tag.class, MinecraftEdition.JAVA_EDITION, true);

        public OfInputStream(Class<T> tagClass, MinecraftEdition edition, boolean autoDecompress) {
            super(tagClass, edition, autoDecompress);
        }

        @Override
        public T load(InputStream source) throws IOException {
            try (var reader = new RawDataReader(new InputSource.OfInputStream(source, false), edition)) {
                return check(autoDecompress ? readTagAutoDecompress(reader) : readTag(reader));
            }
        }

        public static final class Builder<T extends Tag> extends AbstractBuilder<T, InputStream> {
            public Builder(Class<T> tagClass) {
                super(tagClass);
            }

            @Override
            public OfInputStream<T> build() {
                return new OfInputStream<>(tagClass, edition, autoDecompress);
            }
        }
    }

    public static final class OfByteChannel<T extends Tag> extends TagLoaderImpl<T, ReadableByteChannel> {

        public static final OfByteChannel<Tag> DEFAULT = new OfByteChannel<>(Tag.class, MinecraftEdition.JAVA_EDITION, true);

        public OfByteChannel(Class<T> tagClass, MinecraftEdition edition, boolean autoDecompress) {
            super(tagClass, edition, autoDecompress);
        }

        @Override
        public T load(ReadableByteChannel source) throws IOException {
            try (var reader = new RawDataReader(new InputSource.OfByteChannel(source, false), edition)) {
                return check(autoDecompress ? readTagAutoDecompress(reader) : readTag(reader));
            }
        }

        public static final class Builder<T extends Tag> extends AbstractBuilder<T, ReadableByteChannel> {
            public Builder(Class<T> tagClass) {
                super(tagClass);
            }

            @Override
            public OfByteChannel<T> build() {
                return new OfByteChannel<>(tagClass, edition, autoDecompress);
            }
        }
    }

    public static final class OfPath<T extends Tag> extends TagLoaderImpl<T, Path> {

        public static final OfPath<Tag> DEFAULT = new OfPath<>(Tag.class, MinecraftEdition.JAVA_EDITION, true);

        public OfPath(Class<T> tagClass, MinecraftEdition edition, boolean autoDecompress) {
            super(tagClass, edition, autoDecompress);
        }

        @Override
        public T load(Path source) throws IOException {
            try (var channel = Files.newByteChannel(source, StandardOpenOption.READ);
                 var reader = new RawDataReader(new InputSource.OfByteChannel(channel, false), edition)) {
                return check(autoDecompress ? readTagAutoDecompress(reader) : readTag(reader));
            }
        }

        public static final class Builder<T extends Tag> extends AbstractBuilder<T, Path> {
            public Builder(Class<T> tagClass) {
                super(tagClass);
            }

            @Override
            public OfPath<T> build() {
                return new OfPath<>(tagClass, edition, autoDecompress);
            }
        }
    }
}
