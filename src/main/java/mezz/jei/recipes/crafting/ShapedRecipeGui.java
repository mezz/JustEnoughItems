package mezz.jei.recipes.crafting;

import mezz.jei.api.gui.IGuiItemStacks;
import mezz.jei.api.recipes.IRecipeWrapper;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ShapedRecipeGui extends CraftingRecipeGui {

	@Override
	public void setGuiItemStacks(@Nonnull IGuiItemStacks guiItemStacks, @Nonnull IRecipeWrapper recipeWrapper, @Nullable ItemStack focusStack) {

		IShapedCraftingRecipeWrapper wrapper = (IShapedCraftingRecipeWrapper)recipeWrapper;

		setInput(guiItemStacks, wrapper.getInputs(), focusStack, wrapper.getWidth(), wrapper.getHeight());
		setOutput(guiItemStacks, wrapper.getOutputs(), focusStack);
	}
}
