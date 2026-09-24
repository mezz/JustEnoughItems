package mezz.jei.gui.config;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Rect2i;

/**
 * Draws the alignment icon used by JEI alignment controls.
 */
final class AlignmentGridRenderer {
	private static final int SELECTOR_INSET = 3;
	private static final float SELECTOR_SIZE_RATIO = 0.5F;
	private static final int SELECTED_COLOR = 0xFF8FB4DF;
	private static final int SELECTED_SHADOW_COLOR = 0xFF1E4F80;

	private AlignmentGridRenderer() {

	}

	public static void drawIcon(GuiGraphicsExtractor guiGraphics, Rect2i area, Alignment alignment) {
		int boundsX = area.getX() + SELECTOR_INSET;
		int boundsY = area.getY() + SELECTOR_INSET;
		int boundsWidth = Math.max(1, area.getWidth() - SELECTOR_INSET * 2);
		int boundsHeight = Math.max(1, area.getHeight() - SELECTOR_INSET * 2);
		int selectorWidth = Math.max(1, Math.round(boundsWidth * SELECTOR_SIZE_RATIO));
		int selectorHeight = Math.max(1, Math.round(boundsHeight * SELECTOR_SIZE_RATIO));

		int selectedX = getAlignedX(boundsX, boundsWidth, selectorWidth, alignment);
		int selectedY = getAlignedY(boundsY, boundsHeight, selectorHeight, alignment);
		guiGraphics.fill(selectedX - 1, selectedY - 1, selectedX + selectorWidth + 1, selectedY + selectorHeight + 1, SELECTED_SHADOW_COLOR);
		guiGraphics.fill(selectedX, selectedY, selectedX + selectorWidth, selectedY + selectorHeight, SELECTED_COLOR);
	}

	private static int getAlignedX(int x, int width, int selectorWidth, Alignment alignment) {
		return switch (alignment.horizontalAlignment()) {
			case LEFT -> x;
			case CENTER -> x + (width - selectorWidth) / 2;
			case RIGHT -> x + width - selectorWidth;
		};
	}

	private static int getAlignedY(int y, int height, int selectorHeight, Alignment alignment) {
		return switch (alignment.verticalAlignment()) {
			case TOP -> y;
			case CENTER -> y + (height - selectorHeight) / 2;
			case BOTTOM -> y + height - selectorHeight;
		};
	}

	static int getColumn(Alignment alignment) {
		return switch (alignment.horizontalAlignment()) {
			case LEFT -> 0;
			case CENTER -> 1;
			case RIGHT -> 2;
		};
	}

	static int getRow(Alignment alignment) {
		return switch (alignment.verticalAlignment()) {
			case TOP -> 0;
			case CENTER -> 1;
			case BOTTOM -> 2;
		};
	}
}
