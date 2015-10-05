package mezz.jei.plugins.vanilla.crafting;

import javax.annotation.Nonnull;

import net.minecraft.item.crafting.ShapelessRecipes;

import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class ShapelessRecipesHandler implements IRecipeHandler {

	@Nonnull
	@Override
	public Class getRecipeClass() {
		return ShapelessRecipes.class;
	}

	@Nonnull
	@Override
	public Class<? extends IRecipeCategory> getRecipeCategoryClass() {
		return CraftingRecipeCategory.class;
	}

	@Nonnull
	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull Object recipe) {
		return new ShapelessRecipesWrapper(recipe);
	}

}
