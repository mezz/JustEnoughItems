package mezz.jei.plugins.vanilla.crafting;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;

import javax.annotation.Nonnull;

public class ShapelessRecipesHandler implements IRecipeHandler<ShapelessRecipes> {
	@Nonnull
	private final IGuiHelper guiHelper;

	public ShapelessRecipesHandler(@Nonnull IGuiHelper guiHelper) {
		this.guiHelper = guiHelper;
	}

	@Override
	@Nonnull
	public Class<ShapelessRecipes> getRecipeClass() {
		return ShapelessRecipes.class;
	}

	@Nonnull
	@Override
	public String getRecipeCategoryUid() {
		return VanillaRecipeCategoryUid.CRAFTING;
	}

	@Override
	@Nonnull
	public IRecipeWrapper getRecipeWrapper(@Nonnull ShapelessRecipes recipe) {
		return new ShapelessRecipesWrapper(guiHelper, recipe);
	}

	@Override
	public boolean isRecipeValid(@Nonnull ShapelessRecipes recipe) {
		if (recipe.getRecipeOutput() == null) {
			return false;
		}
		int inputCount = 0;
		for (Object input : recipe.recipeItems) {
			if (input instanceof ItemStack) {
				inputCount++;
			} else {
				return false;
			}
		}
		return inputCount > 0;
	}
}
