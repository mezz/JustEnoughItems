package mezz.jei.api.recipes;

import mezz.jei.api.gui.IGuiItemStacks;
import mezz.jei.gui.resource.IDrawable;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An IRecipeGuiHelper helps draw a representation of a single Recipe.
 */
public interface IRecipeGuiHelper {

	/** Use guiItemStacks.initItemStack to set up the recipe's item stacks */
	void initGuiItemStacks(@Nonnull IGuiItemStacks guiItemStacks);

	/** Set the itemStacks based on the recipe and focusStack */
	void setGuiItemStacks(@Nonnull IGuiItemStacks guiItemStacks, @Nonnull IRecipeWrapper recipeWrapper, @Nullable ItemStack focusStack);

	@Nonnull
	IDrawable getBackground();

	/* Draw additional info. */
	void draw(@Nonnull Minecraft minecraft, int mouseX, int mouseY);

}
