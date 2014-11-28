package mezz.jei.plugins.vanilla.crafting;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.type.EnumRecipeTypeKey;
import mezz.jei.api.recipe.type.IRecipeTypeKey;
import net.minecraftforge.oredict.ShapedOreRecipe;

import javax.annotation.Nonnull;

public class ShapedOreRecipeHandler implements IRecipeHandler {

	@Nonnull
	@Override
	public IRecipeTypeKey getRecipeTypeKey() {
		return EnumRecipeTypeKey.CRAFTING_TABLE;
	}

	@Nonnull
	@Override
	public Class getRecipeClass() {
		return ShapedOreRecipe.class;
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull Object recipe) {
		return new ShapedOreRecipeWrapper(recipe);
	}

}
