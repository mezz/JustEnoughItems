package mezz.jei.gui;

import mezz.jei.api.recipe.IRecipeGui;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PositionedRecipeGui implements IRecipeGui {

	@Nonnull
	private final IRecipeGui wrapped;
	private int posX;
	private int posY;

	public PositionedRecipeGui(@Nonnull IRecipeGui wrapped) {
		this.wrapped = wrapped;
	}

	public void setPosition(int posX, int posY) {
		this.posX = posX;
		this.posY = posY;
	}

	@Override
	public void setRecipe(@Nonnull IRecipeWrapper recipeWrapper, @Nullable ItemStack focusStack) {
		wrapped.setRecipe(recipeWrapper, focusStack);
	}

	@Override
	public void draw(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		GL11.glPushMatrix();
		GL11.glTranslatef(posX, posY, 0.0F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		wrapped.draw(minecraft, mouseX - posX, mouseY - posY);

		GL11.glPopMatrix();
	}

	@Override
	public ItemStack getStackUnderMouse(int mouseX, int mouseY) {
		return wrapped.getStackUnderMouse(mouseX - posX, mouseY - posY);
	}
}
