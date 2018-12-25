package mezz.jei.gui.overlay;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.ItemStack;

import mezz.jei.config.ClientConfig;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.GuiScreenHelper;
import mezz.jei.gui.PageNavigation;
import mezz.jei.gui.ghost.IGhostIngredientDragSource;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IMouseHandler;
import mezz.jei.input.IPaged;
import mezz.jei.input.IShowsRecipeFocuses;
import mezz.jei.input.MouseUtil;
import mezz.jei.render.IngredientListSlot;
import mezz.jei.render.IngredientRenderer;
import mezz.jei.util.CommandUtil;
import mezz.jei.util.MathUtil;

/**
 * Displays a list of ingredients with navigation at the top.
 */
public class IngredientGridWithNavigation implements IShowsRecipeFocuses, IMouseHandler, IGhostIngredientDragSource {
	private static final int NAVIGATION_HEIGHT = 20;

	private int firstItemIndex = 0;
	private final IPaged pageDelegate;
	private final PageNavigation navigation;
	private final GuiScreenHelper guiScreenHelper;
	private final IngredientGrid ingredientGrid;
	private final IIngredientGridSource ingredientSource;
	private Rectangle area = new Rectangle();

	public IngredientGridWithNavigation(IIngredientGridSource ingredientSource, GuiScreenHelper guiScreenHelper, GridAlignment alignment) {
		this.ingredientGrid = new IngredientGrid(alignment);
		this.ingredientSource = ingredientSource;
		this.guiScreenHelper = guiScreenHelper;
		this.pageDelegate = new IngredientGridPaged();
		this.navigation = new PageNavigation(this.pageDelegate, false);
	}

	public void updateLayout(boolean resetToFirstPage) {
		if (resetToFirstPage) {
			firstItemIndex = 0;
		}
		List<IIngredientListElement> ingredientList = ingredientSource.getIngredientList();
		if (firstItemIndex >= ingredientList.size()) {
			firstItemIndex = 0;
		}
		this.ingredientGrid.guiIngredientSlots.set(firstItemIndex, ingredientList);
		this.navigation.updatePageState();
	}

	public boolean updateBounds(Rectangle availableArea, Set<Rectangle> guiExclusionAreas, int minWidth) {
		Rectangle estimatedNavigationArea = new Rectangle(
			availableArea.x,
			availableArea.y,
			availableArea.width,
			NAVIGATION_HEIGHT
		);
		Rectangle movedNavigationArea = MathUtil.moveDownToAvoidIntersection(guiExclusionAreas, estimatedNavigationArea);
		int navigationMaxY = movedNavigationArea.y + movedNavigationArea.height;
		Rectangle boundsWithoutNavigation = new Rectangle(
			availableArea.x,
			navigationMaxY,
			availableArea.width,
			availableArea.height - navigationMaxY
		);
		boolean gridHasRoom = this.ingredientGrid.updateBounds(boundsWithoutNavigation, minWidth, guiExclusionAreas);
		if (!gridHasRoom) {
			return false;
		}
		Rectangle displayArea = this.ingredientGrid.getArea();
		Rectangle navigationArea = new Rectangle(displayArea.x, movedNavigationArea.y, displayArea.width, NAVIGATION_HEIGHT);
		this.navigation.updateBounds(navigationArea);
		this.area = displayArea.union(navigationArea);
		return true;
	}

	public Rectangle getArea() {
		return this.area;
	}

	public void draw(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
		this.ingredientGrid.draw(minecraft, mouseX, mouseY);
		this.navigation.draw(minecraft, mouseX, mouseY, partialTicks);
	}

	public void drawTooltips(Minecraft minecraft, int mouseX, int mouseY) {
		this.ingredientGrid.drawTooltips(minecraft, mouseX, mouseY);
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return this.area.contains(mouseX, mouseY) &&
			!guiScreenHelper.isInGuiExclusionArea(mouseX, mouseY);
	}

	@Override
	public boolean handleMouseClicked(double mouseX, double mouseY, int mouseButton) {
		return !guiScreenHelper.isInGuiExclusionArea(mouseX, mouseY) &&
			(this.ingredientGrid.handleMouseClicked(mouseX, mouseY) ||
				this.navigation.handleMouseClickedButtons(mouseX, mouseY, mouseButton));
	}

	@Override
	public boolean handleMouseScrolled(double mouseX, double mouseY, double scrollDelta) {
		if (scrollDelta < 0) {
			this.pageDelegate.nextPage();
			return true;
		} else if (scrollDelta > 0) {
			this.pageDelegate.previousPage();
			return true;
		}
		return false;
	}

	public boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
		InputMappings.Input input = InputMappings.getInputByCode(keyCode, scanCode);
		if (KeyBindings.nextPage.isActiveAndMatches(input)) {
			this.pageDelegate.nextPage();
			return true;
		} else if (KeyBindings.previousPage.isActiveAndMatches(input)) {
			this.pageDelegate.previousPage();
			return true;
		}
		return checkHotbarKeys(input);
	}

	/**
	 * Modeled after GuiContainer#checkHotbarKeys(int)
	 * Sets the stack in a hotbar slot to the one that's hovered over.
	 */
	protected boolean checkHotbarKeys(InputMappings.Input input) {
		GuiScreen guiScreen = Minecraft.getInstance().currentScreen;
		if (ClientConfig.getInstance().isCheatItemsEnabled() && guiScreen != null && !(guiScreen instanceof RecipesGui)) {
			final double mouseX = MouseUtil.getX();
			final double mouseY = MouseUtil.getY();
			if (isMouseOver(mouseX, mouseY)) {
				GameSettings gameSettings = Minecraft.getInstance().gameSettings;
				for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot) {
					if (gameSettings.keyBindsHotbar[hotbarSlot].isActiveAndMatches(input)) {
						IClickedIngredient<?> ingredientUnderMouse = getIngredientUnderMouse(mouseX, mouseY);
						if (ingredientUnderMouse != null) {
							ItemStack itemStack = ingredientUnderMouse.getCheatItemStack();
							if (!itemStack.isEmpty()) {
								CommandUtil.setHotbarStack(itemStack, hotbarSlot);
							}
							ingredientUnderMouse.onClickHandled();
						}
						return true;
					}
				}
			}
		}
		return false;
	}

	@Nullable
	@Override
	public IClickedIngredient<?> getIngredientUnderMouse(double mouseX, double mouseY) {
		return this.ingredientGrid.getIngredientUnderMouse(mouseX, mouseY);
	}

	@Nullable
	@Override
	public IIngredientListElement getElementUnderMouse() {
		return this.ingredientGrid.getElementUnderMouse();
	}

	@Override
	public boolean canSetFocusWithMouse() {
		return this.ingredientGrid.canSetFocusWithMouse();
	}

	public List<IIngredientListElement> getVisibleElements() {
		List<IIngredientListElement> visibleElements = new ArrayList<>();
		for (IngredientListSlot slot : this.ingredientGrid.guiIngredientSlots.getAllGuiIngredientSlots()) {
			IngredientRenderer renderer = slot.getIngredientRenderer();
			if (renderer != null) {
				visibleElements.add(renderer.getElement());
			}
		}
		return visibleElements;
	}

	private class IngredientGridPaged implements IPaged {
		@Override
		public boolean nextPage() {
			final int itemsCount = ingredientSource.size();
			if (itemsCount > 0) {
				firstItemIndex += ingredientGrid.size();
				if (firstItemIndex >= itemsCount) {
					firstItemIndex = 0;
				}
				updateLayout(false);
				return true;
			} else {
				firstItemIndex = 0;
				updateLayout(false);
				return false;
			}
		}

		@Override
		public boolean previousPage() {
			final int itemsPerPage = ingredientGrid.size();
			if (itemsPerPage == 0) {
				firstItemIndex = 0;
				updateLayout(false);
				return false;
			}
			final int itemsCount = ingredientSource.size();

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
			updateLayout(false);
			return true;
		}

		@Override
		public boolean hasNext() {
			// true if there is more than one page because this wraps around
			int itemsPerPage = ingredientGrid.size();
			return itemsPerPage > 0 && ingredientSource.size() > itemsPerPage;
		}

		@Override
		public boolean hasPrevious() {
			// true if there is more than one page because this wraps around
			int itemsPerPage = ingredientGrid.size();
			return itemsPerPage > 0 && ingredientSource.size() > itemsPerPage;
		}

		@Override
		public int getPageCount() {
			final int itemCount = ingredientSource.size();
			final int stacksPerPage = ingredientGrid.size();
			if (stacksPerPage == 0) {
				return 1;
			}
			int pageCount = MathUtil.divideCeil(itemCount, stacksPerPage);
			pageCount = Math.max(1, pageCount);
			return pageCount;
		}

		@Override
		public int getPageNumber() {
			final int stacksPerPage = ingredientGrid.size();
			if (stacksPerPage == 0) {
				return 0;
			}
			return firstItemIndex / stacksPerPage;
		}
	}
}
