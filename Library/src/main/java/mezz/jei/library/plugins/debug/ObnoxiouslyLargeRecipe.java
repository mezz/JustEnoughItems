package mezz.jei.library.plugins.debug;

import mezz.jei.api.constants.ModIds;
import net.minecraft.resources.Identifier;

public class ObnoxiouslyLargeRecipe {
	private static int count = 0;

	private final Identifier recipeId;

	public ObnoxiouslyLargeRecipe() {
		recipeId = Identifier.fromNamespaceAndPath(ModIds.JEI_ID, "number_" + count);
		count++;
	}

	public Identifier getRecipeId() {
		return recipeId;
	}
}
