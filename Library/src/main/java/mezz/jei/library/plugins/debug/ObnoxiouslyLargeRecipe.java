package mezz.jei.library.plugins.debug;

import mezz.jei.api.constants.ModIds;
import net.minecraft.resources.ResourceLocation;

public class ObnoxiouslyLargeRecipe {
	private static int count = 0;

	private final ResourceLocation recipeId;

	public ObnoxiouslyLargeRecipe() {
		recipeId = ResourceLocation.fromNamespaceAndPath(ModIds.JEI_ID, "number_" + count);
		count++;
	}

	public ResourceLocation getRecipeId() {
		return recipeId;
	}
}

