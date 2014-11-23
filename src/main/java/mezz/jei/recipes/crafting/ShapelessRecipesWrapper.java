package mezz.jei.recipes.crafting;

import mezz.jei.api.recipes.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class ShapelessRecipesWrapper implements IRecipeWrapper {

	@Nonnull
	private final ShapelessRecipes recipe;

	public ShapelessRecipesWrapper(@Nonnull Object recipe) {
		this.recipe = (ShapelessRecipes)recipe;
	}

	@Nonnull
	@Override
	public List getInputs() {
		return recipe.recipeItems;
	}

	@Nonnull
	@Override
	public List<ItemStack> getOutputs() {
		return Collections.singletonList(recipe.getRecipeOutput());
	}
}
