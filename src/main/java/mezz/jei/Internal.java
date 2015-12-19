package mezz.jei;

import mezz.jei.api.IItemRegistry;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IRecipeRegistry;

/** For JEI internal use only, these are normally accessed from the API. */
public class Internal {
	private static IJeiHelpers helpers;
	private static IRecipeRegistry recipeRegistry;
	private static IItemRegistry itemRegistry;

	private Internal() {

	}

	public static IJeiHelpers getHelpers() {
		return helpers;
	}

	public static void setHelpers(IJeiHelpers helpers) {
		Internal.helpers = helpers;
	}

	public static IRecipeRegistry getRecipeRegistry() {
		return recipeRegistry;
	}

	public static void setRecipeRegistry(IRecipeRegistry recipeRegistry) {
		Internal.recipeRegistry = recipeRegistry;
	}

	public static IItemRegistry getItemRegistry() {
		return itemRegistry;
	}

	public static void setItemRegistry(IItemRegistry itemRegistry) {
		Internal.itemRegistry = itemRegistry;
	}
}
