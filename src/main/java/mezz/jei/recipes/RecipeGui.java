package mezz.jei.recipes;

import mezz.jei.api.gui.IGuiItemStack;
import mezz.jei.api.recipes.IRecipeGui;
import mezz.jei.gui.resource.IDrawable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public abstract class RecipeGui extends Gui implements IRecipeGui {

	private final IDrawable background;

	private int posX;
	private int posY;
	private boolean hasRecipe;

	private final ArrayList<IGuiItemStack> items = new ArrayList<IGuiItemStack>();

	public RecipeGui(IDrawable background) {
		this.background = background;
	}

	@Override
	public void setPosition(int posX, int posY) {
		this.posX = posX;
		this.posY = posY;
	}

	@Override
	public boolean hasRecipe() {
		return hasRecipe;
	}

	@Override
	public void setRecipe(Object recipe, ItemStack itemStack) {
		clearItems();
		if (recipe != null) {
			setItemsFromRecipe(recipe, itemStack);
			hasRecipe = true;
		} else {
			hasRecipe = false;
		}
	}

	abstract protected void setItemsFromRecipe(Object recipe, ItemStack focusStack);

	protected void addItem(IGuiItemStack guiItemStack) {
		items.add(guiItemStack);
	}

	protected void setItem(int index, Object item, ItemStack focusStack) {
		items.get(index).setItemStacks(item, focusStack);
	}

	protected void clearItems() {
		for (IGuiItemStack guiItemStack : items) {
			guiItemStack.clearItemStacks();
		}
	}

	@Override
	public ItemStack getStackUnderMouse(int mouseX, int mouseY) {
		mouseX -= posX;
		mouseY -= posY;

		for (IGuiItemStack item : items) {
			if (item.isMouseOver(mouseX, mouseY))
				return item.getItemStack();
		}
		return null;
	}

	@Override
	public final void draw(Minecraft minecraft, int mouseX, int mouseY) {
		GL11.glPushMatrix();
		GL11.glTranslatef(posX, posY, 0.0F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		mouseX -= posX;
		mouseY -= posY;

		drawBackground(minecraft, mouseX, mouseY);
		drawForeground(minecraft, mouseX, mouseY);

		GL11.glPopMatrix();
	}

	public void drawBackground(Minecraft minecraft, int mouseX, int mouseY) {
		background.draw(minecraft, 0, 0);
	}

	public void drawForeground(Minecraft minecraft, int mouseX, int mouseY) {
		IGuiItemStack hovered = null;
		for (IGuiItemStack item : items) {
			if (hovered == null && item.isMouseOver(mouseX, mouseY))
				hovered = item;
			else
				item.draw(minecraft);
		}
		if (hovered != null)
			hovered.drawHovered(minecraft, mouseX, mouseY);

	}

}
