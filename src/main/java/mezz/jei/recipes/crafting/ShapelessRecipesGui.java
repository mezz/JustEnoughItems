package mezz.jei.recipes.crafting;

import mezz.jei.api.gui.IGuiItemStacks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;

import javax.annotation.Nonnull;
import java.util.List;

public class ShapelessRecipesGui extends CraftingRecipeGui {

	@Override
	protected void setItemsFromRecipe(@Nonnull IGuiItemStacks guiItemStacks, @Nonnull Object recipe, ItemStack focusStack) {
		ShapelessRecipes shapelessRecipe = (ShapelessRecipes)recipe;

		List<ItemStack> input = ShapelessRecipesHelper.getRecipeItems(shapelessRecipe);
		if (input == null)
			return;

		for (int i = 0; i < input.size(); i++) {
			setInput(guiItemStacks, i, input.get(i));
		}

		setOutput(guiItemStacks, shapelessRecipe.getRecipeOutput());
	}

}
