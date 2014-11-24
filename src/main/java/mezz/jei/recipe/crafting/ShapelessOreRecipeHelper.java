package mezz.jei.recipe.crafting;

import mezz.jei.api.recipe.IRecipeHelper;
import mezz.jei.api.recipe.type.EnumRecipeTypeKey;
import mezz.jei.api.recipe.type.IRecipeTypeKey;
import mezz.jei.api.recipe.wrapper.IRecipeWrapper;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import javax.annotation.Nonnull;

public class ShapelessOreRecipeHelper implements IRecipeHelper {

	@Nonnull
	@Override
	public IRecipeTypeKey getRecipeTypeKey() {
		return EnumRecipeTypeKey.CRAFTING_TABLE;
	}

	@Nonnull
	@Override
	public Class getRecipeClass() {
		return ShapelessOreRecipe.class;
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull Object recipe) {
		return new ShapelessOreRecipeWrapper(recipe);
	}

}
