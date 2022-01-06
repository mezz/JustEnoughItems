package mezz.jei.gui.overlay;

import com.mojang.blaze3d.matrix.MatrixStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import mezz.jei.input.CombinedMouseHandler;
import mezz.jei.util.Rectangle2dBuilder;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.ItemStack;

import mezz.jei.config.IClientConfig;
import mezz.jei.config.IFilterTextSource;
import mezz.jei.config.IWorldConfig;
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
import mezz.jei.render.IngredientListElementRenderer;
import mezz.jei.render.IngredientListSlot;
import mezz.jei.util.CommandUtil;
import mezz.jei.util.MathUtil;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.util.Size2i;

/**
 * Displays a list of ingredients with navigation at the top.
 */
public class IngredientGridWithNavigation implements IShowsRecipeFocuses, IGhostIngredientDragSource {
	private static final int NAVIGATION_HEIGHT = 20;

	private int firstItemIndex = 0;
	private final IngredientGridPaged pageDelegate;
	private final PageNavigation navigation;
	private final GuiScreenHelper guiScreenHelper;
	private final IFilterTextSource filterTextSource;
	private final IWorldConfig worldConfig;
	private final IClientConfig clientConfig;
	private final IngredientGrid ingredientGrid;
	private final IIngredientGridSource ingredientSource;
	private final IMouseHandler mouseHandler;
	private Rectangle2d area = new Rectangle2d(0, 0, 0, 0);

	public IngredientGridWithNavigation(
		IIngredientGridSource ingredientSource,
		IFilterTextSource filterTextSource,
		GuiScreenHelper guiScreenHelper,
		IngredientGrid ingredientGrid,
		IWorldConfig worldConfig,
		IClientConfig clientConfig
	) {
		this.filterTextSource = filterTextSource;
		this.worldConfig = worldConfig;
		this.clientConfig = clientConfig;
		this.ingredientGrid = ingredientGrid;
		this.ingredientSource = ingredientSource;
		this.guiScreenHelper = guiScreenHelper;
		this.pageDelegate = new IngredientGridPaged();
		this.navigation = new PageNavigation(this.pageDelegate, false);
		this.mouseHandler = new CombinedMouseHandler(this.pageDelegate, this.ingredientGrid.getMouseHandler(), this.navigation.getMouseHandler());
	}

	public void updateLayout(boolean resetToFirstPage) {
		if (resetToFirstPage) {
			firstItemIndex = 0;
		}
		String filterText = filterTextSource.getFilterText();
		List<IIngredientListElement<?>> ingredientList = ingredientSource.getIngredientList(filterText);
		if (firstItemIndex >= ingredientList.size()) {
			firstItemIndex = 0;
		}
		this.ingredientGrid.guiIngredientSlots.set(firstItemIndex, ingredientList);
		this.navigation.updatePageState();
	}

	public boolean updateBounds(Rectangle2d availableArea, Set<Rectangle2d> guiExclusionAreas) {
		Tuple<Rectangle2d, Rectangle2d> result = MathUtil.splitY(availableArea, NAVIGATION_HEIGHT);
		final Rectangle2d estimatedNavigationArea = result.getA();

		Collection<Rectangle2d> intersectsNavigationArea = guiExclusionAreas.stream()
			.filter(rectangle2d -> MathUtil.intersects(rectangle2d, estimatedNavigationArea))
			.collect(Collectors.toList());

		final int maxWidth = this.ingredientGrid.maxWidth();
		final int maxHeight = availableArea.getHeight();
		if (maxWidth <= 0 || maxHeight <= 0) {
			return false;
		}
		Size2i maxContentSize = new Size2i(maxWidth, maxHeight);
		availableArea = MathUtil.cropToAvoidIntersection(intersectsNavigationArea, availableArea, maxContentSize);
		if (MathUtil.contentArea(availableArea, maxContentSize) == 0) {
			return false;
		}

		result = MathUtil.splitY(availableArea, NAVIGATION_HEIGHT);
		Rectangle2d navigationArea = result.getA();
		Rectangle2d boundsWithoutNavigation = result.getB();
		boolean gridHasRoom = this.ingredientGrid.updateBounds(boundsWithoutNavigation, guiExclusionAreas);
		if (!gridHasRoom) {
			return false;
		}
		Rectangle2d displayArea = this.ingredientGrid.getArea();
		navigationArea = new Rectangle2dBuilder(navigationArea)
			.setX(displayArea)
			.setWidth(displayArea)
			.build();
		this.navigation.updateBounds(navigationArea);
		this.area = MathUtil.union(displayArea, navigationArea);
		return true;
	}

	public Rectangle2d getArea() {
		return this.area;
	}

	public void draw(Minecraft minecraft, MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.ingredientGrid.draw(minecraft, matrixStack, mouseX, mouseY);
		this.navigation.draw(minecraft, matrixStack, mouseX, mouseY, partialTicks);
	}

	public void drawTooltips(Minecraft minecraft, MatrixStack matrixStack, int mouseX, int mouseY) {
		this.ingredientGrid.drawTooltips(minecraft, matrixStack, mouseX, mouseY);
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return MathUtil.contains(this.area, mouseX, mouseY) &&
			!guiScreenHelper.isInGuiExclusionArea(mouseX, mouseY);
	}

	public IMouseHandler getMouseHandler() {
		return mouseHandler;
	}

	public boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
		InputMappings.Input input = InputMappings.getKey(keyCode, scanCode);
		if (KeyBindings.nextPage.isActiveAndMatches(input)) {
			this.pageDelegate.nextPage();
			return true;
		} else if (KeyBindings.previousPage.isActiveAndMatches(input)) {
			this.pageDelegate.previousPage();
			return true;
		}
		if (clientConfig.isCheatToHotbarUsingHotkeysEnabled()) {
			return checkHotbarKeys(input);
		}
		return false;
	}

	/**
	 * Modeled after ContainerScreen#checkHotbarKeys(int)
	 * Sets the stack in a hotbar slot to the one that's hovered over.
	 */
	protected boolean checkHotbarKeys(InputMappings.Input input) {
		Screen guiScreen = Minecraft.getInstance().screen;
		if (worldConfig.isCheatItemsEnabled() && guiScreen != null && !(guiScreen instanceof RecipesGui)) {
			final double mouseX = MouseUtil.getX();
			final double mouseY = MouseUtil.getY();
			if (isMouseOver(mouseX, mouseY)) {
				GameSettings gameSettings = Minecraft.getInstance().options;
				for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot) {
					if (gameSettings.keyHotbarSlots[hotbarSlot].isActiveAndMatches(input)) {
						IClickedIngredient<?> ingredientUnderMouse = getIngredientUnderMouse(mouseX, mouseY);
						if (ingredientUnderMouse != null) {
							ItemStack itemStack = ingredientUnderMouse.getCheatItemStack();
							if (!itemStack.isEmpty()) {
								CommandUtil.setHotbarStack(itemStack, hotbarSlot);
							}
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
	public IIngredientListElement<?> getElementUnderMouse() {
		return this.ingredientGrid.getElementUnderMouse();
	}

	@Override
	public boolean canSetFocusWithMouse() {
		return this.ingredientGrid.canSetFocusWithMouse();
	}

	public List<IIngredientListElement<?>> getVisibleElements() {
		List<IIngredientListElement<?>> visibleElements = new ArrayList<>();
		for (IngredientListSlot slot : this.ingredientGrid.guiIngredientSlots.getAllGuiIngredientSlots()) {
			IngredientListElementRenderer<?> renderer = slot.getIngredientRenderer();
			if (renderer != null) {
				visibleElements.add(renderer.getElement());
			}
		}
		return visibleElements;
	}

	private class IngredientGridPaged implements IPaged, IMouseHandler {
		@Override
		public boolean nextPage() {
			String filterText = filterTextSource.getFilterText();
			final int itemsCount = ingredientSource.getIngredientList(filterText).size();
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
			String filterText = filterTextSource.getFilterText();
			final int itemsCount = ingredientSource.getIngredientList(filterText).size();

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
			String filterText = filterTextSource.getFilterText();
			// true if there is more than one page because this wraps around
			int itemsPerPage = ingredientGrid.size();
			return itemsPerPage > 0 && ingredientSource.getIngredientList(filterText).size() > itemsPerPage;
		}

		@Override
		public boolean hasPrevious() {
			String filterText = filterTextSource.getFilterText();
			// true if there is more than one page because this wraps around
			int itemsPerPage = ingredientGrid.size();
			return itemsPerPage > 0 && ingredientSource.getIngredientList(filterText).size() > itemsPerPage;
		}

		@Override
		public int getPageCount() {
			String filterText = filterTextSource.getFilterText();
			final int itemCount = ingredientSource.getIngredientList(filterText).size();
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

		@Override
		public boolean handleMouseScrolled(double mouseX, double mouseY, double scrollDelta) {
			if (!isMouseOver(mouseX, mouseY)) {
				return false;
			}
			if (scrollDelta < 0) {
				this.nextPage();
				return true;
			} else if (scrollDelta > 0) {
				this.previousPage();
				return true;
			}
			return false;
		}
	}
}
