package mezz.jei.gui.overlay;

import mezz.jei.Internal;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.config.Config;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.PageNavigation;
import mezz.jei.gui.ghost.IGhostIngredientDragSource;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IMouseHandler;
import mezz.jei.input.IPaged;
import mezz.jei.input.IShowsRecipeFocuses;
import mezz.jei.input.MouseHelper;
import mezz.jei.render.IngredientListSlot;
import mezz.jei.render.IngredientRenderer;
import mezz.jei.runtime.JeiRuntime;
import mezz.jei.util.CommandUtil;
import mezz.jei.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Displays all known recipe ingredients.
 */
public class IngredientGridAll implements IShowsRecipeFocuses, IMouseHandler, IGhostIngredientDragSource {
	private static final int NAVIGATION_HEIGHT = 20;
	private static int firstItemIndex = 0;

	private final IPaged pageDelegate;
	private final PageNavigation navigation;
	private final IngredientGrid ingredientGrid;
	private final IngredientFilter ingredientFilter;
	private Rectangle area = new Rectangle();
	private Set<Rectangle> guiExclusionAreas = Collections.emptySet();

	public IngredientGridAll(IngredientFilter ingredientFilter) {
		this.ingredientGrid = new IngredientGrid();
		this.ingredientFilter = ingredientFilter;
		this.pageDelegate = new IngredientGridPaged();
		this.navigation = new PageNavigation(this.pageDelegate, false);
	}

	public void updateLayout(boolean filterChanged) {
		if (filterChanged) {
			firstItemIndex = 0;
		}
		this.ingredientGrid.updateLayout(this.guiExclusionAreas);
		List<IIngredientListElement> ingredientList = ingredientFilter.getIngredientList();
		this.ingredientGrid.guiIngredientSlots.set(firstItemIndex, ingredientList);
		this.navigation.updatePageState();
	}

	public void updateBounds(Rectangle availableArea, int minWidth) {
		Rectangle boundsWithoutNavigation = new Rectangle(
			availableArea.x,
			availableArea.y + NAVIGATION_HEIGHT,
			availableArea.width,
			availableArea.height - NAVIGATION_HEIGHT
		);
		this.ingredientGrid.updateBounds(boundsWithoutNavigation, minWidth, this.guiExclusionAreas);
		Rectangle displayArea = this.ingredientGrid.getArea();
		Rectangle navigationArea = new Rectangle(displayArea.x, availableArea.y, displayArea.width, NAVIGATION_HEIGHT);
		this.navigation.updateBounds(navigationArea);
		this.area = displayArea.union(navigationArea);
	}

	public Rectangle getArea() {
		return this.area;
	}

	public void draw(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
		if (this.updateGuiExclusionAreas()) {
			updateLayout(false);
		}
		this.ingredientGrid.draw(minecraft, mouseX, mouseY);
		this.navigation.draw(minecraft, mouseX, mouseY, partialTicks);
	}

	public void drawTooltips(Minecraft minecraft, int mouseX, int mouseY) {
		this.ingredientGrid.drawTooltips(minecraft, mouseX, mouseY);
	}

	@Override
	public boolean isMouseOver(int mouseX, int mouseY) {
		return this.area.contains(mouseX, mouseY) &&
			!MathUtil.contains(guiExclusionAreas, mouseX, mouseY);
	}

	@Override
	public boolean handleMouseClicked(int mouseX, int mouseY, int mouseButton) {
		return this.ingredientGrid.handleMouseClicked(mouseX, mouseY) ||
			this.navigation.handleMouseClickedButtons(mouseX, mouseY);
	}

	@Override
	public boolean handleMouseScrolled(int mouseX, int mouseY, int scrollDelta) {
		if (scrollDelta < 0) {
			this.pageDelegate.nextPage();
			return true;
		} else if (scrollDelta > 0) {
			this.pageDelegate.previousPage();
			return true;
		}
		return false;
	}

	public boolean onKeyPressed(char typedChar, int keyCode) {
		if (KeyBindings.nextPage.isActiveAndMatches(keyCode)) {
			this.pageDelegate.nextPage();
			return true;
		} else if (KeyBindings.previousPage.isActiveAndMatches(keyCode)) {
			this.pageDelegate.previousPage();
			return true;
		}
		return checkHotbarKeys(keyCode);
	}

	/**
	 * Modeled after {@link GuiContainer#checkHotbarKeys(int)}
	 * Sets the stack in a hotbar slot to the one that's hovered over.
	 */
	protected boolean checkHotbarKeys(int keyCode) {
		GuiScreen guiScreen = Minecraft.getMinecraft().currentScreen;
		if (Config.isCheatItemsEnabled() && guiScreen != null && !(guiScreen instanceof RecipesGui)) {
			final int mouseX = MouseHelper.getX();
			final int mouseY = MouseHelper.getY();
			if (isMouseOver(mouseX, mouseY)) {
				GameSettings gameSettings = Minecraft.getMinecraft().gameSettings;
				for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot) {
					if (gameSettings.keyBindsHotbar[hotbarSlot].isActiveAndMatches(keyCode)) {
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
	public IClickedIngredient<?> getIngredientUnderMouse(int mouseX, int mouseY) {
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

	public boolean updateGuiExclusionAreas() {
		final Set<Rectangle> guiAreas = getGuiAreas();
		if (!guiAreas.equals(this.guiExclusionAreas)) {
			this.guiExclusionAreas = guiAreas;
			return true;
		}
		return false;
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

	private static Set<Rectangle> getGuiAreas() {
		final GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
		if (currentScreen instanceof GuiContainer) {
			final GuiContainer guiContainer = (GuiContainer) currentScreen;
			final JeiRuntime jeiRuntime = Internal.getRuntime();
			if (jeiRuntime != null) {
				final Set<Rectangle> allGuiExtraAreas = new HashSet<>();
				final List<IAdvancedGuiHandler<GuiContainer>> activeAdvancedGuiHandlers = jeiRuntime.getActiveAdvancedGuiHandlers(guiContainer);
				for (IAdvancedGuiHandler<GuiContainer> advancedGuiHandler : activeAdvancedGuiHandlers) {
					final List<Rectangle> guiExtraAreas = advancedGuiHandler.getGuiExtraAreas(guiContainer);
					if (guiExtraAreas != null) {
						allGuiExtraAreas.addAll(guiExtraAreas);
					}
				}
				return allGuiExtraAreas;
			}
		}
		return Collections.emptySet();
	}

	private class IngredientGridPaged implements IPaged {
		@Override
		public boolean nextPage() {
			final int itemsCount = ingredientFilter.size();
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
			final int itemsCount = ingredientFilter.size();

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
			return itemsPerPage > 0 && ingredientFilter.size() > itemsPerPage;
		}

		@Override
		public boolean hasPrevious() {
			// true if there is more than one page because this wraps around
			int itemsPerPage = ingredientGrid.size();
			return itemsPerPage > 0 && ingredientFilter.size() > itemsPerPage;
		}

		@Override
		public int getPageCount() {
			final int itemCount = ingredientFilter.size();
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
				return 1;
			}
			return firstItemIndex / stacksPerPage;
		}
	}
}
