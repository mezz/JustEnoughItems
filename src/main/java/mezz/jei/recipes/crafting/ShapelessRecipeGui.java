package mezz.jei.recipe.crafting;

import mezz.jei.api.gui.IGuiItemStacks;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ShapelessRecipeGui extends CraftingRecipeGui {

	@Override
	public void setGuiItemStacks(@Nonnull IGuiItemStacks guiItemStacks, @Nonnull IRecipeWrapper recipeWrapper, @Nullable ItemStack focusStack) {
		setInput(guiItemStacks, recipeWrapper.getInputs(), focusStack);
		setOutput(guiItemStacks, recipeWrapper.getOutputs(), focusStack);
	}

}
