package mezz.jei.gui.overlay.bookmarks;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IBookmarkOverlay;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.config.HistoryDisplaySide;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.config.IWorldConfig;
import mezz.jei.common.gui.JeiGuiColors;
import mezz.jei.common.gui.JeiGuiColors.GuiColor;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.IUserInputHandler;
import mezz.jei.common.input.MouseUtil;
import mezz.jei.common.input.handlers.CombinedInputHandler;
import mezz.jei.common.transfer.RecipeTransferService;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.bookmarks.IBookmark;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IDragHandler;
import mezz.jei.gui.input.IDraggableIngredientInternal;
import mezz.jei.gui.input.IPaged;
import mezz.jei.gui.input.IRecipeFocusSource;
import mezz.jei.gui.input.handlers.CombinedDragHandler;
import mezz.jei.gui.input.handlers.NullDragHandler;
import mezz.jei.gui.input.handlers.NullInputHandler;
import mezz.jei.gui.input.handlers.ProxyDragHandler;
import mezz.jei.gui.input.handlers.ProxyInputHandler;
import mezz.jei.gui.overlay.ScreenPropertiesCache;
import mezz.jei.gui.overlay.bookmarks.history.LookupHistoryButton;
import mezz.jei.gui.overlay.bookmarks.history.LookupHistoryOverlay;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.overlay.ingredients.IngredientGrid;
import mezz.jei.gui.overlay.ingredients.IngredientGridWithNavigation;
import mezz.jei.gui.overlay.ingredients.IIngredientGridSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import org.jetbrains.annotations.Nullable;

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
	private static final int LOOKUP_HISTORY_PADDING_EXTRA = LOOKUP_HISTORY_BOTTOM_PADDING - INNER_PADDING;
	private static final int BUTTON_GAP = 2;
	private static final int BUTTON_ROW_WIDTH = BUTTON_SIZE * 2 + BUTTON_GAP;

	// input
	private final BookmarkDragManager bookmarkDragManager;

	// areas
	private final ScreenPropertiesCache screenPropertiesCache;

	// display elements
	private final IngredientGridWithNavigation contents;
	private final LookupHistoryOverlay lookupHistoryOverlay;
	private final GuiIconToggleButton bookmarkButton;
	private final GuiIconToggleButton historyButton;

	// data
	private final BookmarkList bookmarkList;
	private final IWorldConfig worldConfig;
	private final IClientConfig clientConfig;
	private final IIngredientGridConfig bookmarkListConfig;
	private final BookmarkPreviewTooltipController previewTooltipController;

	// these need to be stored as strong references here because listeners are weakly stored elsewhere
	@SuppressWarnings("FieldCanBeLocal")
	private final Consumer<Boolean> lookupHistoryEnabledListener;
	@SuppressWarnings("FieldCanBeLocal")
	private final Consumer<HistoryDisplaySide> lookupHistoryViewSideListener;

	public BookmarkOverlay(
		BookmarkList bookmarkList,
		RecipeTransferService recipeTransferService,
		IngredientGridWithNavigation contents,
		LookupHistoryOverlay lookupHistoryOverlay,
		IWorldConfig worldConfig,
		IIngredientGridConfig bookmarkListConfig,
		IClientConfig clientConfig,
		IScreenHelper screenHelper,
		IInternalKeyMappings keyBindings
	) {
		this.bookmarkList = bookmarkList;
		this.worldConfig = worldConfig;
		this.clientConfig = clientConfig;
		this.bookmarkListConfig = bookmarkListConfig;
		this.bookmarkButton = BookmarkButton.create(this, bookmarkList, worldConfig, keyBindings);
		this.historyButton = LookupHistoryButton.create(clientConfig);
		this.contents = contents;
		this.lookupHistoryOverlay = lookupHistoryOverlay;
		this.screenPropertiesCache = new ScreenPropertiesCache(screenHelper);
		this.bookmarkDragManager = new BookmarkDragManager(this);
		this.previewTooltipController = new BookmarkPreviewTooltipController(this, recipeTransferService);
		bookmarkList.addSourceListChangedListener(() -> {
			worldConfig.setBookmarkEnabled(!bookmarkList.isEmpty());
			Minecraft minecraft = Minecraft.getInstance();
			this.getScreenPropertiesUpdater()
				.updateScreen(minecraft.screen)
				.update();
		});

		lookupHistoryOverlay.getLookupHistory().addSourceListChangedListener(() -> {
			Minecraft minecraft = Minecraft.getInstance();
			this.getScreenPropertiesUpdater()
				.updateScreen(minecraft.screen)
				.update();
		});

		bookmarkListConfig.addLayoutListener(this::onScreenPropertiesChanged);

		this.lookupHistoryEnabledListener = v -> onScreenPropertiesChanged();
		this.lookupHistoryViewSideListener = v -> onScreenPropertiesChanged();

		clientConfig.addLookupHistoryEnabledListener(lookupHistoryEnabledListener);
		clientConfig.addLookupHistoryDisplaySideListener(lookupHistoryViewSideListener);
	}

	public boolean isListDisplayed() {
		return worldConfig.isBookmarkOverlayEnabled() &&
			screenPropertiesCache.hasValidScreen() &&
			contents.hasRoom() &&
			!bookmarkList.isEmpty();
	}

	public boolean hasRoom() {
		return contents.hasRoom();
	}

	public ScreenPropertiesCache.Updater getScreenPropertiesUpdater() {
		return this.screenPropertiesCache.getUpdater(this::onScreenPropertiesChanged);
	}

	private void onScreenPropertiesChanged() {
		this.screenPropertiesCache.getGuiProperties()
			.ifPresentOrElse(this::updateBounds, () -> {
				this.contents.close();
				this.lookupHistoryOverlay.close();
			});
	}

	private void updateBounds(IGuiProperties guiProperties) {
		ImmutableRect2i displayArea = getDisplayArea(guiProperties);
		Set<ImmutableRect2i> guiExclusionAreas = this.screenPropertiesCache.getGuiExclusionAreas();
		ImmutablePoint2i mouseExclusionArea = this.screenPropertiesCache.getMouseExclusionArea();

		ImmutableRect2i availableContentsArea = displayArea.cropBottom(BUTTON_SIZE + INNER_PADDING);
		IElement<?> pageAnchorElement = this.contents.getPageAnchorElement();
		Optional<ImmutableRect2i> historyArea = Optional.empty();
		if (clientConfig.isLookupHistoryEnabled() && lookupHistoryOverlay.isOnSide()) {
			int historyHeight = lookupHistoryOverlay.getDisplayHeight();
			if (historyHeight > 0) {
				ImmutableRect2i area = displayArea
					.insetBy(BORDER_MARGIN)
					.cropBottom(BUTTON_SIZE + LOOKUP_HISTORY_BOTTOM_PADDING)
					.keepBottom(historyHeight);
				historyArea = Optional.of(area);
				availableContentsArea = cropBottomTo(
					availableContentsArea,
					area.y() - LOOKUP_HISTORY_PADDING_EXTRA
				);
			}
		}
		this.contents.updateBounds(availableContentsArea, guiExclusionAreas, mouseExclusionArea);
		this.contents.updateLayoutKeepingPageAnchorVisible(pageAnchorElement);

		historyArea.ifPresent(area -> {
			this.lookupHistoryOverlay.updateBounds(alignLookupHistoryArea(area), guiExclusionAreas, mouseExclusionArea);
			this.lookupHistoryOverlay.updateLayout();
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

	private ImmutableRect2i alignLookupHistoryArea(ImmutableRect2i lookupHistoryArea) {
		ImmutableRect2i ingredientGridArea = this.contents.getIngredientGridArea();
		if (ingredientGridArea.isEmpty()) {
			return lookupHistoryArea;
		}
		return lookupHistoryArea.matchWidthAndX(ingredientGridArea);
	}

	private static ImmutableRect2i getDisplayArea(IGuiProperties guiProperties) {
		int width = guiProperties.getGuiLeft();
		if (width <= 0) {
			width = 0;
		}
		int screenHeight = guiProperties.getScreenHeight();
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

	public void drawScreen(Minecraft minecraft, PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		if (isListDisplayed()) {
			this.bookmarkDragManager.updateDrag(mouseX, mouseY);
			this.contents.draw(minecraft, poseStack, mouseX, mouseY, partialTicks);
			drawPageFlipEdgeHighlights(poseStack, mouseX, mouseY);
		}
		if (screenPropertiesCache.hasValidScreen() && worldConfig.isOverlayEnabled()) {
			this.lookupHistoryOverlay.draw(minecraft, poseStack, mouseX, mouseY, partialTicks);
		}
		if (this.screenPropertiesCache.hasValidScreen()) {
			this.bookmarkButton.draw(poseStack, mouseX, mouseY, partialTicks);
			this.historyButton.draw(poseStack, mouseX, mouseY, partialTicks);
		}
	}

	private void drawPageFlipEdgeHighlights(PoseStack poseStack, int mouseX, int mouseY) {
		if (!this.bookmarkDragManager.isDragging() || !canFlipPage()) {
			return;
		}
		PageFlipHover.Direction hoveredDirection = getHoveredPageEdge(mouseX, mouseY);
		drawPageFlipEdgeHighlight(poseStack, getNextPageEdgeArea(), hoveredDirection == PageFlipHover.Direction.NEXT);
		drawPageFlipEdgeHighlight(poseStack, getBackPageEdgeArea(), hoveredDirection == PageFlipHover.Direction.PREVIOUS);
	}

	private static void drawPageFlipEdgeHighlight(PoseStack poseStack, ImmutableRect2i area, boolean hovered) {
		GuiColor color;
		if (hovered) {
			color = GuiColor.BOOKMARK_DRAG_PAGE_FLIP_HIGHLIGHT;
		} else {
			color = GuiColor.BOOKMARK_DRAG_PAGE_FLIP_HINT;
		}
		GuiComponent.fill(
			poseStack,
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

	public void drawTooltips(Minecraft minecraft, PoseStack poseStack, int mouseX, int mouseY) {
		if (!this.bookmarkDragManager.drawDraggedItem(poseStack, mouseX, mouseY)) {
			if (isListDisplayed() && !previewTooltipController.isVisible()) {
				this.contents.drawTooltips(minecraft, poseStack, mouseX, mouseY);
			}
			if (screenPropertiesCache.hasValidScreen() && worldConfig.isOverlayEnabled()) {
				this.lookupHistoryOverlay.drawTooltips(minecraft, poseStack, mouseX, mouseY);
			}
		}
		if (this.screenPropertiesCache.hasValidScreen()) {
			bookmarkButton.drawTooltips(poseStack, mouseX, mouseY);
			historyButton.drawTooltips(poseStack, mouseX, mouseY);
		}
	}

	public void tick() {
		this.bookmarkButton.tick();
		this.historyButton.tick();
		if (isListDisplayed()) {
			this.contents.tick();
		}
		if (screenPropertiesCache.hasValidScreen() && worldConfig.isOverlayEnabled()) {
			this.lookupHistoryOverlay.tick();
		}
	}

	@Override
	public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		Stream<IClickableIngredientInternal<?>> ingredients = Stream.empty();
		if (isListDisplayed()) {
			ingredients = this.contents.getIngredientUnderMouse(mouseX, mouseY);
		}
		if (lookupHistoryOverlay.isListDisplayed()) {
			ingredients = Stream.concat(ingredients, this.lookupHistoryOverlay.getIngredientUnderMouse(mouseX, mouseY));
		}
		return ingredients;
	}

	@Override
	public Stream<IDraggableIngredientInternal<?>> getDraggableIngredientUnderMouse(double mouseX, double mouseY) {
		Stream<IDraggableIngredientInternal<?>> ingredients = Stream.empty();
		if (isListDisplayed()) {
			ingredients = this.contents.getDraggableIngredientUnderMouse(mouseX, mouseY);
		}
		if (lookupHistoryOverlay.isListDisplayed()) {
			ingredients = Stream.concat(ingredients, this.lookupHistoryOverlay.getDraggableIngredientUnderMouse(mouseX, mouseY));
		}
		return ingredients;
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
		final IDragHandler historyDragHandler = this.lookupHistoryOverlay.createDragHandler();
		final IDragHandler combinedDragHandlers = new CombinedDragHandler(
			this.contents.createDragHandler(),
			historyDragHandler,
			this.bookmarkDragManager.createDragHandler()
		);

		return new ProxyDragHandler(() -> {
			if (isListDisplayed()) {
				return combinedDragHandlers;
			}
			if (lookupHistoryOverlay.isListDisplayed()) {
				return historyDragHandler;
			}
			return NullDragHandler.INSTANCE;
		});
	}

	public void drawOnForeground(PoseStack poseStack, int mouseX, int mouseY) {
		if (isListDisplayed()) {
			this.contents.drawOnForeground(poseStack, mouseX, mouseY);
		}
		this.lookupHistoryOverlay.drawOnForeground(poseStack, mouseX, mouseY);
	}

	public List<BookmarkDragTarget> createBookmarkDragTargets(IBookmark draggedBookmark) {
		List<IElement<?>> elements = this.bookmarkList.getElements();
		List<BookmarkDragTarget> targets = BookmarkDragTarget.createSlotTargets(this.contents.getAllSlots(), elements, draggedBookmark);
		List<IElement<?>> pageElements = this.contents.getPageElements();
		if (pageElements.isEmpty()) {
			return targets;
		}

		// Use the current page's range, including hidden bookmarks, even after navigating during a drag.
		int firstIndex = elements.indexOf(pageElements.get(0));
		int lastIndex = elements.indexOf(pageElements.get(pageElements.size() - 1));
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
			.keepRight(IngredientGrid.INGREDIENT_WIDTH / 2);
	}

	private ImmutableRect2i getBackPageEdgeArea() {
		return this.contents.getSlotBackgroundArea()
			.keepLeft(IngredientGrid.INGREDIENT_WIDTH / 2);
	}

	PageFlipHover.@Nullable Direction getHoveredPageEdge(double mouseX, double mouseY) {
		if (!canFlipPage()) {
			return null;
		}
		if (getNextPageEdgeArea().contains(mouseX, mouseY)) {
			return PageFlipHover.Direction.NEXT;
		}
		if (getBackPageEdgeArea().contains(mouseX, mouseY)) {
			return PageFlipHover.Direction.PREVIOUS;
		}
		return null;
	}

	private boolean canFlipPage() {
		return !this.bookmarkListConfig.navigationMode().get().usesScrollbar() &&
			getPageDelegate().getPageCount() > 1;
	}

	void scrollDuringDrag(BookmarkDragScroll dragScroll, double mouseX, double mouseY) {
		double pixels = dragScroll.update(this.contents.getSlotBackgroundArea(), this.bookmarkListConfig.navigationMode().get(), mouseX, mouseY);
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
