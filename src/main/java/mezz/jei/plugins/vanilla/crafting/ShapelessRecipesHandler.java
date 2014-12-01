package mezz.jei.plugins.vanilla.crafting;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeType;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.crafting.ShapelessRecipes;

import javax.annotation.Nonnull;

public class ShapelessRecipesHandler implements IRecipeHandler {

	@Nonnull
	@Override
	public Class getRecipeClass() {
		return ShapelessRecipes.class;
	}

	@Nonnull
	@Override
	public Class<? extends IRecipeType> getRecipeTypeClass() {
		return CraftingRecipeType.class;
	}

	@Nonnull
	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull Object recipe) {
		return new ShapelessRecipesWrapper(recipe);
	}

}
