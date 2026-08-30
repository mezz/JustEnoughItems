package mezz.jei.gui.overlay.ingredients;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.PageNavigation;
import mezz.jei.gui.ghost.GhostIngredientDragManager;
import mezz.jei.gui.ghost.GhostIngredientQuickMoveManager;
import mezz.jei.gui.input.DelegatingClickableIngredientInternal;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IDragHandler;
import mezz.jei.gui.input.IDraggableIngredientInternal;
import mezz.jei.gui.input.IMouseOverable;
import mezz.jei.gui.input.IPaged;
import mezz.jei.gui.input.IRecipeFocusSource;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.input.handlers.CombinedInputHandler;
import mezz.jei.gui.input.handlers.SameElementInputHandler;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.gui.util.CommandUtil;
import mezz.jei.gui.util.FocusUtil;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Displays a list of ingredients with navigation at the top.
 */
public class IngredientGridWithNavigation implements IRecipeFocusSource {
	private final IngredientGridPageState pageState = new IngredientGridPageState();
	private final IngredientGridPaged pageDelegate;
	private final IngredientGridScrollController scrollController;
	private final PageNavigation navigation;
	private final IngredientGridScrollbar scrollbar;
	private final IIngredientGridConfig gridConfig;
	private final IIngredientManager ingredientManager;
	private final String debugName;
	private final IClientToggleState toggleState;
	private final IClientConfig clientConfig;
	private final IngredientGrid ingredientGrid;
	private final IIngredientGridSource ingredientSource;
	private final DrawableNineSliceTexture background;
	private final DrawableNineSliceTexture slotBackground;
	private final CommandUtil commandUtil;
	private final GhostIngredientDragManager ghostIngredientDragManager;
	private final GhostIngredientQuickMoveManager ghostIngredientQuickMoveManager;

	private ImmutableRect2i backgroundArea = ImmutableRect2i.EMPTY;
	private ImmutableRect2i slotBackgroundArea = ImmutableRect2i.EMPTY;
	private Set<ImmutableRect2i> guiExclusionAreas = Set.of();
	private boolean active;

	public IngredientGridWithNavigation(
		String debugName,
		IIngredientGridSource ingredientSource,
		IngredientGrid ingredientGrid,
		IClientToggleState toggleState,
		IClientConfig clientConfig,
		IConnectionToServer serverConnection,
		IIngredientGridConfig gridConfig,
		DrawableNineSliceTexture background,
		DrawableNineSliceTexture slotBackground,
		IScreenHelper screenHelper,
		IIngredientManager ingredientManager
	) {
		this.debugName = debugName;
		this.toggleState = toggleState;
		this.clientConfig = clientConfig;
		this.ingredientGrid = ingredientGrid;
		this.ingredientSource = ingredientSource;
		this.gridConfig = gridConfig;
		this.ingredientManager = ingredientManager;
		this.pageDelegate = new IngredientGridPaged();
		this.scrollController = new IngredientGridScrollController(
			ingredientSource,
			this.ingredientGrid,
			gridConfig,
			clientConfig
		);
		this.navigation = new PageNavigation(this.pageDelegate, false);
		this.scrollbar = new IngredientGridScrollbar(this.scrollController, this.navigation::updatePageNumber);
		this.background = background;
		this.slotBackground = slotBackground;
		this.commandUtil = new CommandUtil(clientConfig, serverConnection);
		this.ghostIngredientDragManager = new GhostIngredientDragManager(this.ingredientGrid, screenHelper, ingredientManager, toggleState);
		this.ghostIngredientQuickMoveManager = new GhostIngredientQuickMoveManager(this.ingredientGrid, screenHelper);

		this.ingredientSource.addSourceListChangedListener(() -> {
			if (isActive()) {
				updateLayoutKeepingPageAnchorVisible(getPageAnchorElement());
			}
		});
	}

	private boolean isActive() {
		return active;
	}

	public boolean hasRoom() {
		return active;
	}

	public void updateLayout(boolean resetToFirstPage) {
		if (resetToFirstPage) {
			updateLayoutToFirstPage();
		} else {
			updateLayoutStartingAt(this.pageState.getFirstItemIndex());
		}
	}

	public void updateLayoutToFirstPage() {
		updateLayoutStartingAt(0);
	}

	public void updateLayoutKeepingPageAnchorVisible(@Nullable IElement<?> pageAnchorElement) {
		if (usesScrollbar()) {
			this.scrollController.updateLayoutKeepingScrollAnchorVisible(pageAnchorElement);
		} else {
			List<IElement<?>> ingredientList = ingredientSource.getElements();
			int firstItemIndex = this.pageState.updateKeepingPageAnchorVisible(pageAnchorElement, ingredientList, ingredientGrid.size());
			this.ingredientGrid.set(firstItemIndex, ingredientList);
		}
		this.navigation.updatePageNumber();
	}

	@Nullable
	public IElement<?> getPageAnchorElement() {
		IElement<?> pageAnchorElement;
		if (usesScrollbar()) {
			pageAnchorElement = this.scrollController.getScrollAnchorElement();
		} else {
			pageAnchorElement = this.pageState.getPageAnchorElement(ingredientSource.getElements());
		}
		if (pageAnchorElement != null) {
			return pageAnchorElement;
		}
		return this.ingredientGrid.getSlots()
			.map(IngredientListSlot::getOptionalElement)
			.flatMap(Optional::stream)
			.findFirst()
			.orElse(null);
	}

	public <T> IClickableIngredientInternal<T> createPageAnchorIngredient(IClickableIngredientInternal<T> delegate) {
		return new PageAnchorClickableIngredient<>(delegate);
	}

	private void updateLayoutStartingAt(int firstItemIndex) {
		if (usesScrollbar()) {
			this.scrollController.updateLayoutStartingAt(firstItemIndex);
		} else {
			List<IElement<?>> ingredientList = ingredientSource.getElements();
			int renderFirstItemIndex = this.pageState.updateForPageNavigation(firstItemIndex, ingredientList.size(), ingredientGrid.size());
			this.ingredientGrid.set(renderFirstItemIndex, ingredientList);
		}
		this.navigation.updatePageNumber();
		if (!usesScrollbar()) {
			setAnchorToFirstVisible();
		}
	}

	private void setAnchorToFirstVisible() {
		this.ingredientGrid.getSlots()
			.map(IngredientListSlot::getOptionalElement)
			.flatMap(Optional::stream)
			.findFirst()
			.ifPresent(this.pageState::setPageAnchorElement);
	}

	public void updateBounds(final ImmutableRect2i availableArea, Set<ImmutableRect2i> guiExclusionAreas, @Nullable ImmutablePoint2i mouseExclusionPoint) {
		this.guiExclusionAreas = guiExclusionAreas;
		IngredientGridWithNavigationLayout layout = calculateLayout(
			availableArea,
			guiExclusionAreas,
			mouseExclusionPoint,
			ingredientSource.getElements().size()
		);
		if (!layout.hasRoom()) {
			clearLayout();
			return;
		}
		this.ingredientGrid.updateBounds(layout.ingredientGridArea(), guiExclusionAreas, mouseExclusionPoint);
		this.slotBackgroundArea = layout.slotBackgroundArea();
		this.navigation.updateBounds(layout.navigationArea());
		this.scrollbar.updateBounds(layout.scrollbarArea());
		this.backgroundArea = layout.backgroundArea();
		this.active = true;
	}

	private IngredientGridWithNavigationLayout calculateLayout(
		ImmutableRect2i availableArea,
		Set<ImmutableRect2i> guiExclusionAreas,
		@Nullable ImmutablePoint2i mouseExclusionPoint,
		int ingredientCount
	) {
		if (usesScrollbar()) {
			return IngredientGridScrollbarLayout.calculate(
				this.gridConfig,
				availableArea,
				guiExclusionAreas,
				mouseExclusionPoint,
				ingredientCount
			);
		}

		return IngredientGridButtonNavigationLayout.calculate(
			this.gridConfig,
			availableArea,
			guiExclusionAreas,
			mouseExclusionPoint,
			ingredientCount
		);
	}

	private void clearLayout() {
		this.ingredientGrid.updateBounds(ImmutableRect2i.EMPTY, Set.of(), null);
		this.slotBackgroundArea = ImmutableRect2i.EMPTY;
		this.navigation.updateBounds(ImmutableRect2i.EMPTY);
		this.scrollbar.updateBounds(ImmutableRect2i.EMPTY);
		this.backgroundArea = ImmutableRect2i.EMPTY;
		this.active = false;
	}

	public ImmutableRect2i getBackgroundArea() {
		return this.backgroundArea;
	}

	public ImmutableRect2i getSlotBackgroundArea() {
		return this.slotBackgroundArea;
	}

	public ImmutableRect2i getNextPageButtonArea() {
		return this.navigation.getNextButtonArea();
	}

	public ImmutableRect2i getBackButtonArea() {
		return this.navigation.getBackButtonArea();
	}

	public IPaged getPageDelegate() {
		return pageDelegate;
	}

	public void draw(Minecraft minecraft, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (gridConfig.drawBackground()) {
			background.draw(guiGraphics, this.backgroundArea);
			slotBackground.draw(guiGraphics, this.slotBackgroundArea);
		}

		this.ingredientGrid.draw(minecraft, guiGraphics, mouseX, mouseY);
		this.scrollbar.draw(guiGraphics, mouseX, mouseY);
		this.navigation.draw(minecraft, guiGraphics, mouseX, mouseY, partialTicks);
	}

	public void drawTooltips(Minecraft minecraft, GuiGraphics guiGraphics, int mouseX, int mouseY) {
		this.ghostIngredientDragManager.drawTooltips(minecraft, guiGraphics, mouseX, mouseY);
		this.ingredientGrid.drawTooltips(minecraft, guiGraphics, mouseX, mouseY);
	}

	public void tick() {
		if (!this.active) {
			return;
		}
		this.ingredientGrid.tick();
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return this.backgroundArea.contains(mouseX, mouseY) &&
			this.guiExclusionAreas.stream()
				.noneMatch(area -> area.contains(mouseX, mouseY));
	}

	public IUserInputHandler createDeleteItemInputHandler() {
		return this.ingredientGrid.getInputHandler();
	}

	public IUserInputHandler createInputHandler() {
		return new CombinedInputHandler(
			this.debugName,
			this.scrollbar,
			new UserInputHandler(
				this.pageDelegate,
				this.scrollController,
				this.gridConfig,
				this.ingredientGrid,
				this.toggleState,
				this.clientConfig,
				this.commandUtil,
				this.ingredientManager,
				this::isMouseOver,
				this.navigation::updatePageNumber,
				this.ghostIngredientQuickMoveManager
			),
			this.navigation.createInputHandler()
		);
	}

	@Override
	public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		return this.ingredientGrid.getIngredientUnderMouse(mouseX, mouseY)
			.map(this::createPageAnchorIngredient);
	}

	@Override
	public Stream<IDraggableIngredientInternal<?>> getDraggableIngredientUnderMouse(double mouseX, double mouseY) {
		return this.ingredientGrid.getDraggableIngredientUnderMouse(mouseX, mouseY);
	}

	public <T> Stream<T> getVisibleIngredients(IIngredientType<T> ingredientType) {
		return this.ingredientGrid.getVisibleIngredients(ingredientType);
	}

	public boolean isEmpty() {
		return this.ingredientSource.getElements().isEmpty();
	}

	public void close() {
		this.active = false;
		this.ghostIngredientDragManager.stopDrag();
	}

	public void drawOnForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		this.ghostIngredientDragManager.drawOnForeground(guiGraphics, mouseX, mouseY);
	}

	public IDragHandler createDragHandler() {
		return this.ghostIngredientDragManager.createDragHandler();
	}

	public int size() {
		return this.ingredientGrid.size();
	}

	public Stream<IngredientListSlot> getSlots() {
		return this.ingredientGrid.getSlots();
	}

	private boolean usesScrollbar() {
		return this.gridConfig.getNavigationMode()
			.usesScrollbar();
	}

	private boolean updateLayoutWhenChanged(boolean layoutChanged) {
		if (layoutChanged) {
			this.navigation.updatePageNumber();
		}
		return layoutChanged;
	}

	private class IngredientGridPaged implements IPaged {
		@Override
		public boolean nextPage() {
			if (usesScrollbar()) {
				return updateLayoutWhenChanged(scrollController.scrollByRows(scrollController.getVisibleScrollRows()));
			}

			if (getPageCount() <= 1) {
				return false;
			}
			final int itemsCount = ingredientSource.getElements().size();
			if (itemsCount > 0) {
				int nextFirstItemIndex = pageState.getFirstItemIndex() + ingredientGrid.size();
				if (nextFirstItemIndex >= itemsCount) {
					nextFirstItemIndex = 0;
				}
				updateLayoutStartingAt(nextFirstItemIndex);
				return true;
			} else {
				updateLayoutStartingAt(0);
				return false;
			}
		}

		@Override
		public boolean previousPage() {
			if (usesScrollbar()) {
				return updateLayoutWhenChanged(scrollController.scrollByRows(-scrollController.getVisibleScrollRows()));
			}

			if (getPageCount() <= 1) {
				return false;
			}

			final int itemsPerPage = ingredientGrid.size();
			if (itemsPerPage == 0) {
				updateLayoutStartingAt(0);
				return false;
			}
			final int itemsCount = ingredientSource.getElements().size();

			int pageNum = pageState.getFirstItemIndex() / itemsPerPage;
			if (pageNum == 0) {
				pageNum = itemsCount / itemsPerPage;
			} else {
				pageNum--;
			}

			int previousFirstItemIndex = itemsPerPage * pageNum;
			if (previousFirstItemIndex > 0 && previousFirstItemIndex == itemsCount) {
				pageNum--;
				previousFirstItemIndex = itemsPerPage * pageNum;
			}
			updateLayoutStartingAt(previousFirstItemIndex);
			return true;
		}

		@Override
		public boolean hasNext() {
			if (usesScrollbar()) {
				return scrollController.canScroll();
			}

			// true if there is more than one page because this wraps around
			return getPageCount() > 1;
		}

		@Override
		public boolean hasPrevious() {
			if (usesScrollbar()) {
				return scrollController.canScroll();
			}

			// true if there is more than one page because this wraps around
			return getPageCount() > 1;
		}

		@Override
		public int getPageCount() {
			if (usesScrollbar()) {
				return scrollController.getHiddenScrollRows() + 1;
			}

			return IngredientGridPageState.getPageCount(ingredientSource.getElements().size(), ingredientGrid.size());
		}

		@Override
		public int getPageNumber() {
			if (usesScrollbar()) {
				return scrollController.getFirstVisibleScrollRow();
			}

			return IngredientGridPageState.getPageNumberForFirstItemIndex(pageState.getFirstItemIndex(), ingredientGrid.size(), ingredientSource.getElements().size());
		}
	}

	private class PageAnchorClickableIngredient<T> extends DelegatingClickableIngredientInternal<T> {
		PageAnchorClickableIngredient(IClickableIngredientInternal<T> delegate) {
			super(delegate);
		}

		@Override
		public void show(IRecipesGui recipesGui, FocusUtil focusUtil, List<RecipeIngredientRole> roles) {
			IElement<T> element = getElement();
			if (element.isVisible()) {
				pageState.setPageAnchorElement(element);
				scrollController.setScrollAnchorElement(element);
			}
			super.show(recipesGui, focusUtil, roles);
		}
	}

	private static class UserInputHandler implements IUserInputHandler {
		private final IngredientGridPaged paged;
		private final IngredientGridScrollController scrollController;
		private final IIngredientGridConfig gridConfig;
		private final IRecipeFocusSource focusSource;
		private final IClientToggleState toggleState;
		private final IClientConfig clientConfig;
		private final IMouseOverable mouseOverable;
		private final Runnable onLayoutChanged;
		private final CommandUtil commandUtil;
		private final IIngredientManager ingredientManager;
		private final GhostIngredientQuickMoveManager ghostIngredientQuickMoveManager;

		private UserInputHandler(
			IngredientGridPaged paged,
			IngredientGridScrollController scrollController,
			IIngredientGridConfig gridConfig,
			IRecipeFocusSource focusSource,
			IClientToggleState toggleState,
			IClientConfig clientConfig,
			CommandUtil commandUtil,
			IIngredientManager ingredientManager,
			IMouseOverable mouseOverable,
			Runnable onLayoutChanged,
			GhostIngredientQuickMoveManager ghostIngredientQuickMoveManager
		) {
			this.paged = paged;
			this.scrollController = scrollController;
			this.gridConfig = gridConfig;
			this.focusSource = focusSource;
			this.toggleState = toggleState;
			this.clientConfig = clientConfig;
			this.mouseOverable = mouseOverable;
			this.onLayoutChanged = onLayoutChanged;
			this.commandUtil = commandUtil;
			this.ingredientManager = ingredientManager;
			this.ghostIngredientQuickMoveManager = ghostIngredientQuickMoveManager;
		}

		@Override
		public Optional<IUserInputHandler> handleMouseScrolled(double mouseX, double mouseY, double scrollDeltaY) {
			if (!mouseOverable.isMouseOver(mouseX, mouseY)) {
				return Optional.empty();
			}
			if (this.gridConfig.getNavigationMode().usesScrollbar()) {
				IngredientGridScrollController.ScrollResult scrollResult = this.scrollController.scrollByMouse(scrollDeltaY);
				if (scrollResult.changed()) {
					this.onLayoutChanged.run();
				}
				if (scrollResult.consumed()) {
					return Optional.of(this);
				}
				return Optional.empty();
			}
			if (scrollDeltaY < 0) {
				if (this.paged.nextPage()) {
					return Optional.of(this);
				}
			} else if (scrollDeltaY > 0) {
				if (this.paged.previousPage()) {
					return Optional.of(this);
				}
			}
			return Optional.empty();
		}

		@Override
		public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
			if (input.is(keyBindings.getNextPage())) {
				this.paged.nextPage();
				return Optional.of(this);
			}

			if (input.is(keyBindings.getPreviousPage())) {
				this.paged.previousPage();
				return Optional.of(this);
			}

			if (input.is(keyBindings.getQuickMove())) {
				if (this.ghostIngredientQuickMoveManager.quickMove(screen, input)) {
					return Optional.of(this);
				}
			}

			return checkHotbarKeys(screen, input);
		}

		/**
		 * Modeled after ContainerScreen#checkHotbarKeys(int)
		 * Sets the stack in a hotbar slot to the one that's hovered over.
		 */
		private Optional<IUserInputHandler> checkHotbarKeys(Screen screen, UserInput input) {
			if (!clientConfig.isCheatToHotbarUsingHotkeysEnabled() ||
				!this.toggleState.isCheatItemsEnabled() ||
				screen instanceof RecipesGui
			) {
				return Optional.empty();
			}

			final double mouseX = input.getMouseX();
			final double mouseY = input.getMouseY();
			if (!this.mouseOverable.isMouseOver(mouseX, mouseY)) {
				return Optional.empty();
			}

			Minecraft minecraft = Minecraft.getInstance();
			Options gameSettings = minecraft.options;
			int hotbarSlot = getHotbarSlotForInput(input, gameSettings);
			if (hotbarSlot < 0) {
				return Optional.empty();
			}

			return this.focusSource.getIngredientUnderMouse(mouseX, mouseY)
				.<IUserInputHandler>flatMap(clickedIngredient -> {
					ItemStack cheatItemStack = clickedIngredient.getCheatItemStack(ingredientManager);
					if (!cheatItemStack.isEmpty()) {
						commandUtil.setHotbarStack(cheatItemStack, hotbarSlot);
						return Stream.of(new SameElementInputHandler(this, clickedIngredient::isMouseOver));
					}
					return Stream.empty();
				})
				.findFirst();
		}

		private static int getHotbarSlotForInput(UserInput input, Options gameSettings) {
			for (int hotbarSlot = 0; hotbarSlot < gameSettings.keyHotbarSlots.length; ++hotbarSlot) {
				KeyMapping keyHotbarSlot = gameSettings.keyHotbarSlots[hotbarSlot];
				if (input.is(keyHotbarSlot)) {
					return hotbarSlot;
				}
			}
			return -1;
		}
	}
}
