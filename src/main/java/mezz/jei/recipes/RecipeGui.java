package mezz.jei.recipes;

import mezz.jei.api.JEIManager;
import mezz.jei.api.gui.IGuiItemStacks;
import mezz.jei.api.recipes.IRecipeGui;
import mezz.jei.gui.resource.IDrawable;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class RecipeGui implements IRecipeGui {

	@Nonnull
	private final IDrawable background;
	@Nonnull
	private final IGuiItemStacks guiItemStacks;

	private int posX;
	private int posY;

	protected RecipeGui(@Nonnull IDrawable background) {
		this.background = background;
		this.guiItemStacks = JEIManager.guiHelper.makeGuiItemStacks();
	}

	@Override
	public void setPosition(int posX, int posY) {
		this.posX = posX;
		this.posY = posY;
	}

	@Override
	public void setRecipe(Object recipe, ItemStack focusStack) {
		guiItemStacks.clearItemStacks();
		if (recipe != null) {
			setItemsFromRecipe(guiItemStacks, recipe, focusStack);
		}
	}

	abstract protected void setItemsFromRecipe(@Nonnull IGuiItemStacks guiItemStacks, @Nonnull Object recipe, @Nullable ItemStack focusStack);

	protected void addItem(int index, int xPosition, int yPosition) {
		guiItemStacks.initItemStack(index, xPosition, yPosition);
	}

	@Nullable
	@Override
	public ItemStack getStackUnderMouse(int mouseX, int mouseY) {
		return guiItemStacks.getStackUnderMouse(mouseX - posX, mouseY - posY);
	}

	@Override
	public final void draw(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		GL11.glPushMatrix();
		GL11.glTranslatef(posX, posY, 0.0F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		mouseX -= posX;
		mouseY -= posY;

		drawBackground(minecraft, mouseX, mouseY);
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
