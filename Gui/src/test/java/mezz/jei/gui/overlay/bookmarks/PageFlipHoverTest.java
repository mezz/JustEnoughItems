package mezz.jei.gui.overlay.bookmarks;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PageFlipHoverTest {
	@Test
	public void hoverDoesNotFlipBeforeTheDelay() {
		// Setup: a controllable clock and a hover tracker.
		AtomicLong currentTimeMillis = new AtomicLong(1_000);
		PageFlipHover pageFlipHover = new PageFlipHover(currentTimeMillis::get);

		// Operation: hover over the next-page button for just under the delay.
		assertNull(pageFlipHover.update(PageFlipHover.Button.NEXT));
		currentTimeMillis.set(1_000 + PageFlipHover.FLIP_DELAY_MS - 1);
		assertNull(pageFlipHover.update(PageFlipHover.Button.NEXT));

		// Assertions: no page flip is requested before the delay elapses.
	}

	@Test
	public void hoverFlipsAfterTheDelayAndRestartsIt() {
		// Setup: a hover has already been running for the full delay.
		AtomicLong currentTimeMillis = new AtomicLong(1_000);
		PageFlipHover pageFlipHover = new PageFlipHover(currentTimeMillis::get);
		assertNull(pageFlipHover.update(PageFlipHover.Button.NEXT));
		currentTimeMillis.set(1_000 + PageFlipHover.FLIP_DELAY_MS);

		// Operation: keep hovering past the delay, then shortly after the flip.
		assertEquals(PageFlipHover.Button.NEXT, pageFlipHover.update(PageFlipHover.Button.NEXT));
		assertNull(pageFlipHover.update(PageFlipHover.Button.NEXT));

		// Assertions: the delay restarts after a flip, so continuing to hover flips repeatedly.
		currentTimeMillis.set(1_000 + 2 * PageFlipHover.FLIP_DELAY_MS);
		assertEquals(PageFlipHover.Button.NEXT, pageFlipHover.update(PageFlipHover.Button.NEXT));
	}

	@Test
	public void leavingTheButtonResetsTheDelay() {
		// Setup: a hover has nearly reached the delay.
		AtomicLong currentTimeMillis = new AtomicLong(1_000);
		PageFlipHover pageFlipHover = new PageFlipHover(currentTimeMillis::get);
		assertNull(pageFlipHover.update(PageFlipHover.Button.NEXT));
		currentTimeMillis.set(1_000 + PageFlipHover.FLIP_DELAY_MS - 1);
		assertNull(pageFlipHover.update(PageFlipHover.Button.NEXT));

		// Operation: leave the button and come back.
		assertNull(pageFlipHover.update(null));
		currentTimeMillis.set(1_000 + PageFlipHover.FLIP_DELAY_MS);
		assertNull(pageFlipHover.update(PageFlipHover.Button.NEXT));

		// Assertions: the delay started over when the hover resumed.
		currentTimeMillis.set(1_000 + 2 * PageFlipHover.FLIP_DELAY_MS);
		assertEquals(PageFlipHover.Button.NEXT, pageFlipHover.update(PageFlipHover.Button.NEXT));
	}

	@Test
	public void switchingButtonsRestartsTheDelay() {
		// Setup: the next-page button has been hovered for the full delay.
		AtomicLong currentTimeMillis = new AtomicLong(1_000);
		PageFlipHover pageFlipHover = new PageFlipHover(currentTimeMillis::get);
		assertNull(pageFlipHover.update(PageFlipHover.Button.NEXT));
		currentTimeMillis.set(1_000 + PageFlipHover.FLIP_DELAY_MS);

		// Operation: move from the next-page button to the back-page button.
		assertNull(pageFlipHover.update(PageFlipHover.Button.BACK));

		// Assertions: the back-page button needs its own full delay before flipping.
		currentTimeMillis.set(1_000 + PageFlipHover.FLIP_DELAY_MS + PageFlipHover.FLIP_DELAY_MS - 1);
		assertNull(pageFlipHover.update(PageFlipHover.Button.BACK));
		currentTimeMillis.set(1_000 + 2 * PageFlipHover.FLIP_DELAY_MS);
		assertEquals(PageFlipHover.Button.BACK, pageFlipHover.update(PageFlipHover.Button.BACK));
	}

	@Test
	public void resetClearsTheHover() {
		// Setup: a hover is nearly at the delay.
		AtomicLong currentTimeMillis = new AtomicLong(1_000);
		PageFlipHover pageFlipHover = new PageFlipHover(currentTimeMillis::get);
		assertNull(pageFlipHover.update(PageFlipHover.Button.BACK));
		currentTimeMillis.set(1_000 + PageFlipHover.FLIP_DELAY_MS - 1);

		// Operation: reset the tracker, then hover again for a long time.
		pageFlipHover.reset();
		currentTimeMillis.set(1_000 + 10 * PageFlipHover.FLIP_DELAY_MS);
		assertNull(pageFlipHover.update(PageFlipHover.Button.BACK));

		// Assertions: the hover restarts from scratch after a reset.
		currentTimeMillis.set(1_000 + 11 * PageFlipHover.FLIP_DELAY_MS);
		assertEquals(PageFlipHover.Button.BACK, pageFlipHover.update(PageFlipHover.Button.BACK));
	}
}
