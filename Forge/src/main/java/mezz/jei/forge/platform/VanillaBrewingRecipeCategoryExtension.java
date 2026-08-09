package mezz.jei.forge.platform;

import mezz.jei.api.recipe.category.extensions.vanilla.brewing.IBrewingCategoryExtension;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.library.util.BrewingRecipeMakerCommon;
import net.minecraftforge.common.brewing.VanillaBrewingRecipe;

import java.util.ArrayList;
import java.util.List;

public final class VanillaBrewingRecipeCategoryExtension implements IBrewingCategoryExtension<VanillaBrewingRecipe> {
	private final IIngredientManager ingredientManager;

	public VanillaBrewingRecipeCategoryExtension(IIngredientManager ingredientManager) {
		this.ingredientManager = ingredientManager;
	}

	@Override
	public List<IJeiBrewingRecipe> getBrewingRecipes(
		VanillaBrewingRecipe brewingRecipe,
		IVanillaRecipeFactory vanillaRecipeFactory
	) {
		return new ArrayList<>(
			BrewingRecipeMakerCommon.getVanillaBrewingRecipes(
				vanillaRecipeFactory,
				ingredientManager,
				brewingRecipe::getOutput
			)
		);
	}
}
