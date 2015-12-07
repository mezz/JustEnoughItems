package mezz.jei.gui;

import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Called before the Recipes Gui is init.
 *
 * Creating a delay here can harm the perceived responsiveness of JEI, this event
 * will be removed when a better workaround can be found for Thaumcraft's research gating.
 */
public class RecipesGuiInitEvent extends Event {
}
