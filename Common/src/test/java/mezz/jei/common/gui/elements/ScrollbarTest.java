package mezz.jei.common.gui.elements;

import mezz.jei.common.util.ImmutableRect2i;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ScrollbarTest {
	private static final ImmutableRect2i AREA = new ImmutableRect2i(10, 20, Scrollbar.WIDTH, 74);

	@Test
	public void markerPositionTracksNormalizedScrollOffset() {
		assertEquals(
			new ImmutableRect2i(11, 21, 12, 36),
			Scrollbar.calculateMarkerArea(AREA, 4, 4, 0)
		);
		assertEquals(
			new ImmutableRect2i(11, 39, 12, 36),
			Scrollbar.calculateMarkerArea(AREA, 4, 4, 0.5f)
		);
		assertEquals(
			new ImmutableRect2i(11, 57, 12, 36),
			Scrollbar.calculateMarkerArea(AREA, 4, 4, 1)
		);
	}

	@Test
	public void markerStaysInsideShortScrollbars() {
		ImmutableRect2i shortArea = new ImmutableRect2i(10, 20, Scrollbar.WIDTH, 10);

		ImmutableRect2i markerArea = Scrollbar.calculateMarkerArea(shortArea, 1, 100, 1);

		assertEquals(new ImmutableRect2i(11, 21, 12, 8), markerArea);
	}

	@Test
	public void trackClickAndDragUseTheSameInnerBoundsAsMarkerRendering() {
		Scrollbar scrollbar = createScrollbar();

		Scrollbar.ScrollResult clickResult = scrollbar.startDrag(12, 93, 4, 4, 0);

		assertTrue(clickResult.handled());
		assertEquals(1, clickResult.scrollOffsetY());
		assertTrue(scrollbar.isDragging());

		Scrollbar.ScrollResult dragResult = scrollbar.dragTo(21, 4, 4, clickResult.scrollOffsetY());

		assertTrue(dragResult.handled());
		assertEquals(0, dragResult.scrollOffsetY());
	}

	@Test
	public void scrollbarWithoutHiddenContentDoesNotStartDragging() {
		Scrollbar scrollbar = createScrollbar();

		Scrollbar.ScrollResult result = scrollbar.startDrag(12, 40, 4, 0, 0);

		assertFalse(result.handled());
		assertFalse(scrollbar.isDragging());
	}

	private static Scrollbar createScrollbar() {
		return new Scrollbar(AREA, DrawableBlank.EMPTY, DrawableBlank.EMPTY);
	}
}
