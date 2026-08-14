package mezz.jei.common.gui;

import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.drawable.IScalableDrawable;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.List;

public abstract class IngredientGridTooltipComponent<T> implements ClientTooltipComponent, TooltipComponent {
	private static final int CELL_SIZE = 18;
	private static final int GRID_PADDING = 1;
	private static final int BOTTOM_PADDING = 2;
	private static final int SCROLLBAR_WIDTH = 14;
	private static final int SCROLLBAR_GAP = 2;
	private static final int MIN_SCROLL_MARKER_HEIGHT = 14;
	private static final int MAX_COLUMNS = 10;
	private static final int MAX_ROWS = 4;
	private static final int MAX_WIDTH = (2 * GRID_PADDING) + (MAX_COLUMNS * CELL_SIZE) + SCROLLBAR_GAP + SCROLLBAR_WIDTH;

	private final IScalableDrawable scrollbarBackground;
	private final IScalableDrawable scrollbarMarker;
	private final IDrawableStatic slotBackground;
	private final List<T> ingredients;
	private final int columns;
	private final int visibleRows;
	private final int totalRows;
	private final int maxRowOffset;
	private final int width;
	private final int height;

	private ImmutableRect2i area = ImmutableRect2i.EMPTY;
	private ImmutableRect2i scrollbarArea = ImmutableRect2i.EMPTY;
	private ImmutableRect2i scrollbarMarkerArea = ImmutableRect2i.EMPTY;
	private int rowOffset;
	private double mouseX;
	private double mouseY;
	private boolean mousePositionSet;
	private double scrollbarDragOriginY = -1;

	protected IngredientGridTooltipComponent(List<T> ingredients) {
		this.ingredients = ingredients;
		int count = ingredients.size();
		this.columns = Math.min(count, MAX_COLUMNS);
		this.totalRows = MathUtil.divideCeil(count, this.columns);
		this.visibleRows = Math.min(this.totalRows, MAX_ROWS);
		this.maxRowOffset = Math.max(0, this.totalRows - MAX_ROWS);

		int scrollbarSpace = 0;
		if (this.maxRowOffset > 0) {
			scrollbarSpace = SCROLLBAR_GAP + SCROLLBAR_WIDTH;
		}
		this.width = (2 * GRID_PADDING) + (this.columns * CELL_SIZE) + scrollbarSpace;
		this.height = (2 * GRID_PADDING) + (this.visibleRows * CELL_SIZE) + BOTTOM_PADDING;

		Textures textures = Internal.getTextures();
		this.scrollbarBackground = textures.getScrollbarBackground();
		this.scrollbarMarker = textures.getScrollbarMarker();
		this.slotBackground = textures.getSlot();
	}

	static int getMaximumWidth() {
		return MAX_WIDTH;
	}

	@Override
	public int getHeight() {
		return this.height;
	}

	@Override
	public int getWidth(Font font) {
		return this.width;
	}

	@Override
	public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
		this.area = new ImmutableRect2i(x, y, this.width, this.height - BOTTOM_PADDING);
		int gridX = x + GRID_PADDING;
		int gridY = y + GRID_PADDING;

		int firstIndex = this.rowOffset * this.columns;
		int lastIndex = Math.min(firstIndex + (this.visibleRows * this.columns), this.ingredients.size());
		int hoveredIndex = -1;
		if (this.mousePositionSet) {
			hoveredIndex = getIngredientIndexUnderMouse(this.mouseX, this.mouseY);
		}
		for (int i = firstIndex; i < lastIndex; i++) {
			int column = i % this.columns;
			int displayRow = (i / this.columns) - this.rowOffset;
			int cellX = gridX + (column * CELL_SIZE);
			int cellY = gridY + (displayRow * CELL_SIZE);
			this.slotBackground.draw(guiGraphics, cellX, cellY);
			drawIngredient(guiGraphics, this.ingredients.get(i), i, cellX + 1, cellY + 1, i == hoveredIndex);
		}

		if (this.maxRowOffset > 0) {
			drawScrollbar(guiGraphics, gridX, gridY);
		}
	}

	protected abstract void drawIngredient(
		GuiGraphics guiGraphics,
		T ingredient,
		int index,
		int x,
		int y,
		boolean hovered
	);

	public void setMousePosition(double mouseX, double mouseY) {
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.mousePositionSet = true;
	}

	public boolean mouseScrolled(double scrollDeltaY) {
		if (scrollDeltaY == 0) {
			return false;
		}
		int delta = 1;
		if (scrollDeltaY > 0) {
			delta = -1;
		}
		int newRowOffset = Math.max(0, Math.min(this.rowOffset + delta, this.maxRowOffset));
		if (newRowOffset == this.rowOffset) {
			return false;
		}
		this.rowOffset = newRowOffset;
		return true;
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return this.area.contains(mouseX, mouseY);
	}

	public boolean isMouseOverScrollbar(double mouseX, double mouseY) {
		return this.scrollbarArea.contains(mouseX, mouseY);
	}

	public boolean isDraggingScrollbar() {
		return this.scrollbarDragOriginY >= 0;
	}

	public boolean startScrollbarDrag(double mouseX, double mouseY) {
		if (!isMouseOverScrollbar(mouseX, mouseY)) {
			return false;
		}
		if (!this.scrollbarMarkerArea.contains(mouseX, mouseY)) {
			moveScrollbarCenterTo(mouseY);
			updateScrollbarMarkerArea();
		}
		this.scrollbarDragOriginY = mouseY - this.scrollbarMarkerArea.y();
		return true;
	}

	public boolean mouseDragged(double mouseY) {
		if (!isDraggingScrollbar()) {
			return false;
		}
		double markerTopY = mouseY - this.scrollbarDragOriginY;
		moveScrollbarTo(markerTopY);
		return true;
	}

	public void stopScrollbarDrag() {
		this.scrollbarDragOriginY = -1;
	}

	protected int getIngredientIndexUnderMouse(double mouseX, double mouseY) {
		if (this.area.getWidth() == 0 || this.area.getHeight() == 0) {
			return -1;
		}
		int localX = (int) mouseX - (this.area.getX() + GRID_PADDING);
		int localY = (int) mouseY - (this.area.getY() + GRID_PADDING);
		if (localX < 0 || localY < 0) {
			return -1;
		}
		int column = localX / CELL_SIZE;
		int row = localY / CELL_SIZE;
		if (column >= this.columns || row >= this.visibleRows) {
			return -1;
		}
		int index = ((this.rowOffset + row) * this.columns) + column;
		if (index >= this.ingredients.size()) {
			return -1;
		}
		return index;
	}

	protected T getIngredient(int index) {
		return this.ingredients.get(index);
	}

	private void drawScrollbar(GuiGraphics guiGraphics, int gridX, int gridY) {
		int scrollAreaX = gridX + (this.columns * CELL_SIZE) + SCROLLBAR_GAP;
		this.scrollbarArea = new ImmutableRect2i(scrollAreaX, gridY, SCROLLBAR_WIDTH, this.visibleRows * CELL_SIZE);
		this.scrollbarBackground.draw(
			guiGraphics,
			this.scrollbarArea.getX(),
			this.scrollbarArea.getY(),
			this.scrollbarArea.getWidth(),
			this.scrollbarArea.getHeight()
		);
		updateScrollbarMarkerArea();
		this.scrollbarMarker.draw(
			guiGraphics,
			this.scrollbarMarkerArea.getX(),
			this.scrollbarMarkerArea.getY(),
			this.scrollbarMarkerArea.getWidth(),
			this.scrollbarMarkerArea.getHeight()
		);
	}

	private void updateScrollbarMarkerArea() {
		int totalSpace = this.scrollbarArea.getHeight() - 2;
		int scrollMarkerWidth = this.scrollbarArea.getWidth() - 2;
		int minMarkerHeight = Math.min(MIN_SCROLL_MARKER_HEIGHT, totalSpace);
		int markerHeight = Math.max(Math.round(totalSpace * (this.visibleRows / (float) this.totalRows)), minMarkerHeight);
		float scrollFraction = this.rowOffset / (float) this.maxRowOffset;
		int markerY = Math.round((totalSpace - markerHeight) * scrollFraction);
		this.scrollbarMarkerArea = new ImmutableRect2i(
			this.scrollbarArea.getX() + 1,
			this.scrollbarArea.getY() + 1 + markerY,
			scrollMarkerWidth,
			markerHeight
		);
	}

	private void moveScrollbarCenterTo(double centerY) {
		double markerTopY = centerY - (this.scrollbarMarkerArea.height() / 2.0);
		moveScrollbarTo(markerTopY);
	}

	private void moveScrollbarTo(double markerTopY) {
		int minY = this.scrollbarArea.y() + 1;
		int maxY = this.scrollbarArea.y() + this.scrollbarArea.height() - 1 - this.scrollbarMarkerArea.height();
		int totalSpace = maxY - minY;
		if (totalSpace <= 0) {
			return;
		}
		float scrollFraction = (float) ((markerTopY - minY) / totalSpace);
		float clampedScrollFraction = Math.max(0, Math.min(scrollFraction, 1));
		this.rowOffset = Math.round(clampedScrollFraction * this.maxRowOffset);
	}
}
