package mezz.jei.recipes.crafting;

import mezz.jei.api.gui.IGuiItemStacks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ShapedRecipesGui extends CraftingRecipeGui {

	@Override
	public void setGuiItemStacks(@Nonnull IGuiItemStacks guiItemStacks, @Nonnull Object recipe, @Nullable ItemStack focusStack) {
		ShapedRecipes shapedRecipe = (ShapedRecipes)recipe;

		setInput(guiItemStacks, shapedRecipe.recipeItems, focusStack, shapedRecipe.recipeWidth, shapedRecipe.recipeHeight);
		setOutput(guiItemStacks, shapedRecipe.getRecipeOutput(), focusStack);
	}
}
