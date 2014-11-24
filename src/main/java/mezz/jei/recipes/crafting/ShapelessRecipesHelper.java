package mezz.jei.recipe.crafting;

import mezz.jei.api.recipe.type.EnumRecipeType;
import mezz.jei.api.recipe.IRecipeGuiHelper;
import mezz.jei.api.recipe.IRecipeHelper;
import mezz.jei.api.recipe.type.IRecipeTypeKey;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.crafting.ShapelessRecipes;

import javax.annotation.Nonnull;

public class ShapelessRecipesHelper implements IRecipeHelper {

	private ShapelessRecipes recipe;

	@Nonnull
	@Override
	public IRecipeTypeKey getRecipeTypeKey() {
		return EnumRecipeType.CRAFTING_TABLE;
	}

	@Nonnull
	@Override
	public Class getRecipeClass() {
		return ShapelessRecipes.class;
	}

	@Nonnull
	@Override
	public IRecipeGuiHelper createGuiHelper() {
		return new ShapelessRecipeGui();
	}

	@Nonnull
	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull Object recipe) {
		return new ShapelessRecipesWrapper(recipe);
	}

}
