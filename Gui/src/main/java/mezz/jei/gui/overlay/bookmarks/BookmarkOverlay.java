package mezz.jei.gui.overlay.bookmarks;

import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IBookmarkOverlay;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.gui.JeiGuiColors;
import mezz.jei.common.gui.JeiGuiColors.GuiColor;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.transfer.RecipeTransferService;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.bookmarks.IBookmark;
import mezz.jei.gui.elements.IconButton;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IDragHandler;
import mezz.jei.gui.input.IDraggableIngredientInternal;
import mezz.jei.gui.input.IPaged;
import mezz.jei.gui.input.IRecipeFocusSource;
import mezz.jei.common.input.IUserInputHandler;
import mezz.jei.common.input.MouseUtil;
import mezz.jei.gui.input.handlers.CombinedDragHandler;
import mezz.jei.common.input.handlers.CombinedInputHandler;
import mezz.jei.gui.input.handlers.NullDragHandler;
import mezz.jei.gui.input.handlers.NullInputHandler;
import mezz.jei.gui.input.handlers.ProxyDragHandler;
import mezz.jei.gui.input.handlers.ProxyInputHandler;
import mezz.jei.gui.overlay.ingredients.IngredientGridWithNavigation;
import mezz.jei.gui.overlay.ingredients.IngredientGridBackgroundRenderer;
import mezz.jei.gui.overlay.ingredients.IIngredientGridSource;
import mezz.jei.gui.overlay.ingredients.IngredientGridLayout;
import mezz.jei.gui.overlay.IScreenPropertiesUpdater;
import mezz.jei.gui.overlay.GuiPropertiesCache;
import mezz.jei.gui.overlay.bookmarks.history.LookupHistoryButtonController;
import mezz.jei.gui.overlay.bookmarks.history.LookupHistoryOverlay;
import mezz.jei.gui.overlay.elements.IElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

public class BookmarkOverlay implements IRecipeFocusSource, IBookmarkOverlay {
	private static final int BORDER_MARGIN = 6;
	private static final int INNER_PADDING = 2;
	private static final int BUTTON_SIZE = 20;
	private static final int LOOKUP_HISTORY_BOTTOM_PADDING = BORDER_MARGIN;
	private static final int LOOKUP_HISTORY_PADDING_EXTRA = LOOKUP_HISTORY_BOTTOM_PADDING - INNER_PADDING;
	private static final int BUTTON_GAP = 2;
	private static final int BUTTON_ROW_WIDTH = BUTTON_SIZE * 2 + BUTTON_GAP;

	// input
	private final BookmarkDragManager bookmarkDragManager;

	// areas
	private final GuiPropertiesCache<Screen> guiPropertiesCache;

	// display elements
	private final IngredientGridWithNavigation contents;
	private final LookupHistoryOverlay lookupHistoryOverlay;
	private final IngredientGridBackgroundRenderer backgroundRenderer;
	private final IconButton bookmarkButton;
	private final IconButton historyButton;

	// data
	private final BookmarkList bookmarkList;
	private final IClientToggleState toggleState;
	private final IClientConfig clientConfig;
	private final IIngredientGridConfig bookmarkListConfig;
	private final BookmarkPreviewTooltipController previewTooltipController;
	private boolean screenPropertiesDirty;

	public BookmarkOverlay(
		BookmarkList bookmarkList,
		RecipeTransferService recipeTransferService,
		IngredientGridWithNavigation contents,
		LookupHistoryOverlay lookupHistoryOverlay,
		IngredientGridBackgroundRenderer backgroundRenderer,
		IClientToggleState toggleState,
		IClientConfig clientConfig,
		IIngredientGridConfig bookmarkListConfig,
		IScreenHelper screenHelper,
		IInternalKeyMappings keyBindings
	) {
		this.bookmarkList = bookmarkList;
		this.toggleState = toggleState;
		this.clientConfig = clientConfig;
		this.bookmarkListConfig = bookmarkListConfig;
		this.bookmarkButton = new IconButton(new BookmarkButtonController(this, bookmarkList, toggleState, keyBindings));
		this.historyButton = new IconButton(new LookupHistoryButtonController(clientConfig));
		this.contents = contents;
		this.lookupHistoryOverlay = lookupHistoryOverlay;
		this.backgroundRenderer = backgroundRenderer;
		this.guiPropertiesCache = new GuiPropertiesCache<>(
			screen -> screenHelper.getGuiProperties(screen)
				.orElse(null)
		);
		this.bookmarkDragManager = new BookmarkDragManager(this);
		this.previewTooltipController = new BookmarkPreviewTooltipController(this, recipeTransferService);
		bookmarkList.addSourceListChangedListener(() -> {
			toggleState.setBookmarkEnabled(!bookmarkList.isEmpty());
			markScreenPropertiesDirty();
		});
		lookupHistoryOverlay.getLookupHistory().addSourceListChangedListener(this::markScreenPropertiesDirty);

		Internal.registerRuntimeListenerRemoval(clientConfig.lookupHistoryEnabled().addListener(v -> markScreenPropertiesDirty()));
		Internal.registerRuntimeListenerRemoval(clientConfig.maxLookupHistoryRows().addListener(v -> markScreenPropertiesDirty()));
		Internal.registerRuntimeListenerRemoval(clientConfig.maxLookupHistoryColumns().addListener(v -> markScreenPropertiesDirty()));
		Internal.registerRuntimeListenerRemoval(clientConfig.lookupHistoryDisplaySide().addListener(v -> markScreenPropertiesDirty()));
		addGridConfigListeners(bookmarkListConfig);
	}

	public boolean isListDisplayed() {
		updateScreenPropertiesIfDirty();
		return toggleState.isBookmarkOverlayEnabled() &&
			guiPropertiesCache.hasValidScreen() &&
			contents.hasRoom() &&
			!bookmarkList.isEmpty();
	}

	public boolean hasRoom() {
		updateScreenPropertiesIfDirty();
		return contents.hasRoom();
	}

	public IUserInputHandler getResizeInputHandler() {
		IUserInputHandler historyResize = new ProxyInputHandler(() -> {
			updateScreenPropertiesIfDirty();
			if (guiPropertiesCache.hasValidScreen() && toggleState.isOverlayEnabled() && lookupHistoryOverlay.isListDisplayed()) {
				return lookupHistoryOverlay.getResizeInputHandler();
			}
			return NullInputHandler.INSTANCE;
		});
		IUserInputHandler contentsResize = new ProxyInputHandler(() -> {
			if (isListDisplayed()) {
				return contents.getResizeInputHandler();
			}
			return NullInputHandler.INSTANCE;
		});
		return new CombinedInputHandler("BookmarkResize", historyResize, contentsResize);
	}

	private void markScreenPropertiesDirty() {
		this.screenPropertiesDirty = true;
	}

	private void addGridConfigListeners(IIngredientGridConfig gridConfig) {
		Internal.registerRuntimeListenerRemoval(gridConfig.maxColumns().addListener(v -> markScreenPropertiesDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.maxRows().addListener(v -> markScreenPropertiesDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.drawBackground().addListener(v -> markScreenPropertiesDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.layoutMode().addListener(v -> markScreenPropertiesDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.navigationMode().addListener(v -> markScreenPropertiesDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.horizontalAlignment().addListener(v -> markScreenPropertiesDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.verticalAlignment().addListener(v -> markScreenPropertiesDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.navigationVisibility().addListener(v -> markScreenPropertiesDirty()));
	}

	private void updateScreenPropertiesIfDirty() {
		if (this.screenPropertiesDirty) {
			this.screenPropertiesDirty = false;
			Minecraft minecraft = Minecraft.getInstance();
			this.getScreenPropertiesUpdater()
				.updateScreen(minecraft.gui.screen())
				.forceUpdate();
		}
	}

	public IScreenPropertiesUpdater getScreenPropertiesUpdater() {
		return this.guiPropertiesCache.createUpdater(this::onGuiPropertiesChanged);
	}

	private void onGuiPropertiesChanged() {
		IGuiProperties guiProperties = this.guiPropertiesCache.getGuiProperties();
		if (guiProperties == null) {
			this.contents.close();
			this.lookupHistoryOverlay.close();
			return;
		}
		updateBounds(guiProperties, this.guiPropertiesCache.getGuiExclusionAreas());
	}

	private void updateBounds(IGuiProperties guiProperties, Set<ImmutableRect2i> guiExclusionAreas) {
		ImmutableRect2i displayArea = BookmarkOverlayLayout.calculateDisplayArea(guiProperties, guiExclusionAreas);
		ImmutablePoint2i mouseExclusionArea = this.guiPropertiesCache.getMouseExclusionArea();
		ImmutableRect2i availableContentsArea = displayArea.cropBottom(BUTTON_SIZE + INNER_PADDING);
		Optional<ImmutableRect2i> historyArea = Optional.empty();
		if (clientConfig.lookupHistoryEnabled().get() && lookupHistoryOverlay.isDisplayedOnThisSide()) {
			int lookupHistoryDisplayHeight = lookupHistoryOverlay.getDisplayHeight();
			if (lookupHistoryDisplayHeight > 0) {
				ImmutableRect2i area = displayArea
					.cropBottom(BUTTON_SIZE + LOOKUP_HISTORY_BOTTOM_PADDING)
					.keepBottom(lookupHistoryDisplayHeight);
				historyArea = Optional.of(area);
				availableContentsArea = cropBottomTo(
					availableContentsArea,
					area.y() - LOOKUP_HISTORY_PADDING_EXTRA
				);
			}
		}
		IElement<?> pageAnchorElement = this.contents.getPageAnchorElement();
		this.contents.updateBounds(availableContentsArea, guiExclusionAreas, mouseExclusionArea);
		this.contents.updateLayoutKeepingPageAnchorVisible(pageAnchorElement);

		historyArea.ifPresent(area -> {
			this.lookupHistoryOverlay.updateBounds(area, guiExclusionAreas, mouseExclusionArea);
			this.lookupHistoryOverlay.updateLayout();
			ImmutableRect2i resizeArea = displayArea.cropBottom(BUTTON_SIZE + LOOKUP_HISTORY_BOTTOM_PADDING);
			this.lookupHistoryOverlay.setResizeBounds(resizeArea, VerticalAlignment.BOTTOM);
		});

		ImmutableRect2i insetDisplayArea = displayArea.insetBy(BORDER_MARGIN);
		if (insetDisplayArea.getWidth() < BUTTON_ROW_WIDTH || insetDisplayArea.getHeight() < BUTTON_SIZE) {
			this.bookmarkButton.updateBounds(ImmutableRect2i.EMPTY);
			this.historyButton.updateBounds(ImmutableRect2i.EMPTY);
		} else if (contents.hasRoom() && this.contents.getBackgroundArea().getWidth() >= BUTTON_ROW_WIDTH) {
			ImmutableRect2i contentsArea = this.contents.getBackgroundArea();
			ImmutableRect2i bookmarkButtonArea = insetDisplayArea
				.matchWidthAndX(contentsArea)
				.keepBottom(BUTTON_SIZE)
				.keepLeft(BUTTON_SIZE);
			this.bookmarkButton.updateBounds(bookmarkButtonArea);
			ImmutableRect2i historyButtonArea = bookmarkButtonArea.moveRight(BUTTON_GAP + BUTTON_SIZE);
			this.historyButton.updateBounds(historyButtonArea);
		} else {
			ImmutableRect2i bookmarkButtonArea = insetDisplayArea
				.keepBottom(BUTTON_SIZE)
				.keepLeft(BUTTON_SIZE);
			this.bookmarkButton.updateBounds(bookmarkButtonArea);
			ImmutableRect2i historyButtonArea = bookmarkButtonArea.moveRight(BUTTON_GAP + BUTTON_SIZE);
			this.historyButton.updateBounds(historyButtonArea);
		}
	}

	private static ImmutableRect2i cropBottomTo(ImmutableRect2i area, int bottomY) {
		int cropAmount = getBottom(area) - bottomY;
		if (cropAmount <= 0) {
			return area;
		}
		return area.cropBottom(cropAmount);
	}

	private static int getBottom(ImmutableRect2i area) {
		return area.y() + area.height();
	}

	public void drawScreen(Minecraft minecraft, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		updateScreenPropertiesIfDirty();
		drawBackground(guiGraphics);
		drawForeground(minecraft, guiGraphics, mouseX, mouseY, partialTicks);
	}

	public void drawBackground(GuiGraphicsExtractor guiGraphics) {
		updateScreenPropertiesIfDirty();
		boolean contentsDisplayed = isListDisplayed();
		List<IngredientGridBackgroundRenderer.Panel> backgroundPanels = new ArrayList<>(2);
		if (contentsDisplayed && this.contents.isBackgroundEnabled()) {
			backgroundPanels.add(new IngredientGridBackgroundRenderer.Panel(
				this.contents.getBackgroundArea(),
				this.contents.getSlotBackgroundArea()
			));
		}
		boolean lookupHistoryDisplayed = guiPropertiesCache.hasValidScreen() &&
			toggleState.isOverlayEnabled() &&
			this.lookupHistoryOverlay.isListDisplayed();
		if (lookupHistoryDisplayed && this.lookupHistoryOverlay.isBackgroundEnabled()) {
			backgroundPanels.add(new IngredientGridBackgroundRenderer.Panel(
				this.lookupHistoryOverlay.getBackgroundArea(),
				this.lookupHistoryOverlay.getSlotBackgroundArea()
			));
		}
		this.backgroundRenderer.draw(
			guiGraphics,
			backgroundPanels,
			this.guiPropertiesCache.getGuiExclusionAreas()
		);
	}

	public void drawForeground(Minecraft minecraft, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		updateScreenPropertiesIfDirty();
		if (isListDisplayed()) {
			this.bookmarkDragManager.updateDrag(mouseX, mouseY);
			drawPageFlipEdgeHighlights(guiGraphics, mouseX, mouseY);
			this.contents.drawForeground(minecraft, guiGraphics, mouseX, mouseY, partialTicks);
		}
		if (guiPropertiesCache.hasValidScreen() && toggleState.isOverlayEnabled()) {
			this.lookupHistoryOverlay.draw(minecraft, guiGraphics, mouseX, mouseY, partialTicks);
		}
		if (this.guiPropertiesCache.hasValidScreen()) {
			this.bookmarkButton.draw(guiGraphics, mouseX, mouseY, partialTicks);
			this.historyButton.draw(guiGraphics, mouseX, mouseY, partialTicks);
		}
	}

	private void drawPageFlipEdgeHighlights(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		if (!this.bookmarkDragManager.isDragging() || !canFlipPage()) {
			return;
		}
		PageFlipHover.Direction hoveredDirection = getHoveredPageEdge(mouseX, mouseY);
		drawPageFlipEdgeHighlight(guiGraphics, getNextPageEdgeArea(), hoveredDirection == PageFlipHover.Direction.NEXT);
		drawPageFlipEdgeHighlight(guiGraphics, getBackPageEdgeArea(), hoveredDirection == PageFlipHover.Direction.PREVIOUS);
	}

	private static void drawPageFlipEdgeHighlight(GuiGraphicsExtractor guiGraphics, ImmutableRect2i area, boolean hovered) {
		GuiColor color;
		if (hovered) {
			color = GuiColor.BOOKMARK_DRAG_PAGE_FLIP_HIGHLIGHT;
		} else {
			color = GuiColor.BOOKMARK_DRAG_PAGE_FLIP_HINT;
		}
		guiGraphics.fill(
			area.getX(),
			area.getY(),
			area.getX() + area.getWidth(),
			area.getY() + area.getHeight(),
			JeiGuiColors.getColor(color)
		);
	}

	public BookmarkPreviewTooltipController getPreviewTooltipController() {
		return previewTooltipController;
	}

	Stream<PreviewSource> getPreviewSourcesUnderMouse(double mouseX, double mouseY) {
		Stream<PreviewSource> bookmarkSources = contents.getIngredientUnderMouse(mouseX, mouseY)
			.map(ingredient -> new PreviewSource(ingredient, bookmarkList, this::isListDisplayed));
		IIngredientGridSource lookupHistory = lookupHistoryOverlay.getLookupHistory();
		Stream<PreviewSource> lookupHistorySources = lookupHistoryOverlay.getIngredientUnderMouse(mouseX, mouseY)
			.map(ingredient -> new PreviewSource(ingredient, lookupHistory, lookupHistoryOverlay::isListDisplayed));
		return Stream.concat(bookmarkSources, lookupHistorySources);
	}

	record PreviewSource(
		IClickableIngredientInternal<?> ingredient,
		IIngredientGridSource owner,
		BooleanSupplier ownerDisplayed
	) {
		boolean isPresentAndVisible() {
			IElement<?> element = ingredient.getElement();
			return ownerDisplayed.getAsBoolean() &&
				element.isVisible() &&
				owner.containsElement(element);
		}
	}

	public void drawTooltips(Minecraft minecraft, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		updateScreenPropertiesIfDirty();
		if (!this.bookmarkDragManager.drawDraggedItem(guiGraphics, mouseX, mouseY)) {
			if (isListDisplayed() && !previewTooltipController.isVisible()) {
				this.contents.drawTooltips(minecraft, guiGraphics, mouseX, mouseY);
			}
			if (guiPropertiesCache.hasValidScreen() && toggleState.isOverlayEnabled()) {
				this.lookupHistoryOverlay.drawTooltips(minecraft, guiGraphics, mouseX, mouseY);
			}
		}
		if (this.guiPropertiesCache.hasValidScreen()) {
			bookmarkButton.drawTooltips(guiGraphics, mouseX, mouseY);
			historyButton.drawTooltips(guiGraphics, mouseX, mouseY);
		}
	}

	public void tick() {
		this.bookmarkButton.tick();
		this.historyButton.tick();
		if (isListDisplayed()) {
			this.contents.tick();
		}
		if (guiPropertiesCache.hasValidScreen() && toggleState.isOverlayEnabled()) {
			this.lookupHistoryOverlay.tick();
		}
	}

	@Override
	public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		updateScreenPropertiesIfDirty();
		if (isListDisplayed()) {
			return Stream.concat(this.contents.getIngredientUnderMouse(mouseX, mouseY), this.lookupHistoryOverlay.getIngredientUnderMouse(mouseX, mouseY));
		}
		if (this.lookupHistoryOverlay.isListDisplayed()) {
			return this.lookupHistoryOverlay.getIngredientUnderMouse(mouseX, mouseY);
		}
		return Stream.empty();
	}

	@Override
	public Stream<IDraggableIngredientInternal<?>> getDraggableIngredientUnderMouse(double mouseX, double mouseY) {
		updateScreenPropertiesIfDirty();
		if (isListDisplayed()) {
			return Stream.concat(this.contents.getDraggableIngredientUnderMouse(mouseX, mouseY), this.lookupHistoryOverlay.getDraggableIngredientUnderMouse(mouseX, mouseY));
		}
		if (this.lookupHistoryOverlay.isListDisplayed()) {
			return this.lookupHistoryOverlay.getDraggableIngredientUnderMouse(mouseX, mouseY);
		}
		return Stream.empty();
	}

	@Override
	public Optional<ITypedIngredient<?>> getIngredientUnderMouse() {
		double mouseX = MouseUtil.getX();
		double mouseY = MouseUtil.getY();
		return getIngredientUnderMouse(mouseX, mouseY)
			.<ITypedIngredient<?>>map(IClickableIngredientInternal::getTypedIngredient)
			.findFirst();
	}

	@Nullable
	@Override
	public <T> T getIngredientUnderMouse(IIngredientType<T> ingredientType) {
		double mouseX = MouseUtil.getX();
		double mouseY = MouseUtil.getY();
		return getIngredientUnderMouse(mouseX, mouseY)
			.map(IClickableIngredientInternal::getTypedIngredient)
			.map(i -> i.getIngredient(ingredientType))
			.flatMap(Optional::stream)
			.findFirst()
			.orElse(null);
	}

	public IUserInputHandler createInputHandler() {
		final IUserInputHandler bookmarkButtonInputHandler = this.bookmarkButton.createInputHandler();
		final IUserInputHandler historyButtonInputHandler = this.historyButton.createInputHandler();
		final IUserInputHandler lookupHistoryInputHandler = this.lookupHistoryOverlay.createInputHandler();
		final IUserInputHandler displayedLookupHistoryInputHandler = new ProxyInputHandler(() -> {
			if (guiPropertiesCache.hasValidScreen() &&
				toggleState.isOverlayEnabled() &&
				this.lookupHistoryOverlay.isListDisplayed()
			) {
				return lookupHistoryInputHandler;
			}
			return NullInputHandler.INSTANCE;
		});

		final IUserInputHandler buttonInputHandler = new CombinedInputHandler(
			"BookmarkOverlayControls",
			bookmarkButtonInputHandler,
			historyButtonInputHandler,
			displayedLookupHistoryInputHandler
		);

		final IUserInputHandler displayedInputHandler = new CombinedInputHandler(
			"BookmarkOverlay",
			this.contents.createInputHandler(),
			buttonInputHandler
		);

		return new ProxyInputHandler(() -> {
			if (isListDisplayed()) {
				return displayedInputHandler;
			}
			return buttonInputHandler;
		});
	}

	public IUserInputHandler createDeleteItemInputHandler() {
		final IUserInputHandler deleteItemInputHandler = this.contents.createDeleteItemInputHandler();

		return new ProxyInputHandler(() -> {
			if (isListDisplayed()) {
				return deleteItemInputHandler;
			}
			return NullInputHandler.INSTANCE;
		});
	}

	public IDragHandler createDragHandler() {
		final IDragHandler lookupHistoryDragHandler = this.lookupHistoryOverlay.createDragHandler();
		final IDragHandler combinedDragHandlers = new CombinedDragHandler(
			this.contents.createDragHandler(),
			lookupHistoryDragHandler,
			this.bookmarkDragManager.createDragHandler()
		);

		return new ProxyDragHandler(() -> {
			if (isListDisplayed()) {
				return combinedDragHandlers;
			}
			if (lookupHistoryOverlay.isListDisplayed()) {
				return lookupHistoryDragHandler;
			}
			return NullDragHandler.INSTANCE;
		});
	}

	public void drawOnForeground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		updateScreenPropertiesIfDirty();
		if (isListDisplayed()) {
			this.contents.drawOnForeground(guiGraphics, mouseX, mouseY);
		}
		this.lookupHistoryOverlay.drawOnForeground(guiGraphics, mouseX, mouseY);
	}

	public List<BookmarkDragTarget> createBookmarkDragTargets(IBookmark draggedBookmark) {
		updateScreenPropertiesIfDirty();
		List<IElement<?>> elements = this.bookmarkList.getElements();
		List<BookmarkDragTarget> targets = BookmarkDragTarget.createSlotTargets(this.contents.getAllSlots(), elements, draggedBookmark);
		List<IElement<?>> pageElements = this.contents.getPageElements();
		if (pageElements.isEmpty()) {
			return targets;
		}

		// Use the current page's range, including hidden bookmarks, even after navigating during a drag.
		int firstIndex = elements.indexOf(pageElements.getFirst());
		int lastIndex = elements.indexOf(pageElements.getLast());
		if (canFlipPage()) {
			targets.add(new BookmarkDragTarget(this.contents.getNextPageButtonArea(), (lastIndex + 1) % elements.size()));
			targets.add(new BookmarkDragTarget(this.contents.getBackButtonArea(), Math.floorMod(firstIndex - 1, elements.size())));
		}

		// Trailing empty slots and background padding place the bookmark at the end of this page.
		targets.add(new BookmarkDragTarget(this.contents.getSlotBackgroundArea(), lastIndex));
		return targets;
	}

	public void moveBookmark(IBookmark bookmark, int index) {
		this.bookmarkList.moveBookmark(bookmark, index);
		// Keep the dropped bookmark visible when its visibility and the cursor's slot are restored.
		this.contents.setPageAnchorElement(bookmark.getElement());
	}

	public IPaged getPageDelegate() {
		return this.contents.getPageDelegate();
	}

	public void setPageButtonsForcePressed(boolean nextButton, boolean backButton) {
		this.contents.setPageButtonsForcePressed(nextButton, backButton);
	}

	private ImmutableRect2i getNextPageEdgeArea() {
		return this.contents.getSlotBackgroundArea()
			.keepRight(IngredientGridLayout.INGREDIENT_WIDTH / 2);
	}

	private ImmutableRect2i getBackPageEdgeArea() {
		return this.contents.getSlotBackgroundArea()
			.keepLeft(IngredientGridLayout.INGREDIENT_WIDTH / 2);
	}

	PageFlipHover.@Nullable Direction getHoveredPageEdge(double mouseX, double mouseY) {
		if (!canFlipPage()) {
			return null;
		}
		if (MathUtil.contains(getNextPageEdgeArea(), mouseX, mouseY)) {
			return PageFlipHover.Direction.NEXT;
		}
		if (MathUtil.contains(getBackPageEdgeArea(), mouseX, mouseY)) {
			return PageFlipHover.Direction.PREVIOUS;
		}
		return null;
	}

	private boolean canFlipPage() {
		return !this.bookmarkListConfig.navigationMode().get().usesScrollbar() &&
			getPageDelegate().getPageCount() > 1;
	}

	void scrollDuringDrag(BookmarkDragScroll dragScroll, double mouseX, double mouseY) {
		double pixels = dragScroll.update(
			this.contents.getSlotBackgroundArea(),
			this.bookmarkListConfig.navigationMode().get().usesScrollbar(),
			this.clientConfig.smoothScrollingEnabled().get(),
			mouseX,
			mouseY
		);
		if (pixels != 0) {
			this.contents.scrollByPixels(pixels);
		}
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return this.contents.isMouseOver(mouseX, mouseY);
	}

	public boolean isBookmarkElementUnderMouse(IElement<?> element, double mouseX, double mouseY) {
		return isListDisplayed() &&
			element.isVisible() &&
			bookmarkList.containsElement(element) &&
			contents.getIngredientUnderMouse(mouseX, mouseY)
				.anyMatch(ingredient -> ingredient.getElement() == element);
	}
}
