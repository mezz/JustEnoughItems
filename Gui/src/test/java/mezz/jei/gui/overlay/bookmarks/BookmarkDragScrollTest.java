package mezz.jei.gui.overlay.bookmarks;

import mezz.jei.common.config.IngredientGridNavigationMode;
import mezz.jei.common.util.ImmutableRect2i;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static mezz.jei.common.config.IngredientGridNavigationMode.PAGED;
import static mezz.jei.common.config.IngredientGridNavigationMode.SCROLLING;
import static mezz.jei.common.config.IngredientGridNavigationMode.SMOOTH_SCROLLING;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BookmarkDragScrollTest {
	private static final ImmutableRect2i AREA = new ImmutableRect2i(10, 20, 100, 100);
	private final AtomicLong nanoTime = new AtomicLong();
	private final BookmarkDragScroll scroll = new BookmarkDragScroll(nanoTime::get);

	@Test
	public void smoothScrollingAcceleratesTowardEachVerticalEdge() {
		assertEquals(3, advance(50, SMOOTH_SCROLLING, 50, 110));
		assertEquals(6, advance(50, SMOOTH_SCROLLING, 50, 120));
		assertEquals(-3, advance(50, SMOOTH_SCROLLING, 50, 30));
		assertEquals(-6, advance(50, SMOOTH_SCROLLING, 50, 20));
		assertEquals(6, advance(50, SMOOTH_SCROLLING, 50, 140));
	}

	@Test
	public void rowScrollingAccumulatesSmallStepsAndRestartsWhenReversing() {
		assertEquals(0, advance(50, SCROLLING, 50, 120));
		assertEquals(0, advance(50, SCROLLING, 50, 120));
		assertEquals(18, advance(50, SCROLLING, 50, 120));
		assertEquals(0, advance(50, SCROLLING, 50, 120));

		assertEquals(0, advance(50, SCROLLING, 50, 20));
		assertEquals(0, advance(50, SCROLLING, 50, 20));
		assertEquals(-18, advance(50, SCROLLING, 50, 20));
	}

	@Test
	public void leavingTheEdgeDiscardsPartialRowMovement() {
		assertEquals(0, advance(50, SCROLLING, 50, 120));
		assertEquals(0, advance(50, SCROLLING, 50, 120));
		assertEquals(0, advance(50, SCROLLING, 50, 70));
		assertEquals(0, advance(50, SCROLLING, 50, 120));
		assertEquals(0, advance(50, SCROLLING, 50, 120));
		assertEquals(18, advance(50, SCROLLING, 50, 120));
	}

	@Test
	public void pagedModeAndPositionsOutsideTheListDoNotScroll() {
		assertEquals(0, advance(50, PAGED, 50, 120));
		assertEquals(0, advance(50, SMOOTH_SCROLLING, 9, 120));
		assertEquals(0, advance(50, SMOOTH_SCROLLING, 110, 20));
		assertEquals(0, advance(50, SMOOTH_SCROLLING, 10, 70));
		assertEquals(0, scroll.update(ImmutableRect2i.EMPTY, SMOOTH_SCROLLING, 0, 0));
	}

	@Test
	public void speedDoesNotDependOnFrameRate() {
		double manyFrames = 0;
		for (int i = 0; i < 10; i++) {
			manyFrames += advance(10, SMOOTH_SCROLLING, 50, 120);
		}
		double fewFrames = advance(50, SMOOTH_SCROLLING, 50, 120) + advance(50, SMOOTH_SCROLLING, 50, 120);
		assertEquals(12, manyFrames, 0.0001);
		assertEquals(manyFrames, fewFrames, 0.0001);
	}

	@Test
	public void stalledFramesDoNotCauseLargeJumps() {
		assertEquals(6, advance(5_000, SMOOTH_SCROLLING, 50, 120));
	}

	@Test
	public void shortListsHaveSeparateTopAndBottomEdgeZones() {
		ImmutableRect2i shortArea = new ImmutableRect2i(10, 20, 100, 18);
		nanoTime.addAndGet(TimeUnit.MILLISECONDS.toNanos(50));
		assertEquals(0, scroll.update(shortArea, SMOOTH_SCROLLING, 50, 29));
		nanoTime.addAndGet(TimeUnit.MILLISECONDS.toNanos(50));
		assertEquals(6, scroll.update(shortArea, SMOOTH_SCROLLING, 50, 38));
	}

	private double advance(long milliseconds, IngredientGridNavigationMode mode, double mouseX, double mouseY) {
		nanoTime.addAndGet(TimeUnit.MILLISECONDS.toNanos(milliseconds));
		return scroll.update(AREA, mode, mouseX, mouseY);
	}
}
