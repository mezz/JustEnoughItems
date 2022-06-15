package mezz.jei.test;

import mezz.jei.common.util.TickTimer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TickTimerTest {
	@Test
	public void testBasicTickTimerMath() {
		int maxValue = 1000;
		int msPerCycle = 20;
		for (int i = 0; i < 1000; i++) {
			int expectedValue = (i % msPerCycle) * 50;
			int value = TickTimer.getValue(0, i, maxValue, msPerCycle, false);
			Assertions.assertEquals(expectedValue, value);

			int expectedDownValue = maxValue - expectedValue;
			int downValue = TickTimer.getValue(0, i, maxValue, msPerCycle, true);
			Assertions.assertEquals(expectedDownValue, downValue);
		}
	}

	@Test
	public void testMoreTicksThanValuesMath() {
		int maxValue = 4;
		int msPerCycle = 20;
		int[] expectedValues = new int[]{0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4};
		for (int i = 0; i < 1000; i++) {
			int expectedValue = expectedValues[i % msPerCycle];
			int value = TickTimer.getValue(0, i, maxValue, msPerCycle, false);
			Assertions.assertEquals(expectedValue, value);

			int expectedDownValue = maxValue - expectedValue;
			int downValue = TickTimer.getValue(0, i, maxValue, msPerCycle, true);
			Assertions.assertEquals(expectedDownValue, downValue);
		}
	}

	@Test
	public void testIndivisibleTicking() {
		int maxValue = 3;
		int msPerCycle = 10;
		int[] expectedValues = new int[]{0, 0, 0, 1, 1, 2, 2, 2, 3, 3};
		for (int i = 0; i < 1000; i++) {
			int expectedValue = expectedValues[i % msPerCycle];
			int value = TickTimer.getValue(0, i, maxValue, msPerCycle, false);
			Assertions.assertEquals(expectedValue, value);

			int expectedDownValue = maxValue - expectedValue;
			int downValue = TickTimer.getValue(0, i, maxValue, msPerCycle, true);
			Assertions.assertEquals(expectedDownValue, downValue);
		}
	}
}
