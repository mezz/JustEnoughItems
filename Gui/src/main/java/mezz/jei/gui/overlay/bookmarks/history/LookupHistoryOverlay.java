package mezz.jei.gui.overlay.bookmarks.history;

import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.config.HistoryDisplaySide;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.ghost.GhostIngredientDragManager;
import mezz.jei.gui.ingredients.GuiIngredientProperties;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IDragHandler;
import mezz.jei.gui.input.IDraggableIngredientInternal;
import mezz.jei.gui.input.IRecipeFocusSource;
import mezz.jei.gui.overlay.ingredients.IIngredientGridSource;
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

	private static final int INGREDIENT_PADDING = 1;
	public static final int SLOT_HEIGHT = GuiIngredientProperties.getHeight(INGREDIENT_PADDING);

	// display elements
	private final IngredientGrid contents;

	// data
	private final IIngredientGridSource lookupHistory;
	private final IClientConfig clientConfig;
	private final HistoryDisplaySide ownerDisplaySide;
	private final GhostIngredientDragManager ghostIngredientDragManager;
	private Set<ImmutableRect2i> guiExclusionAreas = Set.of();
	private int rows;

	public LookupHistoryOverlay(
			IIngredientManager ingredientManager,
			IIngredientGridSource lookupHistory,
			IInternalKeyMappings keyMappings,
			IIngredientGridConfig historyListConfig,
			IIngredientFilterConfig ingredientFilterConfig,
			IClientConfig clientConfig,
			HistoryDisplaySide ownerDisplaySide,
			IClientToggleState toggleState,
			IScreenHelper screenHelper,
			IConnectionToServer serverConnection,
			IColorHelper colorHelper
	) {
		this.clientConfig = clientConfig;
		this.lookupHistory = lookupHistory;
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
	public void updateBounds(final ImmutableRect2i availableArea, Set<ImmutableRect2i> guiExclusionAreas, @Nullable ImmutablePoint2i mouseExclusionPoint) {
		this.guiExclusionAreas = guiExclusionAreas;
		this.contents.updateBounds(availableArea, guiExclusionAreas, mouseExclusionPoint);
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
				argbColor)
			;
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
			ImmutableRect2i area = this.contents.getArea();
			int startY = area.getY() + area.getHeight() - rows * SLOT_HEIGHT - 3;
			int color = 0xFF959595;
			ImmutableRect2i lineArea = new ImmutableRect2i(area.getX(), startY, area.getWidth(), 1);
			drawLine(guiGraphics, lineArea, color);
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
