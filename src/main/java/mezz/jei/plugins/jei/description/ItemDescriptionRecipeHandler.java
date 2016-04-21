package mezz.jei.plugins.jei.description;

import javax.annotation.Nonnull;
import java.util.List;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.Log;

public class ItemDescriptionRecipeHandler implements IRecipeHandler<ItemDescriptionRecipe> {
	@Nonnull
	@Override
	public Class<ItemDescriptionRecipe> getRecipeClass() {
		return ItemDescriptionRecipe.class;
	}

	@Nonnull
	@Override
	public String getRecipeCategoryUid() {
		return VanillaRecipeCategoryUid.DESCRIPTION;
	}

	@Nonnull
	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull ItemDescriptionRecipe recipe) {
		return recipe;
	}

	@Override
	public boolean isRecipeValid(@Nonnull ItemDescriptionRecipe recipe) {
		List<String> description = recipe.getDescription();
		if (description.isEmpty()) {
			String recipeInfo = ErrorUtil.getInfoFromBrokenRecipe(recipe, this);
			Log.error("Recipe has no description text. {}", recipeInfo);
		}
		return true;
	}
}
