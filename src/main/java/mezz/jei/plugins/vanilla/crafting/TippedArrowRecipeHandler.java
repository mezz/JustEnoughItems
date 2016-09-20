package mezz.jei.plugins.vanilla.crafting;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

public class TippedArrowRecipeHandler implements IRecipeHandler<TippedArrowRecipeWrapper> {
	@Override
	public Class<TippedArrowRecipeWrapper> getRecipeClass() {
		return TippedArrowRecipeWrapper.class;
	}

	@Override
	public String getRecipeCategoryUid() {
		return VanillaRecipeCategoryUid.CRAFTING;
	}

	@Override
	public String getRecipeCategoryUid(TippedArrowRecipeWrapper recipe) {
		return VanillaRecipeCategoryUid.CRAFTING;
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(TippedArrowRecipeWrapper recipe) {
		return recipe;
	}

	@Override
	public boolean isRecipeValid(TippedArrowRecipeWrapper recipe) {
		return true;
	}
}
