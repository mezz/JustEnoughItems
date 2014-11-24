package mezz.jei.recipe.furnace;

import mezz.jei.api.JEIManager;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStacks;
import mezz.jei.api.recipe.IRecipeGui;
import mezz.jei.api.recipe.wrapper.IFuelRecipeWrapper;
import mezz.jei.api.recipe.wrapper.IRecipeWrapper;
import mezz.jei.api.recipe.type.IRecipeType;
import mezz.jei.api.recipe.wrapper.ISmeltingRecipeWrapper;
import mezz.jei.util.StackUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class FurnaceRecipeGui implements IRecipeGui {

	protected static final int inputSlot = 0;
	protected static final int fuelSlot = 1;
	protected static final int outputSlot = 2;

	@Nonnull
	private final IDrawable background;
	@Nonnull
	private final IGuiItemStacks guiItemStacks;
	@Nullable
	private IRecipeWrapper recipeWrapper;

	private int posX;
	private int posY;

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
	public void setPosition(int posX, int posY) {
		this.posX = posX;
		this.posY = posY;
	}

	@Override
	public void draw(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		if (recipeWrapper == null)
			return;

		GL11.glPushMatrix();
		GL11.glTranslatef(posX, posY, 0.0F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		mouseX -= posX;
		mouseY -= posY;

		background.draw(minecraft);
		recipeWrapper.drawInfo(minecraft, mouseX, mouseY);
		guiItemStacks.draw(minecraft, mouseX, mouseY);

		GL11.glPopMatrix();
	}

	@Nullable
	@Override
	public ItemStack getStackUnderMouse(int mouseX, int mouseY) {
		return guiItemStacks.getStackUnderMouse(mouseX, mouseY);
	}
}
