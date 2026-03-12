# HelloNBT

[![codecov](https://codecov.io/gh/Glavo/HelloNBT/graph/badge.svg?token=M1XJHATTPN)](https://codecov.io/gh/Glavo/HelloNBT)

A powerful library for reading and writing Minecraft NBT data.

It supports:

- Supports reading and writing NBT (Named Binary Tag), in both big-endian (used by Minecraft Java Edition)
  and little-endian (used by Minecraft Bedrock Edition) formats.
- Supports reading and writing Anvil files and region files, including chunk data over 1MiB.
- Supports reading NBT compressed by GZip, Zlib, and LZ4.
- Supports reading and writing SNBT (Stringified Named Binary Tag).

This library is used for the [Hello! Minecraft Launcher](https://github.com/HMCL-Dev-HMCL).
The API is currently unstable.

(In the early stages of development)

## Download

HelloNBT is currently in the early stages of development. You can get snapshot versions
from [JitPack](https://jitpack.io/#Glavo/HelloNBT).

Gradle (Kotlin DSL):

```kotlin
// Add JitPack repository
repositories {
    maven(url = "https://jitpack.io")
}

// Add HelloNBT dependency
dependencies {
    implementation("com.github.Glavo:HelloNBT:main-SNAPSHOT")
}
```

Maven:
```xml
<!-- Add JitPack repository -->
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<!-- Add HelloNBT dependency -->
<dependency>
    <groupId>com.github.Glavo</groupId>
    <artifactId>HelloNBT</artifactId>
    <version>main-SNAPSHOT</version>
</dependency>
```
