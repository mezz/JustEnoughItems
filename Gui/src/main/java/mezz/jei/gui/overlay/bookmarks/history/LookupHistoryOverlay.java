package mezz.jei.gui.overlay.bookmarks.history;

import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.config.HistoryDisplaySide;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.gui.JeiGuiColors;
import mezz.jei.common.gui.JeiGuiColors.GuiColor;
import mezz.jei.common.gui.elements.ScalableDrawable;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.ghost.GhostIngredientDragManager;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IDragHandler;
import mezz.jei.gui.input.IDraggableIngredientInternal;
import mezz.jei.gui.input.IRecipeFocusSource;
import mezz.jei.gui.overlay.history.LookupHistoryOverlayLayout;
import mezz.jei.gui.overlay.ingredients.IIngredientGridSource;
import mezz.jei.gui.overlay.ingredients.GuiExclusionAreaShadow;
import mezz.jei.gui.overlay.ingredients.IngredientGrid;
import mezz.jei.gui.overlay.elements.IElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class LookupHistoryOverlay implements IRecipeFocusSource, ILookupHistoryOverlay {

	public static final int SLOT_HEIGHT = LookupHistoryOverlayLayout.SLOT_HEIGHT;

	// display elements
	private final IngredientGrid contents;
	private final ScalableDrawable background;
	private final ScalableDrawable slotBackground;
	private final ScalableDrawable exclusionAreaShadow;

	// data
	private final IIngredientGridSource lookupHistory;
	private final IIngredientGridConfig historyListConfig;
	private final IClientConfig clientConfig;
	private final HistoryDisplaySide ownerDisplaySide;
	private final GhostIngredientDragManager ghostIngredientDragManager;
	private Set<ImmutableRect2i> guiExclusionAreas = Set.of();
	private ImmutableRect2i backgroundArea = ImmutableRect2i.EMPTY;
	private ImmutableRect2i slotBackgroundArea = ImmutableRect2i.EMPTY;
	private int rows;

	public LookupHistoryOverlay(
		IIngredientManager ingredientManager,
		IIngredientGridSource lookupHistory,
		IInternalKeyMappings keyMappings,
		IIngredientGridConfig historyListConfig,
		IIngredientFilterConfig ingredientFilterConfig,
		ScalableDrawable background,
		ScalableDrawable slotBackground,
		ScalableDrawable exclusionAreaShadow,
		IClientConfig clientConfig,
		HistoryDisplaySide ownerDisplaySide,
		IClientToggleState toggleState,
		IScreenHelper screenHelper,
		IConnectionToServer serverConnection,
		IColorHelper colorHelper
	) {
		this.clientConfig = clientConfig;
		this.lookupHistory = lookupHistory;
		this.historyListConfig = historyListConfig;
		this.background = background;
		this.slotBackground = slotBackground;
		this.exclusionAreaShadow = exclusionAreaShadow;
		this.contents = new IngredientGrid(
			ingredientManager,
			historyListConfig,
			ingredientFilterConfig,
			clientConfig,
			toggleState,
			serverConnection,
			keyMappings,
			colorHelper,
			false
		);
		this.ghostIngredientDragManager = new GhostIngredientDragManager(this.contents, screenHelper, ingredientManager, toggleState);
		this.ownerDisplaySide = ownerDisplaySide;
		lookupHistory.addSourceListChangedListener(this::updateLayout);
	}

	public boolean isListDisplayed() {
		return clientConfig.isLookupHistoryEnabled() &&
			isDisplayedOnThisSide() &&
			contents.hasRoom();
	}

	@Override
	public boolean isDisplayedOnThisSide() {
		return ownerDisplaySide.equals(clientConfig.getLookupHistoryDisplaySide());
	}

	public IIngredientGridSource getLookupHistory() {
		return lookupHistory;
	}

	@Override
	public int getDisplayHeight() {
		return getDisplayHeight(clientConfig.getMaxLookupHistoryRows(), historyListConfig.drawBackground());
	}

	public static int getDisplayHeight(int maxRows, boolean drawBackground) {
		return LookupHistoryOverlayLayout.getDisplayHeight(maxRows, drawBackground);
	}

	@Override
	public void updateBounds(final ImmutableRect2i availableArea, Set<ImmutableRect2i> guiExclusionAreas, @Nullable ImmutablePoint2i mouseExclusionPoint) {
		this.guiExclusionAreas = guiExclusionAreas;
		LookupHistoryOverlayLayout layout = LookupHistoryOverlayLayout.calculate(this.historyListConfig, availableArea);
		this.contents.updateBounds(layout.availableGridArea(), guiExclusionAreas, mouseExclusionPoint);
		this.backgroundArea = layout.backgroundArea();
		this.slotBackgroundArea = layout.slotBackgroundArea();
		int rows = this.contents.getArea().getHeight() / SLOT_HEIGHT;
		this.rows = Math.min(rows, clientConfig.getMaxLookupHistoryRows());
	}

	@Override
	public void updateLayout() {
		List<IElement<?>> ingredientList = lookupHistory.getElements();
		this.contents.set(0, ingredientList);
	}

	private void drawLine(GuiGraphicsExtractor guiGraphics, ImmutableRect2i lineArea, int argbColor) {
		for (LineSegment segment : calculateLineSegments(lineArea, this.guiExclusionAreas)) {
			drawLineSegment(guiGraphics, segment.x1(), segment.x2(), lineArea.y(), lineArea.height(), argbColor);
		}
	}

	private static void drawLineSegment(GuiGraphicsExtractor guiGraphics, int x1, int x2, int y, int height, int argbColor) {
		final int availableWidth = x2 - x1;
		if (availableWidth <= 0) {
			return;
		}
		final int dashWidth = 8;
		final int spacing = 6;
		if (availableWidth < 2 * dashWidth + spacing) {
			guiGraphics.fill(Math.min(x1 + dashWidth, x2), y, x1, y + height, argbColor);
			return;
		}

		// space out the dashes so that we always start and end with whole dashes
		final int interval = dashWidth + spacing;
		final int dashCount = availableWidth / interval + 1;
		final float floatInterval = (availableWidth - dashWidth) / (float) (dashCount - 1);

		for (int i = 0; i < dashCount; i++) {
			float x = x1 + i * floatInterval;
			guiGraphics.fill(
				(int) Mth.clamp(x + dashWidth, x1, x2),
				y,
				(int) Mth.clamp(x, x1, x2),
				y + height,
				argbColor);
		}
	}

	static List<LineSegment> calculateLineSegments(ImmutableRect2i lineArea, Set<ImmutableRect2i> guiExclusionAreas) {
		if (lineArea.isEmpty()) {
			return List.of();
		}
		if (guiExclusionAreas.isEmpty()) {
			return List.of(new LineSegment(lineArea.x(), lineArea.x() + lineArea.width()));
		}

		List<LineSegment> blockedSegments = guiExclusionAreas.stream()
			.filter(lineArea::intersects)
			.map(exclusionArea -> new LineSegment(
				Math.max(lineArea.x(), exclusionArea.x()),
				Math.min(lineArea.x() + lineArea.width(), exclusionArea.x() + exclusionArea.width())
			))
			.filter(segment -> segment.x1() < segment.x2())
			.sorted(Comparator.comparingInt(LineSegment::x1))
			.toList();

		if (blockedSegments.isEmpty()) {
			return List.of(new LineSegment(lineArea.x(), lineArea.x() + lineArea.width()));
		}

		List<LineSegment> lineSegments = new ArrayList<>();
		int currentX = lineArea.x();
		int lineRight = lineArea.x() + lineArea.width();
		for (LineSegment blockedSegment : blockedSegments) {
			if (blockedSegment.x1() > currentX) {
				lineSegments.add(new LineSegment(currentX, blockedSegment.x1()));
			}
			currentX = Math.max(currentX, blockedSegment.x2());
		}
		if (currentX < lineRight) {
			lineSegments.add(new LineSegment(currentX, lineRight));
		}
		return List.copyOf(lineSegments);
	}

	public void draw(Minecraft minecraft, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (isListDisplayed()) {
			this.contents.draw(minecraft, guiGraphics, mouseX, mouseY);
			if (!this.historyListConfig.drawBackground()) {
				ImmutableRect2i area = this.contents.getArea();
				int startY = area.getY() + area.getHeight() - rows * SLOT_HEIGHT - 3;
				int color = JeiGuiColors.getColor(GuiColor.LOOKUP_HISTORY_LINE);
				ImmutableRect2i lineArea = new ImmutableRect2i(area.getX(), startY, area.getWidth(), 1);
				drawLine(guiGraphics, lineArea, color);
			}
		}
	}

	public void drawBackground(GuiGraphicsExtractor guiGraphics) {
		if (isListDisplayed() && this.historyListConfig.drawBackground()) {
			this.background.draw(guiGraphics, this.backgroundArea);
			this.slotBackground.draw(guiGraphics, this.slotBackgroundArea);
			GuiExclusionAreaShadow.draw(guiGraphics, this.exclusionAreaShadow, this.backgroundArea, this.guiExclusionAreas);
		}
	}

	public void drawTooltips(Minecraft minecraft, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		if (isListDisplayed()) {
			this.ghostIngredientDragManager.drawTooltips(minecraft, guiGraphics, mouseX, mouseY);
			this.contents.drawTooltips(minecraft, guiGraphics, mouseX, mouseY);
		}
	}

	public void tick() {
		if (isListDisplayed()) {
			this.contents.tick();
		}
	}

	public ImmutableRect2i getArea() {
		return this.contents.getArea();
	}

	@Override
	public void close() {
		this.guiExclusionAreas = Set.of();
		this.backgroundArea = ImmutableRect2i.EMPTY;
		this.slotBackgroundArea = ImmutableRect2i.EMPTY;
		this.ghostIngredientDragManager.stopDrag();
	}

	public void drawOnForeground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		if (isListDisplayed()) {
			this.ghostIngredientDragManager.drawOnForeground(guiGraphics, mouseX, mouseY);
		}
	}

	@Override
	public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		if (isListDisplayed()) {
			return contents.getIngredientUnderMouse(mouseX, mouseY);
		}
		return Stream.empty();
	}

	@Override
	public Stream<IDraggableIngredientInternal<?>> getDraggableIngredientUnderMouse(double mouseX, double mouseY) {
		if (isListDisplayed()) {
			return contents.getDraggableIngredientUnderMouse(mouseX, mouseY);
		}
		return Stream.empty();
	}

	public IDragHandler createDragHandler() {
		return this.ghostIngredientDragManager.createDragHandler();
	}

	record LineSegment(int x1, int x2) {

	}
}
