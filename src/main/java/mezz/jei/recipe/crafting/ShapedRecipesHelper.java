package mezz.jei.recipe.crafting;

import mezz.jei.api.recipe.IRecipeHelper;
import mezz.jei.api.recipe.type.EnumRecipeTypeKey;
import mezz.jei.api.recipe.type.IRecipeTypeKey;
import mezz.jei.api.recipe.wrapper.IRecipeWrapper;
import net.minecraft.item.crafting.ShapedRecipes;

import javax.annotation.Nonnull;

public class ShapedRecipesHelper implements IRecipeHelper {

	@Nonnull
	@Override
	public IRecipeTypeKey getRecipeTypeKey() {
		return EnumRecipeTypeKey.CRAFTING_TABLE;
	}

	@Nonnull
	@Override
	public Class getRecipeClass() {
		return ShapedRecipes.class;
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull Object recipe) {
		return new ShapedRecipesWrapper(recipe);
	}

}
