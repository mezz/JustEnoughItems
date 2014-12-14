package mezz.jei.plugins.vanilla.crafting;

import javax.annotation.Nonnull;

import net.minecraftforge.oredict.ShapedOreRecipe;

import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class ShapedOreRecipeHandler implements IRecipeHandler {

	@Nonnull
	@Override
	public Class getRecipeClass() {
		return ShapedOreRecipe.class;
	}

	@Nonnull
	@Override
	public Class<? extends IRecipeCategory> getRecipeCategoryClass() {
		return CraftingRecipeCategory.class;
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull Object recipe) {
		return new ShapedOreRecipeWrapper(recipe);
	}

}
