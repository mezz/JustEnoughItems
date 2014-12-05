package mezz.jei.plugins.forestry.fabricator;

import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FabricatorSmeltingRecipeHandler implements IRecipeHandler {

	@Nullable
	@Override
	public Class getRecipeClass() {
		try {
			return Class.forName("forestry.factory.gadgets.MachineFabricator$Smelting");
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	@Override
	public Class<? extends IRecipeCategory> getRecipeCategoryClass() {
		return FabricatorRecipeCategory.class;
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull Object recipe) {
		return new FabricatorSmeltingRecipeWrapper(recipe);
	}

}
