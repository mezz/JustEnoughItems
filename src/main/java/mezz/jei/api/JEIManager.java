package mezz.jei.api;

import mezz.jei.api.gui.IGuiHelper;
import mezz.jei.api.recipe.IRecipeRegistry;

public class JEIManager {

	/* The following are available after JEI's preInit stage */
	public static IRecipeRegistry recipeRegistry;
	public static IGuiHelper guiHelper;

}
