package mezz.jei.recipes;

import mezz.jei.api.JEIManager;
import mezz.jei.api.gui.IGuiItemStacks;
import mezz.jei.api.recipes.IRecipeGuiHelper;
import mezz.jei.api.recipes.IRecipeWrapper;
import mezz.jei.gui.resource.IDrawable;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class RecipeGui {

	@Nonnull
	private final IDrawable background;
	@Nonnull
	private final IRecipeGuiHelper guiHelper;
	@Nonnull
	private final IGuiItemStacks guiItemStacks;

	private int posX;
	private int posY;

	public RecipeGui(@Nonnull IRecipeGuiHelper guiHelper) {
		this.background = guiHelper.getBackground();
		this.guiHelper = guiHelper;
		this.guiItemStacks = JEIManager.guiHelper.makeGuiItemStacks();
		this.guiHelper.initGuiItemStacks(this.guiItemStacks);
	}

	public void setPosition(int posX, int posY) {
		this.posX = posX;
		this.posY = posY;
	}

	public void setRecipe(@Nonnull IRecipeWrapper recipeWrapper, @Nullable ItemStack focusStack) {
		guiItemStacks.clearItemStacks();
		guiHelper.setGuiItemStacks(guiItemStacks, recipeWrapper, focusStack);
	}

	protected void addItem(int index, int xPosition, int yPosition) {
		guiItemStacks.initItemStack(index, xPosition, yPosition);
	}

	@Nullable
	public ItemStack getStackUnderMouse(int mouseX, int mouseY) {
		return guiItemStacks.getStackUnderMouse(mouseX - posX, mouseY - posY);
	}

	public final void draw(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		GL11.glPushMatrix();
		GL11.glTranslatef(posX, posY, 0.0F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		mouseX -= posX;
		mouseY -= posY;

		drawBackground(minecraft, mouseX, mouseY);
		guiHelper.draw(minecraft, mouseX, mouseY);
		drawForeground(minecraft, mouseX, mouseY);

		GL11.glPopMatrix();
	}

	protected void drawBackground(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		background.draw(minecraft, 0, 0);
	}

	protected void drawForeground(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		guiItemStacks.draw(minecraft, mouseX, mouseY);
	}

}
