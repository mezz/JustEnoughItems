package mezz.jei.recipes.crafting;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.ArrayList;

public class ShapelessOreRecipeGui extends CraftingRecipeGui {

	@Override
	protected void setItemsFromRecipe(Object recipe, ItemStack focusStack) {
		ShapelessOreRecipe shapelessOreRecipe = (ShapelessOreRecipe)recipe;

		ArrayList<Object> recipeItems = shapelessOreRecipe.getInput();
		for (int i = 0; i < recipeItems.size(); i++) {
			setInput(i, recipeItems.get(i), focusStack);
		}

		setOutput(shapelessOreRecipe.getRecipeOutput());
	}

}
