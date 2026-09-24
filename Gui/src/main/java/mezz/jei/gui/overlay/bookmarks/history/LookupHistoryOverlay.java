package mezz.jei.gui.overlay.bookmarks.history;

import mezz.jei.common.config.HistoryDisplaySide;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.gui.JeiGuiColors;
import mezz.jei.common.gui.JeiGuiColors.GuiColor;
import mezz.jei.common.input.IUserInputHandler;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IDragHandler;
import mezz.jei.gui.input.IDraggableIngredientInternal;
import mezz.jei.gui.input.IRecipeFocusSource;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.overlay.history.LookupHistoryOverlayLayout;
import mezz.jei.gui.overlay.ingredients.IIngredientGridSource;
import mezz.jei.gui.overlay.ingredients.IngredientGridWithNavigation;
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
	private final IngredientGridWithNavigation contents;
	private final IIngredientGridSource lookupHistory;
	private final IIngredientGridConfig historyListConfig;
	private final IClientConfig clientConfig;
	private final HistoryDisplaySide ownerDisplaySide;
	private Set<ImmutableRect2i> guiExclusionAreas = Set.of();
	@Nullable
	private IElement<?> pageAnchorElement;

	public LookupHistoryOverlay(
		IIngredientGridSource lookupHistory,
		IngredientGridWithNavigation contents,
		IIngredientGridConfig historyListConfig,
		IClientConfig clientConfig,
		HistoryDisplaySide ownerDisplaySide
	) {
		this.lookupHistory = lookupHistory;
		this.contents = contents;
		this.historyListConfig = historyListConfig;
		this.clientConfig = clientConfig;
		this.ownerDisplaySide = ownerDisplaySide;
	}

	public boolean isListDisplayed() {
		return clientConfig.lookupHistoryEnabled().get() &&
			isDisplayedOnThisSide() &&
			contents.hasRoom();
	}

	@Override
	public boolean isDisplayedOnThisSide() {
		return ownerDisplaySide.equals(clientConfig.lookupHistoryDisplaySide().get());
	}

	public IIngredientGridSource getLookupHistory() {
		return lookupHistory;
	}

	@Override
	public int getDisplayHeight() {
		return LookupHistoryOverlayLayout.getDisplayHeight(
			historyListConfig.maxRows().get(),
			historyListConfig.drawBackground().get(),
			historyListConfig.navigationMode().get().usesScrollbar()
		);
	}

	@Override
	public boolean isBackgroundEnabled() {
		return this.historyListConfig.drawBackground().get();
	}

	@Override
	public void updateBounds(
		ImmutableRect2i availableArea,
		Set<ImmutableRect2i> guiExclusionAreas,
		@Nullable ImmutablePoint2i mouseExclusionPoint
	) {
		this.guiExclusionAreas = guiExclusionAreas;
		this.pageAnchorElement = this.contents.getPageAnchorElement();
		this.contents.updateBounds(availableArea, guiExclusionAreas, mouseExclusionPoint);
	}

	@Override
	public void updateLayout() {
		this.contents.updateLayoutKeepingPageAnchorVisible(this.pageAnchorElement);
		this.pageAnchorElement = null;
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
			this.contents.drawForeground(minecraft, guiGraphics, mouseX, mouseY, partialTicks);
			if (!this.historyListConfig.drawBackground().get()) {
				ImmutableRect2i area = this.contents.getBackgroundArea();
				int color = JeiGuiColors.getColor(GuiColor.LOOKUP_HISTORY_LINE);
				ImmutableRect2i lineArea = new ImmutableRect2i(area.x(), area.y() - 3, area.width(), 1);
				drawLine(guiGraphics, lineArea, color);
			}
		}
	}

	public void drawTooltips(Minecraft minecraft, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		if (isListDisplayed()) {
			this.contents.drawTooltips(minecraft, guiGraphics, mouseX, mouseY);
		}
	}

	public void tick() {
		if (isListDisplayed()) {
			this.contents.tick();
		}
	}

	public ImmutableRect2i getArea() {
		return this.contents.getIngredientGridArea();
	}

	@Override
	public ImmutableRect2i getBackgroundArea() {
		return this.contents.getBackgroundArea();
	}

	public ImmutableRect2i getSlotBackgroundArea() {
		return this.contents.getSlotBackgroundArea();
	}

	@Override
	public void close() {
		this.guiExclusionAreas = Set.of();
		this.pageAnchorElement = null;
		this.contents.close();
	}

	public void drawOnForeground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		if (isListDisplayed()) {
			this.contents.drawOnForeground(guiGraphics, mouseX, mouseY);
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

	public IUserInputHandler createInputHandler() {
		return this.contents.createInputHandler();
	}

	public IDragHandler createDragHandler() {
		return this.contents.createDragHandler();
	}

	record LineSegment(int x1, int x2) {

	}
}
