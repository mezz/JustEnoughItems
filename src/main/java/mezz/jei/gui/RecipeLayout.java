package mezz.jei.gui;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;

import net.minecraftforge.fml.client.config.GuiButtonExt;

import org.lwjgl.opengl.GL11;

import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.gui.ingredients.GuiFluidStackGroup;
import mezz.jei.gui.ingredients.GuiItemStackGroup;

public class RecipeLayout implements IRecipeLayout {
	private static final int RECIPE_BUTTON_SIZE = 12;
	public static final int recipeTransferButtonIndex = 100;

	@Nonnull
	private final IRecipeCategory recipeCategory;
	@Nonnull
	private final GuiItemStackGroup guiItemStackGroup;
	@Nonnull
	private final GuiFluidStackGroup guiFluidStackGroup;
	@Nonnull
	private final GuiButtonExt recipeTransferButton;
	@Nonnull
	private final IRecipeWrapper recipeWrapper;

	private final int posX;
	private final int posY;

	public RecipeLayout(int index, int posX, int posY, @Nonnull IRecipeCategory recipeCategory, @Nonnull IRecipeWrapper recipeWrapper, @Nonnull Focus focus, @Nonnull Focus.Mode focusMode) {
		this.recipeCategory = recipeCategory;
		this.guiItemStackGroup = new GuiItemStackGroup();
		this.guiFluidStackGroup = new GuiFluidStackGroup();
		int width = recipeCategory.getBackground().getWidth();
		int height = recipeCategory.getBackground().getHeight();
		this.recipeTransferButton = new GuiButtonExt(recipeTransferButtonIndex + index, posX + width + 2, posY + height - RECIPE_BUTTON_SIZE, RECIPE_BUTTON_SIZE, RECIPE_BUTTON_SIZE, "+");
		this.posX = posX;
		this.posY = posY;

		this.recipeWrapper = recipeWrapper;
		this.guiItemStackGroup.setFocus(focus, focusMode);
		this.recipeCategory.setRecipe(this, recipeWrapper);
	}

	public void draw(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		GL11.glPushMatrix();
		GL11.glTranslatef(posX, posY, 0.0F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(GL11.GL_LIGHTING);

		IDrawable background = recipeCategory.getBackground();
		background.draw(minecraft);
		recipeCategory.drawExtras(minecraft);

		GL11.glTranslatef(-posX, -posY, 0.0F);
		recipeTransferButton.drawButton(minecraft, mouseX, mouseY);
		GL11.glTranslatef(posX, posY, 0.0F);

		recipeWrapper.drawInfo(minecraft, background.getWidth(), background.getHeight());
		guiItemStackGroup.draw(minecraft, mouseX - posX, mouseY - posY);

		GL11.glPopMatrix();
	}

	public Focus getFocusUnderMouse(int mouseX, int mouseY) {
		return guiItemStackGroup.getFocusUnderMouse(mouseX - posX, mouseY - posY);
	}

	@Override
	@Nonnull
	public GuiItemStackGroup getItemStacks() {
		return guiItemStackGroup;
	}

	@Override
	@Nonnull
	public IGuiFluidStackGroup getFluidStacks() {
		return guiFluidStackGroup;
	}

	@Override
	public void setRecipeTransferButton(int posX, int posY) {
		recipeTransferButton.xPosition = posX + this.posX;
		recipeTransferButton.yPosition = posY + this.posY;
	}

	@Nonnull
	public GuiButtonExt getRecipeTransferButton() {
		return recipeTransferButton;
	}

	@Nonnull
	public IRecipeWrapper getRecipeWrapper() {
		return recipeWrapper;
	}

	@Nonnull
	public IRecipeCategory getRecipeCategory() {
		return recipeCategory;
	}
}
