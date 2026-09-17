package mezz.jei.gui.overlay.bookmarks;

import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IBookmarkOverlay;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.transfer.RecipeTransferService;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.bookmarks.IBookmark;
import mezz.jei.gui.elements.IconButton;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IDragHandler;
import mezz.jei.gui.input.IDraggableIngredientInternal;
import mezz.jei.gui.input.IPaged;
import mezz.jei.gui.input.IRecipeFocusSource;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.MouseUtil;
import mezz.jei.gui.input.handlers.CombinedDragHandler;
import mezz.jei.gui.input.handlers.CombinedInputHandler;
import mezz.jei.gui.input.handlers.NullDragHandler;
import mezz.jei.gui.input.handlers.NullInputHandler;
import mezz.jei.gui.input.handlers.ProxyDragHandler;
import mezz.jei.gui.input.handlers.ProxyInputHandler;
import mezz.jei.gui.overlay.ingredients.IngredientGridWithNavigation;
import mezz.jei.gui.overlay.ingredients.IIngredientGridSource;
import mezz.jei.gui.overlay.ingredients.IngredientListSlot;
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
import java.util.function.Consumer;
import java.util.stream.Stream;

public class BookmarkOverlay implements IRecipeFocusSource, IBookmarkOverlay {
	private static final int BORDER_MARGIN = 6;
	private static final int INNER_PADDING = 2;
	private static final int BUTTON_SIZE = 20;
	private static final int LOOKUP_HISTORY_BOTTOM_PADDING = BORDER_MARGIN;

	// input
	private final BookmarkDragManager bookmarkDragManager;

	// areas
	private final GuiPropertiesCache<Screen> guiPropertiesCache;

	// display elements
	private final IngredientGridWithNavigation contents;
	private final LookupHistoryOverlay lookupHistoryOverlay;
	private final IconButton bookmarkButton;
	private final IconButton historyButton;

	// data
	private final BookmarkList bookmarkList;
	private final IClientToggleState toggleState;
	private final IClientConfig clientConfig;
	private final BookmarkPreviewTooltipController previewTooltipController;
	private boolean screenPropertiesDirty;

	public BookmarkOverlay(
		BookmarkList bookmarkList,
		RecipeTransferService recipeTransferService,
		IngredientGridWithNavigation contents,
		LookupHistoryOverlay lookupHistoryOverlay,
		IClientToggleState toggleState,
		IClientConfig clientConfig,
		IIngredientGridConfig bookmarkListConfig,
		IScreenHelper screenHelper,
		IInternalKeyMappings keyBindings
	) {
		this.bookmarkList = bookmarkList;
		this.toggleState = toggleState;
		this.clientConfig = clientConfig;
		this.bookmarkButton = new IconButton(new BookmarkButtonController(this, bookmarkList, toggleState, keyBindings));
		this.historyButton = new IconButton(new LookupHistoryButtonController(clientConfig));
		this.contents = contents;
		this.lookupHistoryOverlay = lookupHistoryOverlay;
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

		clientConfig.lookupHistoryEnabled().addListener(v -> markScreenPropertiesDirty());
		clientConfig.maxLookupHistoryRows().addListener(v -> markScreenPropertiesDirty());
		clientConfig.lookupHistoryDisplaySide().addListener(v -> markScreenPropertiesDirty());
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

	private void markScreenPropertiesDirty() {
		this.screenPropertiesDirty = true;
	}

	private void addGridConfigListeners(IIngredientGridConfig gridConfig) {
		gridConfig.maxColumns().addListener(v -> markScreenPropertiesDirty());
		gridConfig.maxRows().addListener(v -> markScreenPropertiesDirty());
		gridConfig.drawBackground().addListener(v -> markScreenPropertiesDirty());
		gridConfig.layoutMode().addListener(v -> markScreenPropertiesDirty());
		gridConfig.navigationMode().addListener(v -> markScreenPropertiesDirty());
		gridConfig.horizontalAlignment().addListener(v -> markScreenPropertiesDirty());
		gridConfig.verticalAlignment().addListener(v -> markScreenPropertiesDirty());
		gridConfig.navigationVisibility().addListener(v -> markScreenPropertiesDirty());
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
		ImmutableRect2i displayArea = getDisplayArea(guiProperties);
		ImmutablePoint2i mouseExclusionArea = this.guiPropertiesCache.getMouseExclusionArea();
		ImmutableRect2i availableContentsArea = displayArea.cropBottom(BUTTON_SIZE + INNER_PADDING);
		if (clientConfig.lookupHistoryEnabled().getValue() && lookupHistoryOverlay.isDisplayedOnThisSide()) {
			int lookupHistoryDisplayHeight = lookupHistoryOverlay.getDisplayHeight();
			if (lookupHistoryDisplayHeight > 0) {
				ImmutableRect2i historyArea = displayArea
					.insetBy(BORDER_MARGIN)
					.cropBottom(BUTTON_SIZE + LOOKUP_HISTORY_BOTTOM_PADDING)
					.keepBottom(lookupHistoryDisplayHeight);
				availableContentsArea = cropBottomTo(availableContentsArea, historyArea.y());
				this.lookupHistoryOverlay.updateBounds(historyArea, guiExclusionAreas, mouseExclusionArea);
				this.lookupHistoryOverlay.updateLayout();
			}
		}
		IElement<?> pageAnchorElement = this.contents.getPageAnchorElement();
		this.contents.updateBounds(availableContentsArea, guiExclusionAreas, mouseExclusionArea);
		this.contents.updateLayoutKeepingPageAnchorVisible(pageAnchorElement);

		if (contents.hasRoom()) {
			ImmutableRect2i contentsArea = this.contents.getBackgroundArea();
			ImmutableRect2i bookmarkButtonArea = displayArea
				.insetBy(BORDER_MARGIN)
				.matchWidthAndX(contentsArea)
				.keepBottom(BUTTON_SIZE)
				.keepLeft(BUTTON_SIZE);
			this.bookmarkButton.updateBounds(bookmarkButtonArea);
			ImmutableRect2i historyButtonArea = bookmarkButtonArea.moveRight(2 + BUTTON_SIZE);
			this.historyButton.updateBounds(historyButtonArea);
		} else {
			ImmutableRect2i bookmarkButtonArea = displayArea
				.insetBy(BORDER_MARGIN)
				.keepBottom(BUTTON_SIZE)
				.keepLeft(BUTTON_SIZE);
			this.bookmarkButton.updateBounds(bookmarkButtonArea);
			ImmutableRect2i historyButtonArea = bookmarkButtonArea.moveRight(2 + BUTTON_SIZE);
			this.historyButton.updateBounds(historyButtonArea);
		}
	}

	private static ImmutableRect2i getDisplayArea(IGuiProperties guiProperties) {
		int width = guiProperties.guiLeft();
		if (width <= 0) {
			width = 0;
		}
		int screenHeight = guiProperties.screenHeight();
		return new ImmutableRect2i(0, 0, width, screenHeight);
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
		if (isListDisplayed()) {
			this.contents.drawBackground(guiGraphics);
		}
		if (guiPropertiesCache.hasValidScreen() && toggleState.isOverlayEnabled()) {
			this.lookupHistoryOverlay.drawBackground(guiGraphics);
		}
	}

	public void drawForeground(Minecraft minecraft, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		updateScreenPropertiesIfDirty();
		if (isListDisplayed()) {
			this.bookmarkDragManager.updateDrag(mouseX, mouseY);
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

		final IUserInputHandler buttonInputHandler = new CombinedInputHandler(
			"BookmarkOverlayButton",
			bookmarkButtonInputHandler,
			historyButtonInputHandler
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

	public List<IBookmarkDragTarget> createBookmarkDragTargets(IBookmark draggedBookmark) {
		updateScreenPropertiesIfDirty();
		List<DragTargetSpec> slotSpecs = buildSlotDragTargetSpecs(this.contents.getAllSlots());
		List<IElement<?>> elements = this.bookmarkList.getElements();
		IBookmark firstPageBookmark = getFirstPageBookmark(slotSpecs, draggedBookmark, elements);
		IBookmark lastPageBookmark = getLastPageBookmark(slotSpecs, draggedBookmark, elements);

		List<IBookmarkDragTarget> bookmarkDragTargets = new ArrayList<>(slotSpecs.size());
		for (DragTargetSpec spec : slotSpecs) {
			bookmarkDragTargets.add(new DragTarget(spec.area(), spec.anchor(), bookmarkList, spec.offset()));
		}

		IPaged pageDelegate = this.contents.getPageDelegate();
		if (pageDelegate.getPageCount() > 1) {
			if (pageDelegate.getPageNumber() >= pageDelegate.getPageCount() - 1) {
				// if a bookmark is dropped on the next button while on the last page, put it at the start of the first page
				bookmarkDragTargets.add(new ActionDragTarget(
					this.contents.getNextPageButtonArea(),
					bookmarkList::moveBookmarkToFront,
					pageDelegate::nextPage
				));
			} else {
				// if a bookmark is dropped on the next button, put it on the next page
				bookmarkDragTargets.add(new ActionDragTarget(
					this.contents.getNextPageButtonArea(),
					bookmark -> bookmarkList.moveBookmark(lastPageBookmark, bookmark, 1),
					pageDelegate::nextPage
				));
			}

			// if a bookmark is dropped on the back button, put it on the previous page
			bookmarkDragTargets.add(new ActionDragTarget(
				this.contents.getBackButtonArea(),
				bookmark -> bookmarkList.moveBookmark(firstPageBookmark, bookmark, -1),
				pageDelegate::previousPage
			));
		}

		// if a bookmark is dropped somewhere else in the contents area, put it at the end of the current page
		bookmarkDragTargets.add(new DragTarget(this.contents.getSlotBackgroundArea(), lastPageBookmark, bookmarkList, 0));

		return bookmarkDragTargets;
	}

	record DragTargetSpec(ImmutableRect2i area, IBookmark anchor, int offset) {
	}

	static List<DragTargetSpec> buildSlotDragTargetSpecs(List<IngredientListSlot> slots) {
		List<DragTargetSpec> specs = new ArrayList<>();
		List<ImmutableRect2i> emptySlotAreas = new ArrayList<>();
		for (IngredientListSlot slot : slots) {
			Optional<IBookmark> bookmark = slot.getOptionalElement()
				.flatMap(IElement::getBookmark);
			if (bookmark.isPresent()) {
				for (ImmutableRect2i area : emptySlotAreas) {
					specs.add(new DragTargetSpec(area, bookmark.get(), -1));
				}
				emptySlotAreas.clear();
				specs.add(new DragTargetSpec(slot.getArea(), bookmark.get(), 0));
			} else {
				emptySlotAreas.add(slot.getArea());
			}
		}
		return specs;
	}

	static IBookmark getFirstPageBookmark(List<DragTargetSpec> slotSpecs, IBookmark draggedBookmark, List<IElement<?>> elements) {
		if (slotSpecs.isEmpty()) {
			return draggedBookmark;
		}
		IBookmark firstVisible = slotSpecs.getFirst().anchor();
		if (elements.indexOf(draggedBookmark.getElement()) < elements.indexOf(firstVisible.getElement())) {
			return draggedBookmark;
		}
		return firstVisible;
	}

	static IBookmark getLastPageBookmark(List<DragTargetSpec> slotSpecs, IBookmark draggedBookmark, List<IElement<?>> elements) {
		if (slotSpecs.isEmpty()) {
			return draggedBookmark;
		}
		IBookmark lastVisible = slotSpecs.getLast().anchor();
		if (elements.indexOf(lastVisible.getElement()) < elements.indexOf(draggedBookmark.getElement())) {
			return draggedBookmark;
		}
		return lastVisible;
	}

	public void setPageAnchorElement(IBookmark bookmark) {
		this.contents.setPageAnchorElement(bookmark.getElement());
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

	public static class ActionDragTarget implements IBookmarkDragTarget {
		private final ImmutableRect2i area;
		private final Consumer<IBookmark> move;
		private final Runnable action;

		public ActionDragTarget(ImmutableRect2i area, Consumer<IBookmark> move, Runnable action) {
			this.area = area;
			this.move = move;
			this.action = action;
		}

		@Override
		public ImmutableRect2i getArea() {
			return area;
		}

		@Override
		public void accept(IBookmark bookmark) {
			move.accept(bookmark);
			action.run();
		}
	}

	public static class DragTarget implements IBookmarkDragTarget {
		private final ImmutableRect2i area;
		private final IBookmark bookmark;
		private final BookmarkList bookmarkList;
		private final int offset;

		public DragTarget(ImmutableRect2i area, IBookmark bookmark, BookmarkList bookmarkList, int offset) {
			this.area = area;
			this.bookmark = bookmark;
			this.bookmarkList = bookmarkList;
			this.offset = offset;
		}

		@Override
		public ImmutableRect2i getArea() {
			return area;
		}

		@Override
		public void accept(IBookmark bookmark) {
			bookmarkList.moveBookmark(this.bookmark, bookmark, offset);
		}
	}
}
