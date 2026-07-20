package mezz.jei.gui.overlay.ingredients;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.gui.elements.ScalableDrawable;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.PageNavigation;
import mezz.jei.gui.ghost.GhostIngredientDragManager;
import mezz.jei.gui.ghost.GhostIngredientQuickMoveManager;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IDragHandler;
import mezz.jei.gui.input.IDraggableIngredientInternal;
import mezz.jei.gui.input.IPaged;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.handlers.CombinedInputHandler;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.util.CommandUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jspecify.annotations.Nullable;

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
	private final IngredientGrid ingredientGrid;
	private final IIngredientGridSource ingredientSource;
	private final ScalableDrawable background;
	private final ScalableDrawable slotBackground;
	private final ScalableDrawable exclusionAreaShadow;
	private final GhostIngredientDragManager ghostIngredientDragManager;
	private final IUserInputHandler inputHandler;

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
		ScalableDrawable background,
		ScalableDrawable slotBackground,
		ScalableDrawable exclusionAreaShadow,
		IScreenHelper screenHelper,
		IIngredientManager ingredientManager
	) {
		this.ingredientGrid = ingredientGrid;
		this.ingredientSource = ingredientSource;
		this.gridConfig = gridConfig;
		this.background = background;
		this.slotBackground = slotBackground;
		this.exclusionAreaShadow = exclusionAreaShadow;
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
			this.ingredientGrid.getInputHandler(),
			this.navigation.createInputHandler()
		);

		this.ingredientSource.addSourceListChangedListener(() -> {
			if (isActive()) {
				updateLayoutKeepingPageAnchorVisible(getPageAnchorElement());
			}
		});
	}

	private boolean isActive() {
		return active;
	}

	@Override
	public boolean hasRoom() {
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

	@Override
	public @Nullable IElement<?> getPageAnchorElement() {
		return this.controller.getPageAnchorElement();
	}

	@Override
	public void updateBounds(final ImmutableRect2i availableArea, Set<ImmutableRect2i> guiExclusionAreas, @Nullable ImmutablePoint2i mouseExclusionPoint) {
		IngredientGridWithNavigationLayout layout = calculateLayout(
			availableArea,
			guiExclusionAreas,
			mouseExclusionPoint,
			this.ingredientSource.getElements().size()
		);
		applyLayout(layout, guiExclusionAreas, mouseExclusionPoint);
	}

	private IngredientGridWithNavigationLayout calculateLayout(
		final ImmutableRect2i availableArea,
		Set<ImmutableRect2i> guiExclusionAreas,
		@Nullable ImmutablePoint2i mouseExclusionPoint,
		int ingredientCount
	) {
		if (this.gridConfig.getNavigationMode().usesScrollbar()) {
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

		this.ingredientGrid.updateBounds(layout.ingredientGridArea(), guiExclusionAreas, mouseExclusionPoint);
		this.slotBackgroundArea = layout.slotBackgroundArea();
		this.navigation.updateBounds(layout.navigationArea());
		this.scrollbar.updateBounds(layout.scrollbarArea());
		this.backgroundArea = layout.backgroundArea();
		this.active = true;
	}

	private void clearLayout() {
		this.ingredientGrid.updateBounds(ImmutableRect2i.EMPTY, Set.of(), null);
		this.slotBackgroundArea = ImmutableRect2i.EMPTY;
		this.navigation.updateBounds(ImmutableRect2i.EMPTY);
		this.scrollbar.updateBounds(ImmutableRect2i.EMPTY);
		this.backgroundArea = ImmutableRect2i.EMPTY;
		this.active = false;
	}

	@Override
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
		return controller;
	}

	@Override
	public void drawBackground(GuiGraphicsExtractor guiGraphics) {
		if (!this.active) {
			return;
		}
		if (this.gridConfig.drawBackground()) {
			this.background.draw(guiGraphics, this.backgroundArea);
			this.slotBackground.draw(guiGraphics, this.slotBackgroundArea);
			GuiExclusionAreaShadow.draw(guiGraphics, this.exclusionAreaShadow, this.backgroundArea, this.guiExclusionAreas);
		}
	}

	@Override
	public void drawForeground(Minecraft minecraft, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (!this.active) {
			return;
		}
		this.ingredientGrid.draw(minecraft, guiGraphics, mouseX, mouseY);
		this.scrollbar.draw(guiGraphics, mouseX, mouseY);
		this.navigation.draw(minecraft, guiGraphics, mouseX, mouseY, partialTicks);
	}

	@Override
	public void drawTooltips(Minecraft minecraft, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		if (!this.active) {
			return;
		}
		this.ghostIngredientDragManager.drawTooltips(minecraft, guiGraphics, mouseX, mouseY);
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
		return this.active &&
			this.backgroundArea.contains(mouseX, mouseY) &&
			this.guiExclusionAreas.stream()
				.noneMatch(area -> area.contains(mouseX, mouseY));
	}

	@Override
	public IUserInputHandler createInputHandler() {
		return this.inputHandler;
	}

	@Override
	public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		if (!this.active) {
			return Stream.empty();
		}
		return this.ingredientGrid.getIngredientUnderMouse(mouseX, mouseY)
			.map(this.controller::createPageAnchorIngredient);
	}

	@Override
	public Stream<IDraggableIngredientInternal<?>> getDraggableIngredientUnderMouse(double mouseX, double mouseY) {
		if (!this.active) {
			return Stream.empty();
		}
		return this.ingredientGrid.getDraggableIngredientUnderMouse(mouseX, mouseY);
	}

	@Override
	public <T> Stream<T> getVisibleIngredients(IIngredientType<T> ingredientType) {
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
		if (!this.active) {
			return Stream.empty();
		}
		return this.ingredientGrid.getSlots();
	}
}
