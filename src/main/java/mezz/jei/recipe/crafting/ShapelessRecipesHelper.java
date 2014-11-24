package mezz.jei.recipe.crafting;

import mezz.jei.api.recipe.IRecipeHelper;
import mezz.jei.api.recipe.type.EnumRecipeTypeKey;
import mezz.jei.api.recipe.type.IRecipeTypeKey;
import mezz.jei.api.recipe.wrapper.IRecipeWrapper;
import net.minecraft.item.crafting.ShapelessRecipes;

import javax.annotation.Nonnull;

public class ShapelessRecipesHelper implements IRecipeHelper {

	private ShapelessRecipes recipe;

	@Nonnull
	@Override
	public IRecipeTypeKey getRecipeTypeKey() {
		return EnumRecipeTypeKey.CRAFTING_TABLE;
	}

	@Nonnull
	@Override
	public Class getRecipeClass() {
		return ShapelessRecipes.class;
	}


	@Nonnull
	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull Object recipe) {
		return new ShapelessRecipesWrapper(recipe);
	}

}
