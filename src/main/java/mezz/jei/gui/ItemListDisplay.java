package mezz.jei.gui;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import mezz.jei.Internal;
import mezz.jei.ItemFilter;
import mezz.jei.JeiRuntime;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.config.Config;
import mezz.jei.gui.ingredients.GuiIngredientFast;
import mezz.jei.gui.ingredients.GuiIngredientFastList;
import mezz.jei.gui.ingredients.GuiItemStackGroup;
import mezz.jei.input.ClickedIngredient;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IShowsRecipeFocuses;
import mezz.jei.util.MathUtil;
import mezz.jei.util.StackHelper;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class ItemListDisplay implements IShowsRecipeFocuses {
	private static final int itemStackPadding = 1;
	private static final int itemStackWidth = GuiItemStackGroup.getWidth(itemStackPadding);
	private static final int itemStackHeight = GuiItemStackGroup.getHeight(itemStackPadding);
	private static int firstItemIndex = 0;

	private final GuiIngredientFastList guiIngredientList;
	private final Rectangle area;

	@Nullable
	private GuiIngredientFast hovered = null;
	@Nullable
	private final List<Rectangle> guiAreas;

	public ItemListDisplay(IIngredientRegistry ingredientRegistry, Rectangle area, @Nullable List<Rectangle> guiAreas) {
		this.guiIngredientList = new GuiIngredientFastList(ingredientRegistry);
		this.guiAreas = guiAreas;

		final int columns = area.width / itemStackWidth;
		final int rows = area.height / itemStackHeight;

		final int width = columns * itemStackWidth;
		final int height = rows * itemStackHeight;
		final int x = area.x + (area.width - width) / 2;
		final int y = area.y + (area.height - height) / 2;

		this.area = new Rectangle(x, y, width, height);
		createItemButtons(guiIngredientList, guiAreas, x, y, columns, rows);
	}

	public Rectangle getArea() {
		return area;
	}

	private static void createItemButtons(GuiIngredientFastList guiItemStacks, @Nullable List<Rectangle> guiAreas, final int xStart, final int yStart, final int columnCount, final int rowCount) {
		guiItemStacks.clear();

		for (int row = 0; row < rowCount; row++) {
			int y = yStart + (row * itemStackHeight);
			for (int column = 0; column < columnCount; column++) {
				int x = xStart + (column * itemStackWidth);
				GuiIngredientFast guiIngredientFast = new GuiIngredientFast(x, y, itemStackPadding);
				if (guiAreas != null) {
					Rectangle stackArea = guiIngredientFast.getArea();
					if (intersects(guiAreas, stackArea)) {
						continue;
					}
				}
				guiItemStacks.add(guiIngredientFast);
			}
		}
	}

	private static boolean intersects(List<Rectangle> areas, Rectangle comparisonArea) {
		for (Rectangle area : areas) {
			if (area.intersects(comparisonArea)) {
				return true;
			}
		}
		return false;
	}

	public void updateLayout(ItemFilter itemFilter) {
		ImmutableList<Object> ingredientList = itemFilter.getIngredientList();
		guiIngredientList.set(firstItemIndex, ingredientList);
	}

	public boolean nextPage(ItemFilter itemFilter) {
		final int itemsCount = itemFilter.size();
		if (itemsCount > 0) {
			firstItemIndex += guiIngredientList.size();
			if (firstItemIndex >= itemsCount) {
				firstItemIndex = 0;
			}

			return true;
		} else {
			firstItemIndex = 0;
			return false;
		}
	}

	public boolean previousPage(ItemFilter itemFilter) {
		final int itemsPerPage = guiIngredientList.size();
		if (itemsPerPage == 0) {
			firstItemIndex = 0;
			return false;
		}
		final int itemsCount = itemFilter.size();

		int pageNum = firstItemIndex / itemsPerPage;
		if (pageNum == 0) {
			pageNum = itemsCount / itemsPerPage;
		} else {
			pageNum--;
		}

		firstItemIndex = itemsPerPage * pageNum;
		if (firstItemIndex > 0 && firstItemIndex == itemsCount) {
			pageNum--;
			firstItemIndex = itemsPerPage * pageNum;
		}
		return true;
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

	public boolean isMouseOver(int mouseX, int mouseY) {
		return area.contains(mouseX, mouseY) &&
				!isMouseOverGuiArea(mouseX, mouseY);
	}

	private boolean isMouseOverGuiArea(int mouseX, int mouseY) {
		if (guiAreas != null) {
			for (Rectangle guiArea : guiAreas) {
				if (guiArea.contains(mouseX, mouseY)) {
					return true;
				}
			}
		}
		return false;
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

	public ImmutableList<ItemStack> getVisibleStacks() {
		ImmutableList.Builder<ItemStack> visibleStacks = ImmutableList.builder();
		for (GuiIngredientFast guiItemStack : guiIngredientList.getAllGuiIngredients()) {
			Object ingredient = guiItemStack.getIngredient();
			if (ingredient instanceof ItemStack) {
				ItemStack itemStack = (ItemStack) ingredient;
				visibleStacks.add(itemStack);
			}
		}
		return visibleStacks.build();
	}

	public static void setToFirstPage() {
		firstItemIndex = 0;
	}

	public int getPageCount(ItemFilter itemFilter) {
		final int itemCount = itemFilter.size();
		final int stacksPerPage = guiIngredientList.size();
		if (stacksPerPage == 0) {
			return 1;
		}
		int pageCount = MathUtil.divideCeil(itemCount, stacksPerPage);
		pageCount = Math.max(1, pageCount);
		return pageCount;
	}

	public int getPageNum() {
		final int stacksPerPage = guiIngredientList.size();
		if (stacksPerPage == 0) {
			return 1;
		}
		return firstItemIndex / stacksPerPage;
	}

	private boolean shouldShowDeleteItemTooltip(Minecraft minecraft) {
		if (Config.isDeleteItemsInCheatModeActive()) {
			EntityPlayer player = minecraft.player;
			if (!player.inventory.getItemStack().isEmpty()) {
				JeiRuntime runtime = Internal.getRuntime();
				return runtime == null || !runtime.getRecipesGui().isOpen();
			}
		}
		return false;
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
