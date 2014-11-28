package mezz.jei.plugins.vanilla.crafting;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.type.EnumRecipeTypeKey;
import mezz.jei.api.recipe.type.IRecipeTypeKey;
import net.minecraft.item.crafting.ShapedRecipes;

import javax.annotation.Nonnull;

public class ShapedRecipesHandler implements IRecipeHandler {

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
