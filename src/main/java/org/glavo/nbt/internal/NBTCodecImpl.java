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

import org.glavo.nbt.chunk.ChunkRegion;
import org.glavo.nbt.internal.input.*;
import org.glavo.nbt.internal.output.NBTOutput;
import org.glavo.nbt.internal.output.OutputTarget;
import org.glavo.nbt.internal.output.RawDataWriter;
import org.glavo.nbt.io.MinecraftEdition;
import org.glavo.nbt.io.ExternalChunkAccessor;
import org.glavo.nbt.tag.Tag;
import org.glavo.nbt.io.NBTCodec;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.function.Function;

public record NBTCodecImpl(MinecraftEdition edition,
                           Function<Path, ExternalChunkAccessor> externalChunkAccessorFactory) implements NBTCodec {

    private NBTCodecImpl(MinecraftEdition edition) {
        this(edition, ExternalChunkAccessor.defaultFactory());
    }

    public static final NBTCodecImpl JE = new NBTCodecImpl(MinecraftEdition.JAVA_EDITION);
    public static final NBTCodecImpl BE = new NBTCodecImpl(MinecraftEdition.BEDROCK_EDITION);

    @Override
    public MinecraftEdition getEdition() {
        return edition;
    }

    @Override
    public NBTCodec withEdition(MinecraftEdition edition) {
        Objects.requireNonNull(edition, "edition");
        return edition == this.edition ? this : new NBTCodecImpl(edition, externalChunkAccessorFactory);
    }

    @Override
    public Function<Path, ExternalChunkAccessor> getExternalChunkAccessorFactory() {
        return externalChunkAccessorFactory;
    }

    @Override
    public NBTCodec withExternalChunkAccessorFactory(Function<Path, ExternalChunkAccessor> factory) {
        Objects.requireNonNull(factory, "factory");
        return factory == this.externalChunkAccessorFactory ? this : new NBTCodecImpl(edition, factory);
    }

    private Tag check(@Nullable Tag tag) throws IOException {
        if (tag == null) {
            throw new IOException("Unexpected TAG_END");
        }
        return tag;
    }

    public Tag readTag(byte[] array) throws IOException {
        try (var reader = new RawDataReader(new InputSource.OfByteBuffer(array), edition)) {
            return check(NBTInput.readTagAutoDecompress(reader));
        }
    }

    @Override
    public Tag readTag(ByteBuffer buffer) throws IOException {
        try (var reader = new RawDataReader(new InputSource.OfByteBuffer(buffer), edition)) {
            return check(NBTInput.readTagAutoDecompress(reader));
        }
    }

    @Override
    public Tag readTag(InputStream inputStream) throws IOException {
        try (var reader = new RawDataReader(new InputSource.OfInputStream(inputStream, false), edition)) {
            return check(NBTInput.readTagAutoDecompress(reader));
        }
    }

    @Override
    public Tag readTag(ReadableByteChannel channel) throws IOException {
        try (var reader = new RawDataReader(new InputSource.OfByteChannel(channel, false), edition)) {
            return check(NBTInput.readTagAutoDecompress(reader));
        }
    }

    @Override
    public Tag readTag(Path path) throws IOException {
        try (var channel = Files.newByteChannel(path, StandardOpenOption.READ);
             var reader = new RawDataReader(new InputSource.OfByteChannel(channel, false), edition)) {
            return check(NBTInput.readTagAutoDecompress(reader));
        }
    }

    @Override
    public void writeTag(OutputStream outputStream, Tag tag) throws IOException {
        try (var writer = new RawDataWriter(new OutputTarget.OfOutputStream(outputStream, false), edition)) {
            NBTOutput.writeTag(writer, tag);
        }
    }

    @Override
    public void writeTag(WritableByteChannel channel, Tag tag) throws IOException {
        try (var writer = new RawDataWriter(new OutputTarget.OfByteChannel(channel, false), edition)) {
            NBTOutput.writeTag(writer, tag);
        }
    }

    @Override
    public ChunkRegion readRegion(Path path, ExternalChunkAccessor accessor) throws IOException {
        try (var channel = FileChannel.open(path, StandardOpenOption.READ);
             var reader = new RawDataReader(new InputSource.OfByteChannel(channel, true), MinecraftEdition.JAVA_EDITION)) {
            return NBTInput.readRegion(reader, accessor);
        }
    }

    @Override
    public ChunkRegion readRegion(InputStream inputStream, ExternalChunkAccessor accessor) throws IOException {
        Objects.requireNonNull(inputStream, "inputStream");
        try (var reader = new RawDataReader(new InputSource.OfInputStream(inputStream, false), MinecraftEdition.JAVA_EDITION)) {
            return NBTInput.readRegion(reader, accessor);
        }
    }

    @Override
    public ChunkRegion readRegion(ReadableByteChannel channel, ExternalChunkAccessor accessor) throws IOException {
        Objects.requireNonNull(channel, "channel");
        try (var reader = new RawDataReader(new InputSource.OfByteChannel(channel, false), MinecraftEdition.JAVA_EDITION)) {
            return NBTInput.readRegion(reader, accessor);
        }
    }

    @Override
    public void writeRegion(OutputStream outputStream, ChunkRegion region, ExternalChunkAccessor accessor) throws IOException {
        try (var writer = new RawDataWriter(new OutputTarget.OfOutputStream(outputStream, false), MinecraftEdition.JAVA_EDITION)) {
            NBTOutput.writeRegion(writer, region, accessor);
        }
    }

    @Override
    public void writeRegion(SeekableByteChannel channel, ChunkRegion region, ExternalChunkAccessor accessor) throws IOException {
        NBTOutput.writeRegion(channel, region, accessor);
    }

    @Override
    public String toString() {
        return "NBTCodec[edition=%s, externalChunkAccessorFactory=%s]".formatted(edition, externalChunkAccessorFactory);
    }
}
