package mezz.jei.gui.overlay;

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
	private final IIngredientGridConfig gridConfig;
	private final IngredientGrid ingredientGrid;
	private final IIngredientGridSource ingredientSource;
	private final ScalableDrawable background;
	private final ScalableDrawable slotBackground;
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
		IScreenHelper screenHelper,
		IIngredientManager ingredientManager
	) {
		this.ingredientGrid = ingredientGrid;
		this.ingredientSource = ingredientSource;
		this.gridConfig = gridConfig;
		this.background = background;
		this.slotBackground = slotBackground;
		CommandUtil commandUtil = new CommandUtil(clientConfig, serverConnection);
		this.ghostIngredientDragManager = new GhostIngredientDragManager(this.ingredientGrid, screenHelper, ingredientManager, toggleState);
		GhostIngredientQuickMoveManager ghostIngredientQuickMoveManager = new GhostIngredientQuickMoveManager(this.ingredientGrid, screenHelper);
		this.controller = new IngredientGridWithNavigationController(
			ingredientSource,
			this.ingredientGrid,
			toggleState,
			clientConfig,
			commandUtil,
			ingredientManager,
			this::isMouseOver,
			ghostIngredientQuickMoveManager
		);
		this.navigation = new PageNavigation(this.controller, false);
		this.controller.setOnLayoutChanged(this.navigation::updatePageNumber);
		this.inputHandler = new CombinedInputHandler(
			debugName,
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
		return this.ingredientGrid.hasRoom();
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
		IngredientGridWithNavigationLayout.Layout layout = IngredientGridWithNavigationLayout.calculate(
			this.gridConfig,
			availableArea,
			guiExclusionAreas,
			mouseExclusionPoint,
			this.ingredientSource.getElements().size()
		);
		applyLayout(layout, guiExclusionAreas, mouseExclusionPoint);
	}

	private void applyLayout(
		IngredientGridWithNavigationLayout.Layout layout,
		Set<ImmutableRect2i> guiExclusionAreas,
		@Nullable ImmutablePoint2i mouseExclusionPoint
	) {
		this.guiExclusionAreas = guiExclusionAreas;
		this.ingredientGrid.updateBounds(layout.availableGridArea(), guiExclusionAreas, mouseExclusionPoint);
		if (!layout.hasRoom()) {
			this.active = false;
			return;
		}

		this.slotBackgroundArea = layout.slotBackgroundArea();
		this.navigation.updateBounds(layout.navigationArea());
		this.backgroundArea = layout.backgroundArea();
		this.active = true;
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
	public void draw(Minecraft minecraft, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (gridConfig.drawBackground()) {
			background.draw(guiGraphics, this.backgroundArea);
			slotBackground.draw(guiGraphics, this.slotBackgroundArea);
		}

		this.ingredientGrid.draw(minecraft, guiGraphics, mouseX, mouseY);
		this.navigation.draw(minecraft, guiGraphics, mouseX, mouseY, partialTicks);
	}

	@Override
	public void drawTooltips(Minecraft minecraft, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		this.ghostIngredientDragManager.drawTooltips(minecraft, guiGraphics, mouseX, mouseY);
		this.ingredientGrid.drawTooltips(minecraft, guiGraphics, mouseX, mouseY);
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return this.backgroundArea.contains(mouseX, mouseY) &&
			this.guiExclusionAreas.stream()
				.noneMatch(area -> area.contains(mouseX, mouseY));
	}

	@Override
	public IUserInputHandler createInputHandler() {
		return this.inputHandler;
	}

	@Override
	public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		return this.ingredientGrid.getIngredientUnderMouse(mouseX, mouseY)
			.map(this.controller::createPageAnchorIngredient);
	}

	@Override
	public Stream<IDraggableIngredientInternal<?>> getDraggableIngredientUnderMouse(double mouseX, double mouseY) {
		return this.ingredientGrid.getDraggableIngredientUnderMouse(mouseX, mouseY);
	}

	@Override
	public <T> Stream<T> getVisibleIngredients(IIngredientType<T> ingredientType) {
		return this.ingredientGrid.getVisibleIngredients(ingredientType);
	}

	@Override
	public boolean isEmpty() {
		return this.ingredientSource.getElements().isEmpty();
	}

	@Override
	public void close() {
		this.active = false;
		this.ghostIngredientDragManager.stopDrag();
	}

	@Override
	public void drawOnForeground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		this.ghostIngredientDragManager.drawOnForeground(guiGraphics, mouseX, mouseY);
	}

	@Override
	public IDragHandler createDragHandler() {
		return this.ghostIngredientDragManager.createDragHandler();
	}

	public int size() {
		return this.ingredientGrid.size();
	}

	public Stream<IngredientListSlot> getSlots() {
		return this.ingredientGrid.getSlots();
	}
}
