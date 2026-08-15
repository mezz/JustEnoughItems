package mezz.jei.common.gui.elements;

import mezz.jei.api.gui.drawable.IScalableDrawable;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.gui.GuiGraphics;

public final class Scrollbar {
	public static final int WIDTH = 14;
	private static final int MIN_MARKER_HEIGHT = 14;
	private static final int BORDER_SIZE = 1;

	private final IScalableDrawable background;
	private final IScalableDrawable marker;
	private ImmutableRect2i area;
	private double dragOriginY = -1;

	public Scrollbar(ImmutableRect2i area) {
		this(area, Internal.getTextures());
	}

	private Scrollbar(ImmutableRect2i area, Textures textures) {
		this(area, textures.getScrollbarBackground(), textures.getScrollbarMarker());
	}

	Scrollbar(ImmutableRect2i area, IScalableDrawable background, IScalableDrawable marker) {
		this.area = area;
		this.background = background;
		this.marker = marker;
	}

	public void updateBounds(ImmutableRect2i area) {
		this.area = area;
	}

	public ImmutableRect2i getArea() {
		return area;
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return area.contains(mouseX, mouseY);
	}

	public boolean isDragging() {
		return dragOriginY >= 0;
	}

	public void stopDrag() {
		this.dragOriginY = -1;
	}

	public void draw(GuiGraphics guiGraphics, int visibleAmount, int hiddenAmount, float scrollOffsetY) {
		if (area.isEmpty()) {
			return;
		}
		this.background.draw(guiGraphics, area.x(), area.y(), area.width(), area.height());
		ImmutableRect2i markerArea = calculateMarkerArea(area, visibleAmount, hiddenAmount, scrollOffsetY);
		this.marker.draw(guiGraphics, markerArea.x(), markerArea.y(), markerArea.width(), markerArea.height());
	}

	public ScrollResult startDrag(
		double mouseX,
		double mouseY,
		int visibleAmount,
		int hiddenAmount,
		float scrollOffsetY
	) {
		if (hiddenAmount <= 0 || !isMouseOver(mouseX, mouseY)) {
			return ScrollResult.notHandled(scrollOffsetY);
		}

		float updatedScrollOffsetY = scrollOffsetY;
		ImmutableRect2i markerArea = calculateMarkerArea(area, visibleAmount, hiddenAmount, updatedScrollOffsetY);
		if (!markerArea.contains(mouseX, mouseY)) {
			double markerTopY = mouseY - (markerArea.height() / 2.0);
			updatedScrollOffsetY = calculateScrollOffsetY(area, markerArea, markerTopY, updatedScrollOffsetY);
			markerArea = calculateMarkerArea(area, visibleAmount, hiddenAmount, updatedScrollOffsetY);
		}
		this.dragOriginY = mouseY - markerArea.y();
		return ScrollResult.handled(updatedScrollOffsetY);
	}

	public ScrollResult dragTo(double mouseY, int visibleAmount, int hiddenAmount, float scrollOffsetY) {
		if (!isDragging()) {
			return ScrollResult.notHandled(scrollOffsetY);
		}

		ImmutableRect2i markerArea = calculateMarkerArea(area, visibleAmount, hiddenAmount, scrollOffsetY);
		double markerTopY = mouseY - this.dragOriginY;
		float updatedScrollOffsetY = calculateScrollOffsetY(area, markerArea, markerTopY, scrollOffsetY);
		return ScrollResult.handled(updatedScrollOffsetY);
	}

	static ImmutableRect2i calculateMarkerArea(ImmutableRect2i area, int visibleAmount, int hiddenAmount, float scrollOffsetY) {
		int trackWidth = Math.max(0, area.width() - (2 * BORDER_SIZE));
		int trackHeight = Math.max(0, area.height() - (2 * BORDER_SIZE));
		int markerHeight = trackHeight;
		int totalAmount = Math.max(0, visibleAmount) + Math.max(0, hiddenAmount);
		if (hiddenAmount > 0 && totalAmount > 0) {
			markerHeight = Math.round(trackHeight * (visibleAmount / (float) totalAmount));
			int minMarkerHeight = Math.min(MIN_MARKER_HEIGHT, trackHeight);
			markerHeight = Math.clamp(markerHeight, minMarkerHeight, trackHeight);
		}
		float validScrollOffsetY = Math.clamp(scrollOffsetY, 0, 1);
		int markerY = Math.round((trackHeight - markerHeight) * validScrollOffsetY);
		return new ImmutableRect2i(
			area.x() + BORDER_SIZE,
			area.y() + BORDER_SIZE + markerY,
			trackWidth,
			markerHeight
		);
	}

	private static float calculateScrollOffsetY(
		ImmutableRect2i area,
		ImmutableRect2i markerArea,
		double markerTopY,
		float fallbackScrollOffsetY
	) {
		int minY = area.y() + BORDER_SIZE;
		int trackHeight = Math.max(0, area.height() - (2 * BORDER_SIZE));
		int maxY = minY + trackHeight - markerArea.height();
		int travel = maxY - minY;
		if (travel <= 0) {
			return Math.clamp(fallbackScrollOffsetY, 0, 1);
		}
		float scrollOffsetY = (float) ((markerTopY - minY) / travel);
		return Math.clamp(scrollOffsetY, 0, 1);
	}

	public record ScrollResult(boolean handled, float scrollOffsetY) {
		private static ScrollResult handled(float scrollOffsetY) {
			return new ScrollResult(true, scrollOffsetY);
		}

		private static ScrollResult notHandled(float scrollOffsetY) {
			return new ScrollResult(false, scrollOffsetY);
		}
	}
}
