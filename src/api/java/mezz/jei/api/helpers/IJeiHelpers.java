package mezz.jei.api.helpers;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.recipe.IFocusFactory;

/**
 * {@link IJeiHelpers} provides helpers and tools for addon mods.
 *
 * An instance is passed to your {@link IModPlugin}'s registration methods.
 */
public interface IJeiHelpers {
	/**
	 * Helps with the implementation of GUIs.
	 */
	IGuiHelper getGuiHelper();

	/**
	 * Helps with getting itemStacks from recipes.
	 */
	IStackHelper getStackHelper();

	/**
	 * Helps with getting the mod name from a mod ID.
	 */
	IModIdHelper getModIdHelper();

	/**
	 * Helps with creating focuses.
	 *
	 * @since 9.4.0
	 */
	IFocusFactory getFocusFactory();
}
