package mezz.jei.recipe.furnace.smelting;

import mezz.jei.api.recipe.type.EnumRecipeType;
import mezz.jei.api.recipe.IRecipeGuiHelper;
import mezz.jei.api.recipe.IRecipeHelper;
import mezz.jei.api.recipe.type.IRecipeTypeKey;
import mezz.jei.api.recipe.IRecipeWrapper;

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
		return EnumRecipeType.FURNACE;
	}

	@Nonnull
	@Override
	public IRecipeGuiHelper createGuiHelper() {
		return new SmeltingRecipeGui();
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull Object recipe) {
		return (SmeltingRecipe)recipe;
	}

}
