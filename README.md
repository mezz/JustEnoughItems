JustEnoughItems (JEI)
===============
JustEnoughItems is an Item and Recipe viewing mod which only contains the basics.

This means:
 * not a coremod
 * no dependencies
 * nothing but items and recipes

Download
===============
JEI has versions built for Minecraft 1.8 and 1.8.8.
[Grab the latest versions from curseforge.](http://minecraft.curseforge.com/projects/just-enough-items-jei/files)

Developing Addons
===============
Add to your build.gradle:
```gradle
repositories {
  maven {
    url "http://dvs1.progwml6.com/files/maven"
  }
}

minecraft {
  useDepAts = true
}

dependencies {
  deobfCompile "mezz.jei:jei_<MINECRAFT-VERSION>:<JEI-VERSION>"
}
```

`<MINECRAFT-VERSION>` and `<JEI-VERSION>` can be found on [CurseForge](http://minecraft.curseforge.com/projects/just-enough-items-jei/files), check the file name of the version you want.

Developer FAQ
===
Q: Why isn't my gradle working?
 * A: Do not put the `repositories` section in your `buildscript` area. It should be its own section, outside of `buildscript`.
 * A: See the build.gradle of any of the many mods that include JEI integration (listed in the "Plugin Examples" section below).

Q: Why aren't all of my items showing up in the item list? They are shown in the creative menu.
 * A: If your item is missing its subtypes, make sure your item returns true from `item.getHasSubtypes()`.
 * A: JEI hides items with missing item models (purple and black). You can show them by changing the config option in the Advanced section.

Q: Where is the deobfuscated jar for developers?
 * A: Use the regular jar file. Recent Forge 1.8.9 versions automatically deobfuscate jars in the mods directory.
 * A: Get it from Maven as described in the "Developing Addons" section above.

Still have questions? Join [#JEI on esper.net IRC](http://webchat.esper.net/?nick=JEIGithub...&channels=JEI&prompt=1) for questions or anything else!

Plugin Examples (how to use the API)
===
The API is pretty well documented but it can still be confusing to get started without an example.
Thankfully there are many working examples:
 * [Vanilla Minecraft](https://github.com/mezz/JustEnoughItems/tree/1.8.9/src/main/java/mezz/jei/plugins/vanilla) (written as a plugin in JEI to serve as a canonical example)
 * [Blood Magic](https://github.com/WayofTime/BloodMagic/tree/1.8-Rewrite/src/main/java/WayofTime/bloodmagic/compat/jei)
 * [Botania](https://github.com/williewillus/Botania/tree/MC18/src/main/java/vazkii/botania/client/integration/jei)
 * [TechReborn](https://github.com/TechReborn/TechReborn/tree/1.8.9/src/main/java/techreborn/compat/jei)
 * [IronBackpacks](https://github.com/gr8pefish/IronBackpacks/tree/master-1.8.9/src/main/java/gr8pefish/ironbackpacks/integration/jei)
 * [Mantle](https://github.com/SlimeKnights/Mantle/pull/49/files) (How to move JEI's item list out of the way of extra gui tabs)
 * [PneumaticCraft](https://github.com/MineMaarten/PneumaticCraft/tree/MC1.8.8/src/pneumaticCraft/common/thirdparty/jei)
