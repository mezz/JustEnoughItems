package mezz.jei.gui.overlay.ingredients;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.config.IngredientGridBackgroundStyle;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.ImmutableSize2i;
import mezz.jei.gui.PageNavigation;
import mezz.jei.gui.ghost.GhostIngredientDragManager;
import mezz.jei.gui.ghost.GhostIngredientQuickMoveManager;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IDragHandler;
import mezz.jei.gui.input.IDraggableIngredientInternal;
import mezz.jei.gui.input.IPaged;
import mezz.jei.common.input.IUserInputHandler;
import mezz.jei.common.input.handlers.CombinedInputHandler;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.util.CommandUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Displays a list of ingredients with navigation at the top.
 */
public class IngredientGridWithNavigation implements IIngredientListOverlayContents {
	private final IngredientGridWithNavigationController controller;
	private final PageNavigation navigation;
	private final IngredientGridScrollbar scrollbar;
	private final IIngredientGridConfig gridConfig;
	private final IngredientGridResizer resizer;
	private @Nullable ImmutableRect2i resizeArea;
	private @Nullable VerticalAlignment resizeVerticalAlignment;
	private final IClientConfig clientConfig;
	private final IngredientGrid ingredientGrid;
	private final IIngredientGridSource ingredientSource;
	private final GhostIngredientDragManager ghostIngredientDragManager;
	private final IUserInputHandler inputHandler;

	private ImmutableRect2i backgroundArea = ImmutableRect2i.EMPTY;
	private ImmutableRect2i slotBackgroundArea = ImmutableRect2i.EMPTY;
	@Nullable
	private ImmutableRect2i availableArea;
	private Set<ImmutableRect2i> guiExclusionAreas = Set.of();
	@Nullable
	private ImmutablePoint2i mouseExclusionPoint;
	private boolean active;
	private boolean layoutDirty;

	public IngredientGridWithNavigation(
		String debugName,
		IIngredientGridSource ingredientSource,
		IngredientGrid ingredientGrid,
		IClientToggleState toggleState,
		IClientConfig clientConfig,
		IConnectionToServer serverConnection,
		IIngredientGridConfig gridConfig,
		IScreenHelper screenHelper,
		IIngredientManager ingredientManager
	) {
		this.ingredientGrid = ingredientGrid;
		this.ingredientSource = ingredientSource;
		this.gridConfig = gridConfig;
		this.resizer = new IngredientGridResizer(this, gridConfig, clientConfig);
		this.clientConfig = clientConfig;
		CommandUtil commandUtil = new CommandUtil(clientConfig, serverConnection);
		this.ghostIngredientDragManager = new GhostIngredientDragManager(this.ingredientGrid, screenHelper, ingredientManager, toggleState);
		GhostIngredientQuickMoveManager ghostIngredientQuickMoveManager = new GhostIngredientQuickMoveManager(this.ingredientGrid, screenHelper);
		this.controller = new IngredientGridWithNavigationController(
			ingredientSource,
			this.ingredientGrid,
			gridConfig,
			toggleState,
			clientConfig,
			commandUtil,
			ingredientManager,
			this::isMouseOver,
			ghostIngredientQuickMoveManager
		);
		this.navigation = new PageNavigation(this.controller, false);
		this.scrollbar = new IngredientGridScrollbar(this.controller);
		this.controller.setOnLayoutChanged(this.navigation::updatePageNumber);
		this.inputHandler = new CombinedInputHandler(
			debugName,
			this.scrollbar,
			this.controller,
			this.navigation.createInputHandler()
		);

		this.ingredientSource.addSourceListChangedListener(this::markLayoutDirty);
		addGridConfigListeners(gridConfig);
		Internal.registerRuntimeListenerRemoval(clientConfig.guiResizeEnabled().addListener(v -> resizer.unfocus()));
		Internal.registerRuntimeListenerRemoval(clientConfig.smoothScrollingEnabled().addListener(v -> markLayoutDirty()));
	}

	private void addGridConfigListeners(IIngredientGridConfig gridConfig) {
		Internal.registerRuntimeListenerRemoval(gridConfig.maxColumns().addListener(v -> markLayoutDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.maxRows().addListener(v -> markLayoutDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.backgroundStyle().addListener(v -> markLayoutDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.layoutMode().addListener(v -> markLayoutDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.navigationMode().addListener(v -> markLayoutDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.horizontalAlignment().addListener(v -> markLayoutDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.verticalAlignment().addListener(v -> markLayoutDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.navigationVisibility().addListener(v -> markLayoutDirty()));
	}

	private void markLayoutDirty() {
		this.layoutDirty = true;
	}

	public void setResizeBounds(ImmutableRect2i area, VerticalAlignment verticalAlignment) {
		this.resizeArea = area;
		this.resizeVerticalAlignment = verticalAlignment;
	}

	VerticalAlignment getResizeVerticalAlignment() {
		if (resizeVerticalAlignment != null) {
			return resizeVerticalAlignment;
		}
		return gridConfig.verticalAlignment().get();
	}

	@Override
	public IUserInputHandler getResizeInputHandler() {
		return resizer;
	}

	boolean isResizeExcluded(double mouseX, double mouseY) {
		return guiExclusionAreas.stream().anyMatch(area -> area.contains(mouseX, mouseY)) ||
			(mouseExclusionPoint != null && mouseExclusionPoint.x() == (int) mouseX && mouseExclusionPoint.y() == (int) mouseY);
	}

	ImmutableSize2i getMaximumResizeSize() {
		ImmutableRect2i area = resizeArea;
		if (area == null) {
			area = availableArea;
		}
		if (area == null) {
			return ImmutableSize2i.EMPTY;
		}
		ImmutableRect2i gridArea = ingredientGrid.getArea();
		int margin = 2 * IngredientGridWithNavigationLayout.BORDER_MARGIN;
		return new ImmutableSize2i(
			Math.max(0, area.width() - margin - (backgroundArea.width() - gridArea.width())),
			Math.max(0, area.height() - margin - (backgroundArea.height() - gridArea.height()))
		);
	}

	private void updateLayoutIfDirty() {
		if (this.layoutDirty && this.availableArea != null) {
			IElement<?> pageAnchorElement = getPageAnchorElement();
			updateBounds(this.availableArea, this.guiExclusionAreas, this.mouseExclusionPoint);
			this.controller.updateLayoutKeepingPageAnchorVisible(pageAnchorElement);
		}
	}

	@Override
	public boolean hasRoom() {
		updateLayoutIfDirty();
		return this.active;
	}

	@Override
	public void updateLayoutToFirstPage() {
		this.controller.updateLayoutToFirstPage();
	}

	@Override
	public void updateLayoutKeepingPageAnchorVisible(@Nullable IElement<?> pageAnchorElement) {
		this.controller.updateLayoutKeepingPageAnchorVisible(pageAnchorElement);
	}

	public void setPageAnchorElement(IElement<?> pageAnchorElement) {
		this.controller.setPageAnchorElement(pageAnchorElement);
	}

	public List<IElement<?>> getPageElements() {
		updateLayoutIfDirty();
		return this.controller.getPageElements();
	}

	@Override
	public @Nullable IElement<?> getPageAnchorElement() {
		return this.controller.getPageAnchorElement();
	}

	@Override
	public void updateBounds(final ImmutableRect2i availableArea, Set<ImmutableRect2i> guiExclusionAreas, @Nullable ImmutablePoint2i mouseExclusionPoint) {
		this.availableArea = availableArea;
		this.guiExclusionAreas = guiExclusionAreas;
		this.mouseExclusionPoint = mouseExclusionPoint;
		this.layoutDirty = false;
		IngredientGridWithNavigationLayout layout = calculateLayout(
			availableArea,
			guiExclusionAreas,
			this.ingredientSource.getElements().size()
		);
		applyLayout(layout, guiExclusionAreas, mouseExclusionPoint);
	}

	private IngredientGridWithNavigationLayout calculateLayout(
		final ImmutableRect2i availableArea,
		Set<ImmutableRect2i> guiExclusionAreas,
		int ingredientCount
	) {
		if (this.gridConfig.navigationMode().get().usesScrollbar()) {
			return IngredientGridScrollbarLayout.calculate(
				this.gridConfig,
				availableArea,
				guiExclusionAreas,
				ingredientCount,
				isSmoothScrolling()
			);
		}

		return IngredientGridButtonNavigationLayout.calculate(
			this.gridConfig,
			availableArea,
			guiExclusionAreas,
			ingredientCount
		);
	}

	private void applyLayout(
		IngredientGridWithNavigationLayout layout,
		Set<ImmutableRect2i> guiExclusionAreas,
		@Nullable ImmutablePoint2i mouseExclusionPoint
	) {
		this.guiExclusionAreas = guiExclusionAreas;
		if (!layout.hasRoom()) {
			clearLayout();
			return;
		}

		this.ingredientGrid.updateBounds(
			layout.ingredientGridArea(),
			guiExclusionAreas,
			mouseExclusionPoint
		);
		this.slotBackgroundArea = layout.slotBackgroundArea();
		this.navigation.updateBounds(layout.navigationArea());
		this.scrollbar.updateBounds(layout.scrollbarArea());
		this.backgroundArea = layout.backgroundArea();
		this.active = true;
	}

	private void clearLayout() {
		this.resizer.unfocus();
		this.ingredientGrid.updateBounds(ImmutableRect2i.EMPTY, Set.of(), null);
		this.slotBackgroundArea = ImmutableRect2i.EMPTY;
		this.navigation.updateBounds(ImmutableRect2i.EMPTY);
		this.scrollbar.updateBounds(ImmutableRect2i.EMPTY);
		this.backgroundArea = ImmutableRect2i.EMPTY;
		this.active = false;
	}

	@Override
	public ImmutableRect2i getBackgroundArea() {
		updateLayoutIfDirty();
		return this.backgroundArea;
	}

	public ImmutableRect2i getIngredientGridArea() {
		updateLayoutIfDirty();
		return this.ingredientGrid.getArea();
	}

	@Override
	public boolean isBackgroundEnabled() {
		return this.gridConfig.backgroundStyle().get().isEnabled();
	}

	@Override
	public ImmutableRect2i getSlotBackgroundArea() {
		updateLayoutIfDirty();
		return this.slotBackgroundArea;
	}

	public ImmutableRect2i getNextPageButtonArea() {
		updateLayoutIfDirty();
		return this.navigation.getNextButtonArea();
	}

	public ImmutableRect2i getBackButtonArea() {
		updateLayoutIfDirty();
		return this.navigation.getBackButtonArea();
	}

	public IPaged getPageDelegate() {
		return controller;
	}

	public void scrollByPixels(double pixels) {
		updateLayoutIfDirty();
		this.controller.scrollByPixels(pixels);
	}

	private boolean isSmoothScrolling() {
		return this.gridConfig.navigationMode().get().usesScrollbar() &&
			this.clientConfig.smoothScrollingEnabled().get();
	}

	public void setPageButtonsForcePressed(boolean nextButton, boolean backButton) {
		this.navigation.setForcePressed(nextButton, backButton);
	}

	@Override
	public void drawForeground(Minecraft minecraft, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (!this.active) {
			return;
		}
		boolean drawSlotBackgrounds = this.gridConfig.backgroundStyle().get() == IngredientGridBackgroundStyle.GRID;
		this.ingredientGrid.draw(minecraft, guiGraphics, mouseX, mouseY, drawSlotBackgrounds);
		this.scrollbar.draw(guiGraphics, mouseX, mouseY);
		this.navigation.draw(minecraft, guiGraphics, mouseX, mouseY, partialTicks);
		this.resizer.requestCursor(guiGraphics, mouseX, mouseY);
	}

	@Override
	public void drawTooltips(Minecraft minecraft, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		updateLayoutIfDirty();
		if (!this.active) {
			return;
		}
		this.ghostIngredientDragManager.drawTooltips(minecraft, guiGraphics, mouseX, mouseY);
		if (this.resizer.isMouseOver(mouseX, mouseY)) {
			return;
		}
		this.ingredientGrid.drawTooltips(minecraft, guiGraphics, mouseX, mouseY);
	}

	@Override
	public void tick() {
		if (!this.active) {
			return;
		}
		this.ingredientGrid.tick();
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		updateLayoutIfDirty();
		return this.active &&
			this.backgroundArea.contains(mouseX, mouseY) &&
			this.guiExclusionAreas.stream()
				.noneMatch(area -> area.contains(mouseX, mouseY));
	}

	@Override
	public IUserInputHandler createDeleteItemInputHandler() {
		return this.ingredientGrid.getInputHandler();
	}

	@Override
	public IUserInputHandler createInputHandler() {
		return this.inputHandler;
	}

	@Override
	public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		updateLayoutIfDirty();
		if (!this.active || this.resizer.isMouseOver(mouseX, mouseY)) {
			return Stream.empty();
		}
		return this.ingredientGrid.getIngredientUnderMouse(mouseX, mouseY)
			.map(this.controller::createPageAnchorIngredient);
	}

	@Override
	public Stream<IDraggableIngredientInternal<?>> getDraggableIngredientUnderMouse(double mouseX, double mouseY) {
		updateLayoutIfDirty();
		if (!this.active || this.resizer.isMouseOver(mouseX, mouseY)) {
			return Stream.empty();
		}
		return this.ingredientGrid.getDraggableIngredientUnderMouse(mouseX, mouseY);
	}

	@Override
	public <T> Stream<T> getVisibleIngredients(IIngredientType<T> ingredientType) {
		updateLayoutIfDirty();
		if (!this.active) {
			return Stream.empty();
		}
		return this.ingredientGrid.getVisibleIngredients(ingredientType);
	}

	@Override
	public boolean isEmpty() {
		return this.ingredientSource.getElements().isEmpty();
	}

	@Override
	public void close() {
		clearLayout();
		this.ghostIngredientDragManager.stopDrag();
	}

	@Override
	public void drawOnForeground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		updateLayoutIfDirty();
		if (!this.active) {
			return;
		}
		this.ghostIngredientDragManager.drawOnForeground(guiGraphics, mouseX, mouseY);
	}

	@Override
	public IDragHandler createDragHandler() {
		return this.ghostIngredientDragManager.createDragHandler();
	}

	public int size() {
		if (!this.active) {
			return 0;
		}
		return this.ingredientGrid.size();
	}

	public Stream<IngredientListSlot> getSlots() {
		updateLayoutIfDirty();
		if (!this.active) {
			return Stream.empty();
		}
		return this.ingredientGrid.getSlots();
	}

	public List<IngredientListSlot> getAllSlots() {
		updateLayoutIfDirty();
		if (!this.active) {
			return List.of();
		}
		return this.ingredientGrid.getAllSlots();
	}
}
