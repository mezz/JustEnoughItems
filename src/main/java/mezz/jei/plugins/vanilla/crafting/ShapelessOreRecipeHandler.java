package mezz.jei.plugins.vanilla.crafting;

import javax.annotation.Nonnull;
import java.util.List;

import net.minecraftforge.oredict.ShapelessOreRecipe;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

public class ShapelessOreRecipeHandler implements IRecipeHandler<ShapelessOreRecipe> {

	@Override
	@Nonnull
	public Class<ShapelessOreRecipe> getRecipeClass() {
		return ShapelessOreRecipe.class;
	}

	@Nonnull
	@Override
	public String getRecipeCategoryUid() {
		return VanillaRecipeCategoryUid.CRAFTING;
	}

	@Override
	@Nonnull
	public IRecipeWrapper getRecipeWrapper(@Nonnull ShapelessOreRecipe recipe) {
		return new ShapelessOreRecipeWrapper(recipe);
	}

	@Override
	public boolean isRecipeValid(@Nonnull ShapelessOreRecipe recipe) {
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
