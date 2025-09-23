package mezz.jei.test;

import mezz.jei.common.util.DeduplicatingRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public class DeduplicatingRunnerTest {
	/**
	 * The de-duplicating runner should only run one time.
	 * Spamming runs should be ignored if they are within the given duration.
	 */
	@Test
	public void testDeduplicatedRuns() {
		AtomicInteger runs = new AtomicInteger();
		Runnable testRunnable = runs::getAndIncrement;
		Duration delay = Duration.ofMillis(1000);
		TestDelayedExecutor executor = new TestDelayedExecutor();
		DeduplicatingRunner deduplicatingRunner = new DeduplicatingRunner(delay, executor);
		for (int i = 0; i < 100; i++) {
			deduplicatingRunner.run(testRunnable);
			executor.elapse(Duration.ofMillis(1));
			Assertions.assertEquals(0, runs.get());
		}

		executor.elapse(delay);
		Assertions.assertEquals(1, runs.get());
	}

	/**
	 * The de-duplicating runner should allow multiple runs if they are spaced out more than
	 * the given duration.
	 */
	@Test
	public void testSpacedOutRuns() {
		AtomicInteger runs = new AtomicInteger();
		Runnable testRunnable = runs::getAndIncrement;
		Duration delay = Duration.ofMillis(10);
		TestDelayedExecutor executor = new TestDelayedExecutor();
		DeduplicatingRunner deduplicatingRunner = new DeduplicatingRunner(delay, executor);
		for (int i = 0; i < 5; i++) {
			Assertions.assertEquals(i, runs.get());
			deduplicatingRunner.run(testRunnable);
			Assertions.assertEquals(i, runs.get());
			executor.elapse(delay);
			Assertions.assertEquals(i + 1, runs.get());
		}
	}
}
