package mezz.jei.plugins.vanilla.crafting;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;

import mezz.jei.api.recipe.wrapper.ICraftingRecipeWrapper;
import mezz.jei.plugins.vanilla.VanillaRecipeWrapper;

public class ShapelessRecipesWrapper extends VanillaRecipeWrapper implements ICraftingRecipeWrapper {

	@Nonnull
	private final ShapelessRecipes recipe;

	public ShapelessRecipesWrapper(@Nonnull ShapelessRecipes recipe) {
		this.recipe = recipe;
		for (Object input : this.recipe.recipeItems) {
			if (input instanceof ItemStack) {
				ItemStack itemStack = (ItemStack) input;
				if (itemStack.stackSize != 1) {
					itemStack.stackSize = 1;
				}
			}
		}
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
