# Changelog

## 0.2.0 (In development)

- New API `NBTCodec#writeRegion(Path, ChunkRegion)` and `NBTCodec#writeRegion(Path, ChunkRegion, ExternalChunkAccessor)`
  for writing chunk regions to files.
- New API `NBTCodec#byteSize(Tag)` and `NBTCodec#contentByteSize(Tag)` for calculating the encoded byte size of NBT
  tags.
- New API `NBTCodec#writeTag(ByteBuffer, Tag)` for writing NBT tags to a byte buffer.
- New API `NBTCodec#writeTagToByteArray(Tag)` for writing NBT tags to a byte array.
- New API `ArrayTag#getElementType()` for getting the element type of array tags.

## 0.1.0 (2026-03-16)

- Initial release
