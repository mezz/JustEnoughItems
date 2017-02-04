package mezz.jei.gui;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.List;
import java.util.Set;

import mezz.jei.Internal;
import mezz.jei.JeiRuntime;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.config.Config;
import mezz.jei.gui.ingredients.GuiIngredientFast;
import mezz.jei.gui.ingredients.GuiIngredientFastList;
import mezz.jei.gui.ingredients.GuiItemStackGroup;
import mezz.jei.input.ClickedIngredient;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IShowsRecipeFocuses;
import mezz.jei.util.GuiAreaHelper;
import mezz.jei.util.StackHelper;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * An ingredient grid displays a rectangular area of clickable recipe ingredients.
 */
public abstract class IngredientGrid implements IShowsRecipeFocuses {
	private static final int itemStackPadding = 1;
	private static final int itemStackWidth = GuiItemStackGroup.getWidth(itemStackPadding);
	private static final int itemStackHeight = GuiItemStackGroup.getHeight(itemStackPadding);

	@Nullable
	private final List<Rectangle> guiAreas;
	private final Rectangle area;
	protected final GuiIngredientFastList guiIngredientList;

	@Nullable
	private GuiIngredientFast hovered;

	public IngredientGrid(IIngredientRegistry ingredientRegistry, Rectangle area, @Nullable List<Rectangle> guiAreas) {
		final int columns = area.width / itemStackWidth;
		final int rows = area.height / itemStackHeight;

		final int width = columns * itemStackWidth;
		final int height = rows * itemStackHeight;
		final int x = area.x + (area.width - width) / 2;
		final int y = area.y + (area.height - height) / 2;

		this.guiAreas = guiAreas;
		this.area = new Rectangle(x, y, width, height);
		this.guiIngredientList = new GuiIngredientFastList(ingredientRegistry);

		for (int row = 0; row < rows; row++) {
			int y1 = y + (row * itemStackHeight);
			for (int column = 0; column < columns; column++) {
				int x1 = x + (column * itemStackWidth);
				GuiIngredientFast guiIngredientFast = new GuiIngredientFast(x1, y1, itemStackPadding);
				Rectangle stackArea = guiIngredientFast.getArea();
				if (!GuiAreaHelper.intersects(guiAreas, stackArea)) {
					this.guiIngredientList.add(guiIngredientFast);
				}
			}
		}
	}

	public Rectangle getArea() {
		return area;
	}

	public void draw(Minecraft minecraft, int mouseX, int mouseY, Set<ItemStack> highlightedStacks) {
		GlStateManager.disableBlend();

		if (shouldShowDeleteItemTooltip(minecraft)) {
			hovered = guiIngredientList.render(minecraft, false, mouseX, mouseY);
		} else {
			boolean mouseOver = isMouseOver(mouseX, mouseY);
			hovered = guiIngredientList.render(minecraft, mouseOver, mouseX, mouseY);
		}

		if (!highlightedStacks.isEmpty()) {
			StackHelper helper = Internal.getHelpers().getStackHelper();
			for (GuiIngredientFast guiItemStack : guiIngredientList.getAllGuiIngredients()) {
				Object ingredient = guiItemStack.getIngredient();
				if (ingredient instanceof ItemStack) {
					if (helper.containsStack(highlightedStacks, (ItemStack) ingredient) != null) {
						guiItemStack.drawHighlight();
					}
				}
			}
		}

		if (hovered != null) {
			hovered.drawHovered(minecraft);
		}

		GlStateManager.enableAlpha();
	}

	public void drawTooltips(Minecraft minecraft, int mouseX, int mouseY) {
		boolean mouseOver = isMouseOver(mouseX, mouseY);
		if (mouseOver && shouldShowDeleteItemTooltip(minecraft)) {
			String deleteItem = Translator.translateToLocal("jei.tooltip.delete.item");
			TooltipRenderer.drawHoveringText(minecraft, deleteItem, mouseX, mouseY);
		}

		if (hovered != null) {
			hovered.drawTooltip(minecraft, mouseX, mouseY);
		}
	}

	private static boolean shouldShowDeleteItemTooltip(Minecraft minecraft) {
		if (Config.isDeleteItemsInCheatModeActive()) {
			EntityPlayer player = minecraft.player;
			if (!player.inventory.getItemStack().isEmpty()) {
				JeiRuntime runtime = Internal.getRuntime();
				return runtime == null || !runtime.getRecipesGui().isOpen();
			}
		}
		return false;
	}

	public boolean isMouseOver(int mouseX, int mouseY) {
		return area.contains(mouseX, mouseY) &&
				!GuiAreaHelper.isMouseOverGuiArea(guiAreas, mouseX, mouseY);
	}

	@Nullable
	public ItemStack getStackUnderMouse() {
		if (hovered != null) {
			Object ingredient = hovered.getIngredient();
			if (ingredient instanceof ItemStack) {
				return (ItemStack) ingredient;
			}
		}
		return null;
	}

	@Override
	@Nullable
	public IClickedIngredient<?> getIngredientUnderMouse(int mouseX, int mouseY) {
		if (isMouseOver(mouseX, mouseY)) {
			ClickedIngredient<?> clicked = guiIngredientList.getIngredientUnderMouse(mouseX, mouseY);
			if (clicked != null) {
				clicked.setAllowsCheating();
			}
			return clicked;
		}
		return null;
	}

	@Override
	public boolean canSetFocusWithMouse() {
		return true;
	}
}
