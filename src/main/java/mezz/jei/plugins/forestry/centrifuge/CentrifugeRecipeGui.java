package mezz.jei.plugins.forestry.centrifuge;

import mezz.jei.api.JEIManager;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStacks;
import mezz.jei.api.recipe.IRecipeGui;
import mezz.jei.api.recipe.IRecipeType;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CentrifugeRecipeGui implements IRecipeGui {

	private static final int inputSlot = 0;
	private static final int outputSlot1 = 1;

	@Nonnull
	private final IDrawable background;
	@Nonnull
	private final IGuiItemStacks guiItemStacks;
	@Nullable
	private IRecipeWrapper recipeWrapper;

	public CentrifugeRecipeGui(@Nonnull IRecipeType recipeType) {
		background = recipeType.getBackground();

		guiItemStacks = JEIManager.guiHelper.makeGuiItemStacks();

		// Resource
		guiItemStacks.initItemStack(inputSlot, 34, 37);

		// Product Inventory
		for (int y = 0; y < 3; y++)
			for (int x = 0; x < 3; x++)
				guiItemStacks.initItemStack(outputSlot1 + x + (y * 3), 98 + x * 18, 19 + y * 18);
	}


	@Override
	public void setRecipe(@Nonnull IRecipeWrapper recipeWrapper, @Nullable ItemStack focusStack) {

	}

	@Override
	public void draw(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		if (recipeWrapper == null)
			return;

		background.draw(minecraft);
		recipeWrapper.drawInfo(minecraft, mouseX, mouseY);
		guiItemStacks.draw(minecraft, mouseX, mouseY);
	}

	@Override
	public ItemStack getStackUnderMouse(int mouseX, int mouseY) {
		return guiItemStacks.getStackUnderMouse(mouseX, mouseY);
	}
}
