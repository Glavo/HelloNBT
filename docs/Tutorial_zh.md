# HelloNBT 教程

本文将详细介绍 HelloNBT 的用法。

如果你初次使用 HelloNBT，可以先阅读 [快速入门](/docs/QuickStart_zh.md) 来快速了解 HelloNBT 的基本用法。

## 基础

HelloNBT 提供了对 NBT 元素树的基本抽象。

所有 NBT 元素都实现了 `NBTElement` 接口，该接口有以下实现：

- `NBTElement`: 代表任意 NBT 元素。
    - `ChunkRegion`: 代表一个区域（Region）。一个区域中包含 32 x 32 个区块。
    - `Chunk`: 代表一个区块。区块中会包含其最后更新时间，以及一个可选的表示其 NBT 数据的 `CompoundTag`。
    - `Tag`: 代表一个 NBT Tag。
        - `ParentTag`: 代表可以包含其他 NBT Tag 的 NBT Tag。
            - `CompoundTag`: 代表一个复合 NBT Tag。其中可以包含多个具有不同名称的 NBT Tag。
            - `ListTag`: 代表一个列表 NBT Tag。所有 NBT Tag 不包含名称，且类型相同。
            - `ArrayTag`: 代表一个数组 NBT Tag。所有 NBT Tag 不包含名称，且类型相同。
                - `ByteArrayTag`: 代表包含一系列 `ByteTag` 的数组 NBT Tag。
                - `IntArrayTag`: 代表包含一系列 `IntTag` 的数组 NBT Tag。Minecraft 也会用它来存储 UUID。
                - `LongArrayTag`: 代表包含一系列 `LongTag` 的数组 NBT Tag。
        - `ValueTag`: 代表包含一个值的 NBT Tag。
            - `ByteTag`: 包含一个单字节整数的 NBT Tag。Minecraft 也会用它来存储布尔值。
            - `ShortTag`: 包含一个双字节整数的 NBT Tag。
            - `IntTag`: 包含一个四字节整数的 NBT Tag。
            - `LongTag`: 包含一个八字节整数的 NBT Tag。
            - `FloatTag`: 包含一个单精度浮点数的 NBT Tag。
            - `DoubleTag`: 包含一个双精度浮点数的 NBT Tag。
            - `StringTag`: 包含一个字符串的 NBT Tag。

其中 `ChunkRegion`、`Chunk`、`CompoundTag`、`ListTag`、`ByteArrayTag`、`IntArrayTag` 和 `LongArrayTag` 可以包含其他 `NBTElement` 作为子元素，
这些可以作为父元素的 `NBTElement` 都实现了 `NBTParent` 接口。

一个没有父元素的 `NBTElement` 称为根元素（Root Element）。根元素及其所有子元素共同构成了一个 NBT 树。

每个 `NBTElement` 只能处于一个 NBT 树中，且在树中是唯一的。

尝试将一个 `NBTElement` 添加到另一个 `NBTParent` 中时，它会先被从当前父元素中移除，再添加到新的父元素中。

所有 `NBTElement` 都支持 `clone` 方法进行复制。
`clone` 方法会递归复制其所有子元素，返回的副本将与原元素具有相同的内容，但是与原元素完全独立，且没有父元素。
用户可以 `clone` 出的元素添加至其他 `NBTParent` 中。

