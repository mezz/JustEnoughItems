package mezz.jei.recipes.crafting;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;

import javax.annotation.Nonnull;
import java.util.List;

public class ShapelessRecipesGui extends CraftingRecipeGui {

	@Override
	protected void setItemsFromRecipe(@Nonnull Object recipe, ItemStack focusStack) {
		ShapelessRecipes shapelessRecipe = (ShapelessRecipes)recipe;

		List<ItemStack> recipeItems = ShapelessRecipesHelper.getRecipeItems(shapelessRecipe);
		if (recipeItems == null)
			return;

		for (int i = 0; i < recipeItems.size(); i++) {
			setInput(i, recipeItems.get(i));
		}

		setOutput(shapelessRecipe.getRecipeOutput());
	}

}
