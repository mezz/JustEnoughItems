package mezz.jei.recipes;

import mezz.jei.api.gui.IGuiItemStack;
import mezz.jei.api.recipes.IRecipeGui;
import mezz.jei.gui.resource.IDrawable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;

public abstract class RecipeGui extends Gui implements IRecipeGui {

	@Nonnull
	private final IDrawable background;

	private int posX;
	private int posY;
	private boolean hasRecipe;

	@Nonnull
	private final ArrayList<IGuiItemStack> items = new ArrayList<IGuiItemStack>();

	protected RecipeGui(@Nonnull IDrawable background) {
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
	public void setRecipe(Object recipe, ItemStack focusStack) {
		clearItems();
		if (recipe != null) {
			setItemsFromRecipe(recipe, focusStack);
			hasRecipe = true;
		} else {
			hasRecipe = false;
		}
	}

	abstract protected void setItemsFromRecipe(@Nonnull Object recipe, @Nullable ItemStack focusStack);

	protected void addItem(@Nonnull IGuiItemStack guiItemStack) {
		items.add(guiItemStack);
	}

	protected void setItems(int index, @Nonnull Iterable<ItemStack> itemStacks, @Nullable ItemStack focusStack) {
		this.items.get(index).setItemStacks(itemStacks, focusStack);
	}

	protected void setItem(int index, @Nonnull ItemStack item) {
		items.get(index).setItemStack(item);
	}

	protected void clearItems() {
		for (IGuiItemStack guiItemStack : items) {
			guiItemStack.clearItemStacks();
		}
	}

	@Nullable
	@Override
	public ItemStack getStackUnderMouse(int mouseX, int mouseY) {
		mouseX -= posX;
		mouseY -= posY;

		for (IGuiItemStack item : items) {
			if (item != null && item.isMouseOver(mouseX, mouseY))
				return item.getItemStack();
		}
		return null;
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
