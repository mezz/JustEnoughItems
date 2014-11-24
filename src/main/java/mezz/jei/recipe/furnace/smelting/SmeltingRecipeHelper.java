package mezz.jei.recipe.furnace.smelting;

import mezz.jei.api.recipe.IRecipeHelper;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.type.EnumRecipeTypeKey;
import mezz.jei.api.recipe.type.IRecipeTypeKey;

import javax.annotation.Nonnull;

public class SmeltingRecipeHelper implements IRecipeHelper {

	@Nonnull
	@Override
	public Class getRecipeClass() {
		return SmeltingRecipe.class;
	}

	@Nonnull
	@Override
	public IRecipeTypeKey getRecipeTypeKey() {
		return EnumRecipeTypeKey.FURNACE;
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull Object recipe) {
		return (SmeltingRecipe)recipe;
	}

}
