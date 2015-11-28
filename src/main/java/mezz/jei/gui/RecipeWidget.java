package mezz.jei.gui;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;

import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.gui.ingredients.GuiFluidStackGroup;
import mezz.jei.gui.ingredients.GuiItemStackGroup;

public class RecipeWidget {

	@Nonnull
	private final IRecipeCategory recipeCategory;
	private final GuiItemStackGroup guiItemStackGroup;
	private final GuiFluidStackGroup guiFluidStackGroup;

	private IRecipeWrapper recipeWrapper;
	private int posX;
	private int posY;

	public RecipeWidget(@Nonnull IRecipeCategory recipeCategory) {
		this.recipeCategory = recipeCategory;
		this.guiItemStackGroup = new GuiItemStackGroup();
		this.guiFluidStackGroup = new GuiFluidStackGroup();
		this.recipeCategory.init(guiItemStackGroup, guiFluidStackGroup);
	}

	public void setPosition(int posX, int posY) {
		this.posX = posX;
		this.posY = posY;
	}

	public void setRecipe(@Nonnull IRecipeWrapper recipeWrapper, @Nonnull Focus focus) {
		this.recipeWrapper = recipeWrapper;

		guiItemStackGroup.clear();
		guiItemStackGroup.setFocus(focus);
		recipeCategory.setRecipe(guiItemStackGroup, guiFluidStackGroup, recipeWrapper);
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
		guiItemStackGroup.draw(minecraft, mouseX - posX, mouseY - posY);

		GL11.glPopMatrix();
	}

	public Focus getFocusUnderMouse(int mouseX, int mouseY) {
		return guiItemStackGroup.getFocusUnderMouse(mouseX - posX, mouseY - posY);
	}
}
