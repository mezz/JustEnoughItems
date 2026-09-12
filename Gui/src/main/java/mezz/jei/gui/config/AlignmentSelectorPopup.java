package mezz.jei.gui.config;

import net.mezzdev.config.gui.api.IConfigValuePopup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;

import java.util.Optional;

final class AlignmentSelectorPopup implements IConfigValuePopup<Alignment> {
	private static final int BORDER_SIZE = 1;
	private static final int CELL_SIZE = 20;
	private static final int CELL_GAP = 1;
	private static final int GRID_SIZE = 3;
	private static final int AREA_SIZE = CELL_SIZE * GRID_SIZE + CELL_GAP * (GRID_SIZE - 1) + BORDER_SIZE * 2;
	private static final int BACKGROUND_COLOR = 0xF0101218;
	private static final int CELL_BACKGROUND_COLOR = 0xAA1A1D24;
	private static final int CELL_HOVER_COLOR = 0xFF313A46;
	private static final int CELL_SELECTED_COLOR = 0xFF365B82;
	private static final int CELL_SELECTED_HOVER_COLOR = 0xFF47739F;
	private static final int BORDER_DARK_COLOR = 0xE0000000;
	private static final int BORDER_LIGHT_COLOR = 0x45FFFFFF;
	private static final int DIVIDER_COLOR = 0x22FFFFFF;

	private final Alignment selectedAlignment;

	AlignmentSelectorPopup(Alignment selectedAlignment) {
		this.selectedAlignment = selectedAlignment;
	}

	@Override
	public int getWidth() {
		return AREA_SIZE;
	}

	@Override
	public int getHeight() {
		return AREA_SIZE;
	}

	@Override
	public Optional<Alignment> getHoveredValue(Rect2i area, double mouseX, double mouseY) {
		if (isEmpty(area)) {
			return Optional.empty();
		}
		for (Alignment alignment : Alignment.values()) {
			if (getCellArea(area, alignment).contains((int) mouseX, (int) mouseY)) {
				return Optional.of(alignment);
			}
		}
		return Optional.empty();
	}

	@Override
	public void draw(GuiGraphics guiGraphics, Rect2i area, double mouseX, double mouseY) {
		if (isEmpty(area)) {
			return;
		}
		drawBackground(guiGraphics, area);
		for (Alignment alignment : Alignment.values()) {
			Rect2i cellArea = getCellArea(area, alignment);
			boolean hovered = cellArea.contains((int) mouseX, (int) mouseY);
			boolean selected = alignment == selectedAlignment;
			int color = getCellColor(selected, hovered);
			guiGraphics.fill(
				cellArea.getX(),
				cellArea.getY(),
				cellArea.getX() + cellArea.getWidth(),
				cellArea.getY() + cellArea.getHeight(),
				color
			);
		}
	}

	private static void drawBackground(GuiGraphics guiGraphics, Rect2i area) {
		int x = area.getX();
		int y = area.getY();
		int right = x + area.getWidth();
		int bottom = y + area.getHeight();
		guiGraphics.fill(x, y, right, bottom, BACKGROUND_COLOR);
		guiGraphics.fill(x, y, right, y + 1, BORDER_DARK_COLOR);
		guiGraphics.fill(x, y, x + 1, bottom, BORDER_DARK_COLOR);
		guiGraphics.fill(right - 1, y, right, bottom, BORDER_LIGHT_COLOR);
		guiGraphics.fill(x, bottom - 1, right, bottom, BORDER_LIGHT_COLOR);

		for (int i = 1; i < GRID_SIZE; i++) {
			int dividerX = area.getX() + BORDER_SIZE + i * CELL_SIZE + (i - 1) * CELL_GAP;
			int dividerY = area.getY() + BORDER_SIZE + i * CELL_SIZE + (i - 1) * CELL_GAP;
			guiGraphics.fill(dividerX, area.getY() + BORDER_SIZE, dividerX + CELL_GAP, bottom - BORDER_SIZE, DIVIDER_COLOR);
			guiGraphics.fill(area.getX() + BORDER_SIZE, dividerY, right - BORDER_SIZE, dividerY + CELL_GAP, DIVIDER_COLOR);
		}
	}

	private static Rect2i getCellArea(Rect2i area, Alignment alignment) {
		int x = area.getX() + BORDER_SIZE + AlignmentGridRenderer.getColumn(alignment) * (CELL_SIZE + CELL_GAP);
		int y = area.getY() + BORDER_SIZE + AlignmentGridRenderer.getRow(alignment) * (CELL_SIZE + CELL_GAP);
		return new Rect2i(x, y, CELL_SIZE, CELL_SIZE);
	}

	private static int getCellColor(boolean selected, boolean hovered) {
		if (selected && hovered) {
			return CELL_SELECTED_HOVER_COLOR;
		}
		if (selected) {
			return CELL_SELECTED_COLOR;
		}
		if (hovered) {
			return CELL_HOVER_COLOR;
		}
		return CELL_BACKGROUND_COLOR;
	}

	private static boolean isEmpty(Rect2i area) {
		return area.getWidth() <= 0 || area.getHeight() <= 0;
	}
}
