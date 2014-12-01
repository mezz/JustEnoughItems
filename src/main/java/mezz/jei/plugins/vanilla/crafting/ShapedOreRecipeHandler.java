package mezz.jei.plugins.vanilla.crafting;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeType;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraftforge.oredict.ShapedOreRecipe;

import javax.annotation.Nonnull;

public class ShapedOreRecipeHandler implements IRecipeHandler {

	@Nonnull
	@Override
	public Class getRecipeClass() {
		return ShapedOreRecipe.class;
	}

	@Nonnull
	@Override
	public Class<? extends IRecipeType> getRecipeTypeClass() {
		return CraftingRecipeType.class;
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull Object recipe) {
		return new ShapedOreRecipeWrapper(recipe);
	}

}
