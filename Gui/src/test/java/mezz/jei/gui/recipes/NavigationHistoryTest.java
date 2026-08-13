package mezz.jei.gui.recipes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NavigationHistoryTest {
	@Test
	public void navigatesBackwardAndForward() {
		NavigationHistory<String> history = new NavigationHistory<>();
		history.record("first");
		history.record("second");

		String current = history.goBack("third").orElseThrow();
		assertEquals("second", current);

		current = history.goBack(current).orElseThrow();
		assertEquals("first", current);

		current = history.goForward(current).orElseThrow();
		assertEquals("second", current);

		current = history.goForward(current).orElseThrow();
		assertEquals("third", current);
	}

	@Test
	public void recordingAfterGoingBackClearsForwardHistory() {
		NavigationHistory<String> history = new NavigationHistory<>();
		history.record("first");
		history.record("second");

		String current = history.goBack("third").orElseThrow();
		history.record(current);

		assertTrue(history.goForward("new branch").isEmpty());
		assertEquals("second", history.goBack("new branch").orElseThrow());
	}

	@Test
	public void clearRemovesBackwardAndForwardHistory() {
		NavigationHistory<String> history = new NavigationHistory<>();
		history.record("first");
		String current = history.goBack("second").orElseThrow();

		history.clear();

		assertTrue(history.goBack(current).isEmpty());
		assertTrue(history.goForward(current).isEmpty());
	}
}
