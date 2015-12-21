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

Join [#JEI on esper.net IRC](http://webchat.esper.net/?nick=JEIGithub...&channels=JEI&prompt=1) for questions or anything else.
