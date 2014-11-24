package mezz.jei.recipe.furnace;

import mezz.jei.api.JEIManager;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStacks;
import mezz.jei.api.recipe.IRecipeGui;
import mezz.jei.api.recipe.type.IRecipeType;
import mezz.jei.api.recipe.wrapper.IFuelRecipeWrapper;
import mezz.jei.api.recipe.wrapper.IRecipeWrapper;
import mezz.jei.api.recipe.wrapper.ISmeltingRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class FurnaceRecipeGui implements IRecipeGui {

	private static final int inputSlot = 0;
	private static final int fuelSlot = 1;
	private static final int outputSlot = 2;

	@Nonnull
	private final IDrawable background;
	@Nonnull
	private final IGuiItemStacks guiItemStacks;
	@Nullable
	private IRecipeWrapper recipeWrapper;

	public FurnaceRecipeGui(@Nonnull IRecipeType recipeType) {
		background = recipeType.getBackground();

		guiItemStacks = JEIManager.guiHelper.makeGuiItemStacks();

		guiItemStacks.initItemStack(inputSlot, 0, 0);
		guiItemStacks.initItemStack(fuelSlot, 0, 36);
		guiItemStacks.initItemStack(outputSlot, 60, 18);
	}

	@Override
	public void setRecipe(@Nonnull IRecipeWrapper recipeWrapper, @Nullable ItemStack focusStack) {
		this.recipeWrapper = recipeWrapper;
		guiItemStacks.clearItemStacks();

		if (recipeWrapper instanceof IFuelRecipeWrapper) {
			IFuelRecipeWrapper fuelRecipeWrapper = (IFuelRecipeWrapper)recipeWrapper;
			guiItemStacks.setItemStack(fuelSlot, fuelRecipeWrapper.getInputs(), focusStack);
		} else if (recipeWrapper instanceof ISmeltingRecipeWrapper) {
			ISmeltingRecipeWrapper smeltingRecipeWrapper = (ISmeltingRecipeWrapper)recipeWrapper;
			guiItemStacks.setItemStack(inputSlot, smeltingRecipeWrapper.getInputs(), focusStack);
			guiItemStacks.setItemStack(outputSlot, smeltingRecipeWrapper.getOutputs(), focusStack);
		}
	}

	@Override
	public void draw(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		if (recipeWrapper == null)
			return;

		background.draw(minecraft);
		recipeWrapper.drawInfo(minecraft, mouseX, mouseY);
		guiItemStacks.draw(minecraft, mouseX, mouseY);
	}

	@Nullable
	@Override
	public ItemStack getStackUnderMouse(int mouseX, int mouseY) {
		return guiItemStacks.getStackUnderMouse(mouseX, mouseY);
	}
}
