package mezz.jei.gui;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;

public class RecipeGui {

	@Nonnull
	private final IRecipeCategory recipeCategory;
	private final GuiItemStacks guiItemStacks;

	private IRecipeWrapper recipeWrapper;
	private int posX;
	private int posY;

	public RecipeGui(@Nonnull IRecipeCategory recipeCategory) {
		this.recipeCategory = recipeCategory;
		this.guiItemStacks = new GuiItemStacks();
		this.recipeCategory.init(guiItemStacks);
	}

	public void setPosition(int posX, int posY) {
		this.posX = posX;
		this.posY = posY;
	}

	public void setRecipe(@Nonnull IRecipeWrapper recipeWrapper, @Nullable ItemStack focusStack) {
		this.recipeWrapper = recipeWrapper;

		guiItemStacks.clearItemStacks();
		guiItemStacks.setFocusStack(focusStack);
		recipeCategory.setRecipe(guiItemStacks, recipeWrapper);
	}

	public void draw(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		if (recipeWrapper == null)
			return;

		GL11.glPushMatrix();
		GL11.glTranslatef(posX, posY, 0.0F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		recipeCategory.getBackground().draw(minecraft);
		recipeWrapper.drawInfo(minecraft);
		guiItemStacks.draw(minecraft, mouseX - posX, mouseY - posY);

		GL11.glPopMatrix();
	}

	public ItemStack getStackUnderMouse(int mouseX, int mouseY) {
		return guiItemStacks.getStackUnderMouse(mouseX - posX, mouseY - posY);
	}
}
