[![Build Status](https://dvs1.progwml6.com/jenkins/job/JustEnoughItems/badge/icon)](https://dvs1.progwml6.com/jenkins/job/JustEnoughItems) [![](http://cf.way2muchnoise.eu/full_just-enough-items-jei_downloads.svg)](http://minecraft.curseforge.com/projects/just-enough-items-jei) [![](http://cf.way2muchnoise.eu/versions/Available%20For%20Minecraft_just-enough-items-jei_all.svg)](http://minecraft.curseforge.com/projects/just-enough-items-jei)

[JustEnoughItems (JEI)](http://minecraft.curseforge.com/projects/just-enough-items-jei/files)
===============
JustEnoughItems is an Item and Recipe viewing mod with a focus on stability, performance, and ease of use.

This means:
 * just items and recipes
 * clean API for developers
 * not a coremod, no dependencies other than Forge

Developing Addons
===============
Add to your build.gradle:
```gradle
repositories {
  maven {
    // location of the maven that hosts JEI files
    url "http://dvs1.progwml6.com/files/maven"
  }
}

dependencies {
  // compile against the JEI API
  deobfCompile "mezz.jei:jei_${mcversion}:${jei_version}:api"
  // at runtime, use the full JEI jar
  runtime "mezz.jei:jei_${mcversion}:${jei_version}"
}
```

`${mcversion}` and `${jei_version}` can be found [here](http://dvs1.progwml6.com/files/maven/mezz/jei/) or on [CurseForge](http://minecraft.curseforge.com/projects/just-enough-items-jei/files), check the file name of the version you want.  

Note that the `repositories` section belongs outside the `buildscript` area.

Have questions? Join [#JEI on esper.net IRC](http://webchat.esper.net/?nick=JEIGithub...&channels=JEI&prompt=1) for questions or anything else!

Plugin Examples (how to use the API)
===
The API is pretty well documented but it can still be confusing to get started without an example.
Thankfully there are many working examples:
 * [Vanilla Minecraft](https://github.com/mezz/JustEnoughItems/tree/1.10/src/main/java/mezz/jei/plugins/vanilla) (written as a plugin in JEI)
 * [Blood Magic](https://github.com/WayofTime/BloodMagic/tree/1.9/src/main/java/WayofTime/bloodmagic/compat/jei)
 * [Botania](https://github.com/Vazkii/Botania/tree/master/src/main/java/vazkii/botania/client/integration/jei)
 * [EnderIO](https://github.com/SleepyTrousers/EnderIO/tree/1.10/src/main/java/crazypants/enderio/integration/jei)
 * [Forestry (Apiculture)](https://github.com/ForestryMC/ForestryMC/blob/mc-1.10/src/main/java/forestry/apiculture/compat/ApicultureJeiPlugin.java)
 * [Forestry (Machines)](https://github.com/ForestryMC/ForestryMC/tree/mc-1.10/src/main/java/forestry/factory/recipes/jei)
 * [Immersive Engineering](https://github.com/BluSunrize/ImmersiveEngineering/tree/master/src/main/java/blusunrize/immersiveengineering/common/util/compat/jei)
 * [Refined Storage](https://github.com/raoulvdberge/refinedstorage/tree/mc1.10/src/main/java/refinedstorage/integration/jei)
 * [RFTools](https://github.com/McJty/RFTools/tree/1.10/src/main/java/mcjty/rftools/jei)
 * [Simple Covers](https://github.com/bdew/covers/tree/mc1.10.2/src/net/bdew/covers/compat/jei) (Scala)
 * [TechReborn](https://github.com/TechReborn/TechReborn/tree/1.10.2/src/main/java/techreborn/compat/jei)
