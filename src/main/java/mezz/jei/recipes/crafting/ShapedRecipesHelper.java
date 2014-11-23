package mezz.jei.recipes.crafting;

import mezz.jei.api.recipes.IRecipeGuiHelper;
import mezz.jei.api.recipes.IRecipeHelper;
import mezz.jei.api.recipes.IRecipeType;
import mezz.jei.api.recipes.IRecipeWrapper;
import mezz.jei.api.recipes.RecipeType;
import net.minecraft.item.crafting.ShapedRecipes;

import javax.annotation.Nonnull;

public class ShapedRecipesHelper implements IRecipeHelper {

	@Nonnull
	@Override
	public IRecipeType getRecipeType() {
		return RecipeType.CRAFTING_TABLE;
	}

	@Nonnull
	@Override
	public Class getRecipeClass() {
		return ShapedRecipes.class;
	}

	@Nonnull
	@Override
	public IRecipeGuiHelper createGuiHelper() {
		return new ShapedRecipeGui();
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull Object recipe) {
		return new ShapedRecipesWrapper(recipe);
	}

}
