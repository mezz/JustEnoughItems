package mezz.jei.recipes.crafting;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;

import java.util.List;

public class ShapelessRecipesGui extends CraftingRecipeGui {

	@Override
	protected void setItemsFromRecipe(Object recipe, ItemStack focusStack) {
		ShapelessRecipes shapelessRecipe = (ShapelessRecipes)recipe;

		List recipeItems = shapelessRecipe.recipeItems;
		for (int i = 0; i < recipeItems.size(); i++) {
			setInput(i, recipeItems.get(i), focusStack);
		}

		setOutput(shapelessRecipe.getRecipeOutput());
	}
}
