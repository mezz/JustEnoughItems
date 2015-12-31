package mezz.jei.plugins.jei.debug;

import javax.annotation.Nonnull;
import java.util.List;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

public class DebugRecipeHandler implements IRecipeHandler<DebugRecipe> {
	@Nonnull
	@Override
	public Class<DebugRecipe> getRecipeClass() {
		return DebugRecipe.class;
	}

	@Nonnull
	@Override
	public String getRecipeCategoryUid() {
		return "debug";
	}

	@Nonnull
	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull DebugRecipe recipe) {
		return recipe;
	}

	@Override
	public boolean isRecipeValid(@Nonnull DebugRecipe recipe) {
		return true;
	}
}
