package mezz.jei.recipes.crafting;

import mezz.jei.api.recipes.IRecipeGuiHelper;
import mezz.jei.api.recipes.IRecipeHelper;
import mezz.jei.api.recipes.IRecipeType;
import mezz.jei.api.recipes.IRecipeWrapper;
import mezz.jei.api.recipes.RecipeType;
import net.minecraft.item.crafting.ShapelessRecipes;

import javax.annotation.Nonnull;

public class ShapelessRecipesHelper implements IRecipeHelper {

	private ShapelessRecipes recipe;

	@Nonnull
	@Override
	public IRecipeType getRecipeType() {
		return RecipeType.CRAFTING_TABLE;
	}

	@Nonnull
	@Override
	public Class getRecipeClass() {
		return ShapelessRecipes.class;
	}

	@Nonnull
	@Override
	public IRecipeGuiHelper createGuiHelper() {
		return new ShapelessRecipeGui();
	}

	@Nonnull
	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull Object recipe) {
		return new ShapelessRecipesWrapper(recipe);
	}

}
