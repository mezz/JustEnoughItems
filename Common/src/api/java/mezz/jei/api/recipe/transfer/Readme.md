# Recipe Transfer Handlers

Recipe Transfer Handlers let players click a [+] button on a recipe to make JEI move the items from the player's inventory directly into the crafting area.

JEI has a large amount of flexibility for Recipe Transfer Handlers, so just about any machine or process can have one.

## Basic Handlers

A Basic handler is all that's needed for most machine GUIs. You define the slots for the recipe, and the slots for the inventory (including player inventory) and JEI will create a handler. Magic!

## Complete Control

If your machine uses ghost slots for crafting, or has a massive storage system behind it, you'll need to implement your own `IRecipeTransferHandler` and register it. Since JEI is client-side, you will have to send your own packet to the server and handle the complexities involved with properly transferring the recipe and  not deleting or duplicating items.

There are helpers in `IRecipeTransferHandlerHelper` to aid this implementation.

## Universal Handlers

A universal handler is one step above complete control, it can handle every category of recipe instead of just one.
This is useful for mods with recipe pattern encoding, for automated recipe systems.
Things like Applied Energistics pattern encoding make use of this, so that any recipe can be encoded easily.