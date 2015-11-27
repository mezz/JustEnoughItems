package mezz.jei.gui;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;

import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;

public class RecipeWidget {

	@Nonnull
	private final IRecipeCategory recipeCategory;
	private final GuiItemStacks guiItemStacks;
	private final GuiFluidTanks guiFluidTanks;

	private IRecipeWrapper recipeWrapper;
	private int posX;
	private int posY;

	public RecipeWidget(@Nonnull IRecipeCategory recipeCategory) {
		this.recipeCategory = recipeCategory;
		this.guiItemStacks = new GuiItemStacks();
		this.guiFluidTanks = new GuiFluidTanks();
		this.recipeCategory.init(guiItemStacks, guiFluidTanks);
	}

	public void setPosition(int posX, int posY) {
		this.posX = posX;
		this.posY = posY;
	}

	public void setRecipe(@Nonnull IRecipeWrapper recipeWrapper, @Nonnull Focus focus) {
		this.recipeWrapper = recipeWrapper;

		guiItemStacks.clear();
		guiItemStacks.setFocus(focus);
		recipeCategory.setRecipe(guiItemStacks, guiFluidTanks, recipeWrapper);
	}

	public void draw(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		if (recipeWrapper == null) {
			return;
		}

		GL11.glPushMatrix();
		GL11.glTranslatef(posX, posY, 0.0F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(GL11.GL_LIGHTING);

		recipeCategory.getBackground().draw(minecraft);
		recipeWrapper.drawInfo(minecraft);
		guiItemStacks.draw(minecraft, mouseX - posX, mouseY - posY);

		GL11.glPopMatrix();
	}

	public Focus getFocusUnderMouse(int mouseX, int mouseY) {
		return guiItemStacks.getFocusUnderMouse(mouseX - posX, mouseY - posY);
	}
}
