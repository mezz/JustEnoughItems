package mezz.jei.plugins.forestry.centrifuge;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeType;
import mezz.jei.api.recipe.IRecipeWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CentrifugeRecipeHandler implements IRecipeHandler {

	@Nullable
	@Override
	public Class getRecipeClass() {
		try {
			return Class.forName("forestry.factory.gadgets.MachineCentrifuge$Recipe");
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	@Nonnull
	@Override
	public Class<? extends IRecipeType> getRecipeTypeClass() {
		return CentrifugeRecipeType.class;
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull Object recipe) {
		return new CentrifugeRecipeWrapper(recipe);
	}

}
