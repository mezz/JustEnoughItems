[![Build Status](https://dvs1.progwml6.com/jenkins/job/JustEnoughItems/badge/icon?style=plastic)](https://dvs1.progwml6.com/jenkins/job/JustEnoughItems/?style=plastic)

JustEnoughItems (JEI)
===============
[![](http://cf.way2muchnoise.eu/full_just-enough-items-jei_downloads.svg)](http://minecraft.curseforge.com/projects/just-enough-items-jei) [![](http://cf.way2muchnoise.eu/versions/Available%20For%20Minecraft_just-enough-items-jei_all.svg)]((http://minecraft.curseforge.com/projects/just-enough-items-jei))

JustEnoughItems is an Item and Recipe viewing mod with a focus on stability, performance, and ease of use.

This means:
 * not a coremod
 * no dependencies other than Forge
 * clean API for developers
 * nothing but items and recipes

Download
===============
[Grab the latest versions from curseforge.](http://minecraft.curseforge.com/projects/just-enough-items-jei/files)

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

Developer FAQ
===
Q: Why isn't my gradle working?
 * A: Do not put the `repositories` section in your `buildscript` area. It should be its own section, outside of `buildscript`.
 * A: See the build.gradle of any of the many mods that include JEI integration (listed in the "Plugin Examples" section below).

Q: Why aren't all of my items showing up in the item list? They are shown in the creative menu.
 * A: JEI hides items with missing item models (purple and black). You can show them by changing the config option in the in-game config menu.

Q: Where is the deobfuscated jar for developers?
 * A: Use the regular jar file. Forge 1.8.9+ versions automatically deobfuscate jars in the /mods/ directory.

Still have questions? Join [#JEI on esper.net IRC](http://webchat.esper.net/?nick=JEIGithub...&channels=JEI&prompt=1) for questions or anything else!

Plugin Examples (how to use the API)
===
The API is pretty well documented but it can still be confusing to get started without an example.
Thankfully there are many working examples:
 * [Vanilla Minecraft](https://github.com/mezz/JustEnoughItems/tree/1.10/src/main/java/mezz/jei/plugins/vanilla) (written as a plugin in JEI to serve as a canonical example)
 * [Iron Backpacks](https://github.com/gr8pefish/IronBackpacks/tree/master-1.10/src/main/java/gr8pefish/ironbackpacks/integration/jei)
 * [Blood Magic](https://github.com/WayofTime/BloodMagic/tree/1.9/src/main/java/WayofTime/bloodmagic/compat/jei)
 * [Botania](https://github.com/williewillus/Botania/tree/MC19/src/main/java/vazkii/botania/client/integration/jei)
 * [TechReborn](https://github.com/TechReborn/TechReborn/tree/1.10/src/main/java/techreborn/compat/jei)
 * [Mantle](https://github.com/SlimeKnights/Mantle/blob/master/src/main/java/slimeknights/mantle/util/JeiPlugin.java) (How to move JEI's item list out of the way of extra gui tabs)
 * [Forestry (Apiculture)](https://github.com/ForestryMC/ForestryMC/blob/mc-1.10/src/main/java/forestry/apiculture/compat/ApicultureJeiPlugin.java)
 * [Forestry (Machines)](https://github.com/ForestryMC/ForestryMC/tree/mc-1.10/src/main/java/forestry/factory/recipes/jei)
 * [Simple Covers (Scala)](https://github.com/bdew/covers/tree/mc1.10.2/src/net/bdew/covers/compat/jei)
 
