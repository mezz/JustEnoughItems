package mezz.jei.recipes.furnace.smelting;

import mezz.jei.api.recipes.EnumRecipeType;
import mezz.jei.api.recipes.IRecipeGuiHelper;
import mezz.jei.api.recipes.IRecipeHelper;
import mezz.jei.api.recipes.IRecipeTypeKey;
import mezz.jei.api.recipes.IRecipeWrapper;

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
