package mezz.jei.library.gui.widgets;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IScrollGridWidget;
import mezz.jei.api.gui.widgets.ISlottedRecipeWidget;
import mezz.jei.api.gui.widgets.ScrollbarVisibility;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.gui.GridScrollMath;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.ImmutableSize2i;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ScrollGridRecipeWidget extends AbstractScrollWidget implements IScrollGridWidget, ISlottedRecipeWidget, IJeiInputHandler {
	private final IDrawable slotBackground;
	private final int columns;
	private final int visibleRows;
	private final Consumer<List<IRecipeSlotDrawable>> claimSlots;
	private List<IRecipeSlotDrawable> slots;
	private @Nullable Placement placement;

	public static ImmutableSize2i calculateSize(int columns, int visibleRows) {
		IDrawable slotBackground = Internal.getTextures().getSlot();
		return new ImmutableSize2i(
			columns * slotBackground.getWidth() + getScrollBoxScrollbarExtraWidth(),
			visibleRows * slotBackground.getHeight()
		);
	}

	public static ScrollGridRecipeWidget create(List<IRecipeSlotDrawable> slots, int columns, int visibleRows) {
		return create(slots, columns, visibleRows, ignored -> {});
	}

	public static ScrollGridRecipeWidget create(List<IRecipeSlotDrawable> slots, int columns, int visibleRows, Consumer<List<IRecipeSlotDrawable>> claimSlots) {
		ImmutableSize2i size = calculateSize(columns, visibleRows);
		ImmutableRect2i area = new ImmutableRect2i(0, 0, size.width(), size.height());
		return new ScrollGridRecipeWidget(area, columns, visibleRows, slots, claimSlots);
	}

	public ScrollGridRecipeWidget(ImmutableRect2i area, int columns, int visibleRows, List<IRecipeSlotDrawable> slots) {
		this(area, columns, visibleRows, slots, ignored -> {});
	}

	private ScrollGridRecipeWidget(ImmutableRect2i area, int columns, int visibleRows, List<IRecipeSlotDrawable> slots, Consumer<List<IRecipeSlotDrawable>> claimSlots) {
		super(area);
		this.claimSlots = claimSlots;
		this.slotBackground = Internal.getTextures().getSlot();

		this.columns = columns;
		this.visibleRows = visibleRows;
		this.slots = List.copyOf(slots);
		claimSlots.accept(this.slots);
	}

	@Override
	public ScrollGridRecipeWidget setSlots(List<IRecipeSlotDrawable> slots) {
		this.slots = List.copyOf(slots);
		claimSlots.accept(this.slots);
		resetScroll();
		return this;
	}

	@Override
	public ScrollGridRecipeWidget setScrollbarVisibility(ScrollbarVisibility visibility) {
		super.setScrollbarVisibility(visibility);
		return this;
	}

	@Override
	protected ImmutableRect2i getWidgetArea() {
		ImmutableRect2i widgetArea = area;
		if (!isScrollbarVisible()) {
			widgetArea = widgetArea.cropRight(getScrollBoxScrollbarExtraWidth());
		}
		if (placement != null) {
			return placement.align(widgetArea);
		}
		return widgetArea;
	}

	private int getHiddenRows() {
		return GridScrollMath.getHiddenRows(slots.size(), columns, visibleRows);
	}

	@Override
	public ScrollGridRecipeWidget setPosition(int xPos, int yPos) {
		this.area = area.setPosition(xPos, yPos);
		this.placement = null;
		return this;
	}

	@Override
	public ScrollGridRecipeWidget setPosition(
		int areaX,
		int areaY,
		int areaWidth,
		int areaHeight,
		HorizontalAlignment horizontalAlignment,
		VerticalAlignment verticalAlignment
	) {
		this.placement = new Placement(new ImmutableRect2i(areaX, areaY, areaWidth, areaHeight), horizontalAlignment, verticalAlignment);
		return this;
	}

	@Override
	public int getWidth() {
		return getWidgetArea().width();
	}

	@Override
	public int getHeight() {
		return area.height();
	}

	@Override
	public ScreenRectangle getScreenRectangle() {
		return getArea();
	}

	@Override
	protected int getVisibleAmount() {
		if (isSmoothScrolling()) {
			return visibleRows * slotBackground.getHeight();
		}
		return visibleRows;
	}

	@Override
	protected int getHiddenAmount() {
		int hiddenRows = getHiddenRows();
		if (isSmoothScrolling()) {
			return hiddenRows * slotBackground.getHeight();
		}
		return hiddenRows;
	}

	@Override
	protected void drawContents(GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY, float scrollOffsetY) {
		final int totalSlots = slots.size();
		final int scrollPixelOffset = getScrollPixelOffset();

		final int slotWidth = slotBackground.getWidth();
		final int slotHeight = slotBackground.getHeight();
		final int firstRow = scrollPixelOffset / slotHeight;
		final int firstIndex = columns * firstRow;
		final int rowPixelOffset = scrollPixelOffset % slotHeight;
		int renderedRows = visibleRows;
		if (rowPixelOffset > 0) {
			renderedRows++;
		}

		if (rowPixelOffset > 0) {
			guiGraphics.enableScissor(
				contentsArea.x(),
				contentsArea.y(),
				contentsArea.x() + contentsArea.width(),
				contentsArea.y() + contentsArea.height()
			);
		}

		try {
			for (int row = 0; row < renderedRows; row++) {
				for (int column = 0; column < columns; column++) {
					final int slotIndex = firstIndex + (row * columns) + column;
					ImmutableRect2i slotArea = getSlotArea(column, row, rowPixelOffset);
					slotBackground.draw(guiGraphics, slotArea.x(), slotArea.y());
					if (slotIndex < totalSlots) {
						IRecipeSlotDrawable slot = slots.get(slotIndex);
						setSlotPosition(slot, slotArea);
						boolean hovered = isMouseOverSlot(slot, slotArea, mouseX, mouseY);
						slot.draw(guiGraphics, hovered);
					}
				}
			}
		} finally {
			if (rowPixelOffset > 0) {
				guiGraphics.disableScissor();
			}
		}
	}

	@Override
	public Optional<RecipeSlotUnderMouse> getSlotUnderMouse(double mouseX, double mouseY) {
		if (!contentsArea.contains(mouseX, mouseY)) {
			return Optional.empty();
		}
		final int firstRow = getFirstRow();
		final int startIndex = firstRow * columns;
		final int rowPixelOffset = getRowPixelOffset();
		int visibleRowCount = visibleRows;
		if (rowPixelOffset > 0) {
			visibleRowCount++;
		}
		final int endIndex = Math.min(startIndex + (visibleRowCount * columns), slots.size());
		for (int i = startIndex; i < endIndex; i++) {
			int visibleIndex = i - startIndex;
			int column = visibleIndex % columns;
			int row = visibleIndex / columns;
			ImmutableRect2i slotArea = getSlotArea(column, row, rowPixelOffset);
			IRecipeSlotDrawable slot = slots.get(i);
			setSlotPosition(slot, slotArea);
			if (isMouseOverSlot(slot, slotArea, mouseX, mouseY)) {
				return Optional.of(new RecipeSlotUnderMouse(slot, getPosition()));
			}
		}
		return Optional.empty();
	}

	private ImmutableRect2i getSlotArea(int column, int row, int rowPixelOffset) {
		int slotWidth = slotBackground.getWidth();
		int slotHeight = slotBackground.getHeight();
		return new ImmutableRect2i(
			column * slotWidth,
			(row * slotHeight) - rowPixelOffset,
			slotWidth,
			slotHeight
		);
	}

	private static void setSlotPosition(IRecipeSlotDrawable slot, ImmutableRect2i slotArea) {
		slot.setPosition(slotArea.x() + 1, slotArea.y() + 1);
	}

	private boolean isMouseOverSlot(IRecipeSlotDrawable slot, ImmutableRect2i slotArea, double mouseX, double mouseY) {
		return contentsArea.contains(mouseX, mouseY) &&
			(slotArea.contains(mouseX, mouseY) || slot.isMouseOver(mouseX, mouseY));
	}

	@Override
	protected float calculateScrollAmount(double scrollDeltaY) {
		int hiddenRows = getHiddenRows();
		if (isSmoothScrolling()) {
			int hiddenPixels = hiddenRows * slotBackground.getHeight();
			if (hiddenPixels == 0) {
				return 0;
			}
			IClientConfig clientConfig = Internal.getClientConfigs().getClientConfig();
			return (float) (scrollDeltaY * clientConfig.smoothScrollRate().get() / (double) hiddenPixels);
		}
		return (float) (scrollDeltaY / (double) hiddenRows);
	}

	private boolean isSmoothScrolling() {
		return Internal.getClientConfigs()
			.getClientConfig()
			.smoothScrollingEnabled()
			.get();
	}

	private int getScrollPixelOffset() {
		if (isSmoothScrolling()) {
			return GridScrollMath.getSmoothScrollPixelOffset(getHiddenRows(), slotBackground.getHeight(), getScrollOffsetY());
		}
		return getFirstRow() * slotBackground.getHeight();
	}

	private int getFirstRow() {
		if (isSmoothScrolling()) {
			return GridScrollMath.getFirstRowForSmoothScrollPixelOffset(getScrollPixelOffset(), slotBackground.getHeight());
		}
		return GridScrollMath.getFirstRowForScrollOffset(getHiddenRows(), getScrollOffsetY());
	}

	private int getRowPixelOffset() {
		return getScrollPixelOffset() % slotBackground.getHeight();
	}

	private record Placement(ImmutableRect2i area, HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
		private ImmutableRect2i align(ImmutableRect2i widgetArea) {
			int x = area.x() + horizontalAlignment.getXPos(area.width(), widgetArea.width());
			int y = area.y() + verticalAlignment.getYPos(area.height(), widgetArea.height());
			return widgetArea.setPosition(x, y);
		}
	}
}
