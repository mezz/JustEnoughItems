package mezz.jei.api.recipe;

import mezz.jei.api.recipe.wrapper.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IRecipeGui {
	/** Set the itemStacks based on the recipe and focusStack */
	void setRecipe(@Nonnull IRecipeWrapper recipeWrapper, @Nullable ItemStack focusStack);

	void setPosition(int posX, int posY);

	void draw(@Nonnull Minecraft minecraft, int mouseX, int mouseY);

	public ItemStack getStackUnderMouse(int mouseX, int mouseY);
}
