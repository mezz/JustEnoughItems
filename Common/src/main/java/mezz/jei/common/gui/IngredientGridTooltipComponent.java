package mezz.jei.common.gui;

import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.elements.Scrollbar;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.List;

public abstract class IngredientGridTooltipComponent<T> implements ClientTooltipComponent, TooltipComponent {
	private static final int CELL_SIZE = 18;
	private static final int GRID_PADDING = 1;
	private static final int BOTTOM_PADDING = 2;
	private static final int SCROLLBAR_GAP = 2;
	private static final int MAX_COLUMNS = 10;
	private static final int MAX_ROWS = 4;
	protected static final int MAX_VISIBLE_INGREDIENTS = MAX_COLUMNS * MAX_ROWS;
	private static final int MAX_WIDTH = (2 * GRID_PADDING) + (MAX_COLUMNS * CELL_SIZE) + SCROLLBAR_GAP + Scrollbar.WIDTH;

	private final Scrollbar scrollbar;
	private final IDrawableStatic slotBackground;
	private final List<T> ingredients;
	private final int columns;
	private final int visibleRows;
	private final int maxRowOffset;
	private final int width;
	private final int height;

	private ImmutableRect2i area = ImmutableRect2i.EMPTY;
	private float scrollOffsetY;
	private double mouseX;
	private double mouseY;
	private boolean mousePositionSet;

	protected IngredientGridTooltipComponent(List<T> ingredients) {
		this.ingredients = ingredients;
		int count = ingredients.size();
		this.columns = Math.min(count, MAX_COLUMNS);
		int totalRows = GridScrollMath.getTotalRows(count, this.columns);
		this.visibleRows = Math.min(totalRows, MAX_ROWS);
		this.maxRowOffset = GridScrollMath.getHiddenRows(count, this.columns, this.visibleRows);

		int scrollbarSpace = 0;
		if (this.maxRowOffset > 0) {
			scrollbarSpace = SCROLLBAR_GAP + Scrollbar.WIDTH;
		}
		this.width = (2 * GRID_PADDING) + (this.columns * CELL_SIZE) + scrollbarSpace;
		this.height = (2 * GRID_PADDING) + (this.visibleRows * CELL_SIZE) + BOTTOM_PADDING;

		this.scrollbar = new Scrollbar(ImmutableRect2i.EMPTY);
		this.slotBackground = Internal.getTextures().getSlot();
	}

	static int getMaximumWidth() {
		return MAX_WIDTH;
	}

	@Override
	public int getHeight(Font font) {
		return this.height;
	}

	@Override
	public int getWidth(Font font) {
		return this.width;
	}

	@Override
	public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor guiGraphics) {
		this.area = new ImmutableRect2i(x, y, this.width, this.height - BOTTOM_PADDING);
		int gridX = x + GRID_PADDING;
		int gridY = y + GRID_PADDING;

		int scrollPixelOffset = getScrollPixelOffset();
		int firstRow = scrollPixelOffset / CELL_SIZE;
		int rowPixelOffset = scrollPixelOffset % CELL_SIZE;
		int firstIndex = firstRow * this.columns;
		int renderedRows = this.visibleRows;
		if (rowPixelOffset > 0) {
			renderedRows++;
		}
		int lastIndex = Math.min(firstIndex + (renderedRows * this.columns), this.ingredients.size());
		int hoveredIndex = -1;
		if (this.mousePositionSet) {
			hoveredIndex = getIngredientIndexUnderMouse(this.mouseX, this.mouseY);
		}
		if (rowPixelOffset > 0) {
			guiGraphics.enableScissor(
				gridX,
				gridY,
				gridX + (this.columns * CELL_SIZE),
				gridY + (this.visibleRows * CELL_SIZE)
			);
		}
		try {
			for (int i = firstIndex; i < lastIndex; i++) {
				int column = i % this.columns;
				int displayRow = (i / this.columns) - firstRow;
				int cellX = gridX + (column * CELL_SIZE);
				int cellY = gridY + (displayRow * CELL_SIZE) - rowPixelOffset;
				this.slotBackground.draw(guiGraphics, cellX, cellY);
				drawIngredient(guiGraphics, this.ingredients.get(i), i, cellX + 1, cellY + 1, i == hoveredIndex);
			}
		} finally {
			if (rowPixelOffset > 0) {
				guiGraphics.disableScissor();
			}
		}

		if (this.maxRowOffset > 0) {
			drawScrollbar(guiGraphics, gridX, gridY);
		}
	}

	protected abstract void drawIngredient(
		GuiGraphicsExtractor guiGraphics,
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
		if (scrollDeltaY == 0 || this.maxRowOffset == 0) {
			return false;
		}
		float scrollAmount;
		if (isSmoothScrolling()) {
			int hiddenPixels = this.maxRowOffset * CELL_SIZE;
			int smoothScrollRate = Internal.getClientConfigs().getClientConfig().smoothScrollRate().get();
			scrollAmount = (float) (scrollDeltaY * smoothScrollRate / (double) hiddenPixels);
		} else {
			scrollAmount = (float) (scrollDeltaY / this.maxRowOffset);
		}
		float newScrollOffsetY = Math.clamp(this.scrollOffsetY - scrollAmount, 0, 1);
		if (Float.compare(newScrollOffsetY, this.scrollOffsetY) == 0) {
			return false;
		}
		this.scrollOffsetY = newScrollOffsetY;
		return true;
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return this.area.contains(mouseX, mouseY);
	}

	public boolean isMouseOverScrollbar(double mouseX, double mouseY) {
		return this.scrollbar.isMouseOver(mouseX, mouseY);
	}

	public boolean isDraggingScrollbar() {
		return this.scrollbar.isDragging();
	}

	public boolean startScrollbarDrag(double mouseX, double mouseY) {
		Scrollbar.ScrollResult result = this.scrollbar.startDrag(
			mouseX,
			mouseY,
			this.visibleRows,
			this.maxRowOffset,
			getScrollOffsetY()
		);
		if (result.handled()) {
			setScrollOffsetY(result.scrollOffsetY());
		}
		return result.handled();
	}

	public boolean mouseDragged(double mouseY) {
		Scrollbar.ScrollResult result = this.scrollbar.dragTo(
			mouseY,
			this.visibleRows,
			this.maxRowOffset,
			getScrollOffsetY()
		);
		if (result.handled()) {
			setScrollOffsetY(result.scrollOffsetY());
		}
		return result.handled();
	}

	public void stopScrollbarDrag() {
		this.scrollbar.stopDrag();
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
		int row = (localY + getScrollPixelOffset()) / CELL_SIZE;
		if (column >= this.columns || localY >= this.visibleRows * CELL_SIZE) {
			return -1;
		}
		int index = (row * this.columns) + column;
		if (index >= this.ingredients.size()) {
			return -1;
		}
		return index;
	}

	protected T getIngredient(int index) {
		return this.ingredients.get(index);
	}

	private void drawScrollbar(GuiGraphicsExtractor guiGraphics, int gridX, int gridY) {
		int scrollAreaX = gridX + (this.columns * CELL_SIZE) + SCROLLBAR_GAP;
		ImmutableRect2i scrollbarArea = new ImmutableRect2i(scrollAreaX, gridY, Scrollbar.WIDTH, this.visibleRows * CELL_SIZE);
		this.scrollbar.updateBounds(scrollbarArea);
		int amountMultiplier = 1;
		if (isSmoothScrolling()) {
			amountMultiplier = CELL_SIZE;
		}
		this.scrollbar.draw(
			guiGraphics,
			this.visibleRows * amountMultiplier,
			this.maxRowOffset * amountMultiplier,
			getScrollOffsetY()
		);
	}

	private float getScrollOffsetY() {
		if (this.maxRowOffset == 0) {
			return 0;
		}
		return this.scrollOffsetY;
	}

	private void setScrollOffsetY(float scrollOffsetY) {
		this.scrollOffsetY = Math.clamp(scrollOffsetY, 0, 1);
	}

	private boolean isSmoothScrolling() {
		return Internal.getClientConfigs()
			.getClientConfig()
			.smoothScrollingEnabled()
			.get();
	}

	private int getScrollPixelOffset() {
		if (isSmoothScrolling()) {
			return GridScrollMath.getSmoothScrollPixelOffset(this.maxRowOffset, CELL_SIZE, this.scrollOffsetY);
		}
		int rowOffset = GridScrollMath.getFirstRowForScrollOffset(this.maxRowOffset, this.scrollOffsetY);
		return rowOffset * CELL_SIZE;
	}
}
