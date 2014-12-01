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
import java.util.List;

public class CentrifugeRecipeGui implements IRecipeGui {

	private static final int inputSlot = 0;
	private static final int outputSlot1 = 1;

	@Nonnull
	private final IDrawable background;
	@Nonnull
	private final IGuiItemStacks guiItemStacks;
	@Nullable
	private CentrifugeRecipeWrapper recipeWrapper;

	public CentrifugeRecipeGui(@Nonnull IRecipeType recipeType) {
		background = recipeType.getBackground();

		guiItemStacks = JEIManager.guiHelper.makeGuiItemStacks();

		// Resource
		guiItemStacks.initItemStack(inputSlot, 4, 18);

		// Product Inventory
		for (int y = 0; y < 3; y++)
			for (int x = 0; x < 3; x++)
				guiItemStacks.initItemStack(outputSlot1 + x + (y * 3), 68 + x * 18, y * 18);
	}

	@Override
	public void setRecipe(@Nonnull IRecipeWrapper recipeWrapper, @Nullable ItemStack focusStack) {
		this.recipeWrapper = (CentrifugeRecipeWrapper)recipeWrapper;

		guiItemStacks.setItemStack(inputSlot, this.recipeWrapper.getInputs(), focusStack);

		List<ItemStack> outputs = this.recipeWrapper.getOutputs();
		for (int i = 0; i < outputs.size(); i++) {
			guiItemStacks.setItemStack(outputSlot1 + i, outputs.get(i), focusStack);
		}
	}

	@Override
	public void draw(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		if (recipeWrapper == null)
			return;

		background.draw(minecraft);
		recipeWrapper.drawInfo(minecraft);
		guiItemStacks.draw(minecraft, mouseX, mouseY);
	}

	@Override
	public ItemStack getStackUnderMouse(int mouseX, int mouseY) {
		return guiItemStacks.getStackUnderMouse(mouseX, mouseY);
	}
}
