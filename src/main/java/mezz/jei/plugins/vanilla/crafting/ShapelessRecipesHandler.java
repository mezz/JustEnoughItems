package mezz.jei.plugins.vanilla.crafting;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.type.EnumRecipeTypeKey;
import mezz.jei.api.recipe.type.IRecipeTypeKey;
import net.minecraft.item.crafting.ShapelessRecipes;

import javax.annotation.Nonnull;

public class ShapelessRecipesHandler implements IRecipeHandler {

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
