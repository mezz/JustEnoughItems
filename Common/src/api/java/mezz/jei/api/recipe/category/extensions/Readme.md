## Recipe Category Extensions

These extensions are an opt-in feature to allow other plugins to extend your recipe categories.

By default, the vanilla crafting recipe category is extendable using this system. Plugins can extend the vanilla crafting recipe category by implementing `IModPlugin.registerVanillaCategoryExtensions`.

### Compared to 1.12.2

The rough equivalent in 1.12.2 and earlier was adding Recipe Wrappers that would be handled by Recipe Categories added by other mods.  
Since very few plugins were written to extend others, and since Recipe Wrappers take up some memory, that system was dropped in favor of this opt-in one.  
The only Recipe Category that was regularly extended was Vanilla Crafting, so it is extendable by default and exposed to mod plugins so they can extend it.