package mezz.jei.plugins.vanilla.crafting;

import javax.annotation.Nonnull;
import java.util.List;

import net.minecraftforge.oredict.ShapedOreRecipe;

import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class ShapedOreRecipeHandler implements IRecipeHandler<ShapedOreRecipe> {

	@Nonnull
	@Override
	public Class<ShapedOreRecipe> getRecipeClass() {
		return ShapedOreRecipe.class;
	}

	@Nonnull
	@Override
	public Class<? extends IRecipeCategory> getRecipeCategoryClass() {
		return CraftingRecipeCategory.class;
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull ShapedOreRecipe recipe) {
		return new ShapedOreRecipeWrapper(recipe);
	}

	@Override
	public boolean isRecipeValid(@Nonnull ShapedOreRecipe recipe) {
		if (recipe.getRecipeOutput() == null) {
			return false;
		}
		int inputCount = 0;
		for (Object input : recipe.getInput()) {
			if (input instanceof List) {
				if (((List) input).size() == 0) {
					return false;
				}
			}
			if (input != null) {
				inputCount++;
			}
		}
		return inputCount > 0;
	}
}
