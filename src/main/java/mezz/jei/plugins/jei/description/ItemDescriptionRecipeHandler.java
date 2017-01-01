package mezz.jei.plugins.jei.description;

import java.util.List;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.Log;

public class ItemDescriptionRecipeHandler implements IRecipeHandler<ItemDescriptionRecipe> {
	@Override
	public Class<ItemDescriptionRecipe> getRecipeClass() {
		return ItemDescriptionRecipe.class;
	}

	@Override
	public String getRecipeCategoryUid() {
		return VanillaRecipeCategoryUid.DESCRIPTION;
	}

	@Override
	public String getRecipeCategoryUid(ItemDescriptionRecipe recipe) {
		return VanillaRecipeCategoryUid.DESCRIPTION;
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(ItemDescriptionRecipe recipe) {
		return recipe;
	}

	@Override
	public boolean isRecipeValid(ItemDescriptionRecipe recipe) {
		List<String> description = recipe.getDescription();
		if (description.isEmpty()) {
			String recipeInfo = ErrorUtil.getInfoFromRecipe(recipe, this);
			Log.error("Recipe has no description text. {}", recipeInfo);
		}
		return true;
	}
}
