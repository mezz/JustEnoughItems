package mezz.jei.gui.elements;

import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.ImmutableSize2i;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ResizeDragTest {
	private static final ImmutableSize2i MINIMUM = new ImmutableSize2i(100, 100);
	private static final ImmutableSize2i MAXIMUM = new ImmutableSize2i(500, 400);

	@Test
	void grabbingInsideBorderDoesNotJumpAndCenteredCornerTracksBothEdges() {
		ImmutableRect2i area = new ImmutableRect2i(100, 100, 200, 200);
		ResizeDrag drag = new ResizeDrag(ResizeHandle.at(area, 297, 297), 297, 297, area.getSize());
		assertEquals(area.getSize(), drag.resize(297, 297, true, true, MINIMUM, MAXIMUM));
		assertEquals(new ImmutableSize2i(240, 220), drag.resize(317, 307, true, true, MINIMUM, MAXIMUM));
	}

	@Test
	void dragPastLimitCanReverseWithoutLosingOriginalGrabOffset() {
		ImmutableRect2i area = new ImmutableRect2i(100, 100, 200, 200);
		ResizeDrag drag = new ResizeDrag(ResizeHandle.at(area, 101, 101), 101, 101, area.getSize());
		assertEquals(MAXIMUM, drag.resize(-1000, -1000, true, true, MINIMUM, MAXIMUM));
		assertEquals(new ImmutableSize2i(220, 220), drag.resize(91, 91, true, true, MINIMUM, MAXIMUM));
		assertEquals(MINIMUM, drag.resize(1000, 1000, true, true, MINIMUM, MAXIMUM));
	}

	@Test
	void alignedPanelKeepsUntouchedAxisAndClampsToSmallerViewport() {
		ImmutableRect2i area = new ImmutableRect2i(100, 100, 200, 200);
		ResizeDrag drag = new ResizeDrag(ResizeHandle.at(area, 101, 150), 101, 150, area.getSize());
		assertEquals(new ImmutableSize2i(220, 200), drag.resize(81, -1000, false, false, MINIMUM, MAXIMUM));
		assertEquals(new ImmutableSize2i(80, 200), drag.resize(81, 150, false, false, MINIMUM, new ImmutableSize2i(80, 80)));
	}

	@Test
	void interiorAndEmptyPanelsCannotStartResizing() {
		ImmutableRect2i area = new ImmutableRect2i(100, 100, 200, 200);
		assertFalse(ResizeHandle.at(area, 150, 150).isPresent());
		assertFalse(ResizeHandle.at(area, 300, 150).isPresent());
		assertFalse(ResizeHandle.at(ImmutableRect2i.EMPTY, 0, 0).isPresent());
	}
}
