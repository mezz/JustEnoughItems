package mezz.jei.test;

import mezz.jei.common.util.DeduplicatingRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public class DeduplicatingRunnerTest {
	@Test
	public void testDeduplicatedRuns() {
		// Setup: the deduplicating runner has one command repeatedly scheduled inside the debounce window.
		AtomicInteger runs = new AtomicInteger();
		Runnable testRunnable = runs::getAndIncrement;
		Duration delay = Duration.ofMillis(1000);
		TestDelayedExecutor executor = new TestDelayedExecutor();
		DeduplicatingRunner deduplicatingRunner = new DeduplicatingRunner(delay, executor);

		// Operation: schedule the command many times, then advance past the final scheduled delay.
		for (int i = 0; i < 100; i++) {
			deduplicatingRunner.run(testRunnable);
			executor.elapse(Duration.ofMillis(1));
		}
		executor.elapse(delay);

		// Assertions: the repeated calls collapse into a single delayed execution.
		Assertions.assertEquals(1, runs.get());
	}

	@Test
	public void testLatestRunReplacesPendingRun() {
		// Setup: the deduplicating runner accepts several different commands inside the debounce window.
		AtomicInteger value = new AtomicInteger();
		Duration delay = Duration.ofMillis(1000);
		TestDelayedExecutor executor = new TestDelayedExecutor();
		DeduplicatingRunner deduplicatingRunner = new DeduplicatingRunner(delay, executor);

		// Operation: replace the pending command twice before the delay expires.
		deduplicatingRunner.run(() -> value.set(1));
		executor.elapse(Duration.ofMillis(1));
		deduplicatingRunner.run(() -> value.set(2));
		executor.elapse(Duration.ofMillis(1));
		deduplicatingRunner.run(() -> value.set(3));
		executor.elapse(delay);

		// Assertions: only the latest command takes effect.
		Assertions.assertEquals(3, value.get(), "The latest scheduled command should replace earlier pending commands.");
	}

	@Test
	public void testReplacementRestartsDelay() {
		// Setup: one command is replaced shortly before its original delay expires.
		AtomicInteger value = new AtomicInteger();
		Duration delay = Duration.ofMillis(100);
		TestDelayedExecutor executor = new TestDelayedExecutor();
		DeduplicatingRunner deduplicatingRunner = new DeduplicatingRunner(delay, executor);

		// Operation: replace the pending command, then advance only to the original command's due time.
		deduplicatingRunner.run(() -> value.set(1));
		executor.elapse(Duration.ofMillis(99));
		deduplicatingRunner.run(() -> value.set(2));
		executor.elapse(Duration.ofMillis(1));

		// Assertions: the canceled command does not run at its old due time and the new delay starts from replacement.
		Assertions.assertEquals(0, value.get(), "Replacing a pending command should restart the delay.");

		// Operation: advance through the replacement command's remaining delay.
		executor.elapse(Duration.ofMillis(99));

		// Assertions: the replacement command runs after its own full delay.
		Assertions.assertEquals(2, value.get(), "The replacement command should run after its full delay.");
	}

	@Test
	public void testCommandDoesNotRunBeforeDelay() {
		// Setup: a command is scheduled with a known delay.
		AtomicInteger runs = new AtomicInteger();
		Duration delay = Duration.ofMillis(100);
		TestDelayedExecutor executor = new TestDelayedExecutor();
		DeduplicatingRunner deduplicatingRunner = new DeduplicatingRunner(delay, executor);

		// Operation: advance to just before the delay expires.
		deduplicatingRunner.run(runs::incrementAndGet);
		executor.elapse(Duration.ofMillis(99));

		// Assertions: commands do not run early.
		Assertions.assertEquals(0, runs.get(), "The command should not run before the configured delay.");

		// Operation: advance through the final millisecond of the delay.
		executor.elapse(Duration.ofMillis(1));

		// Assertions: the command runs once the full delay has elapsed.
		Assertions.assertEquals(1, runs.get(), "The command should run after the configured delay.");
	}

	@Test
	public void testSpacedOutRuns() {
		// Setup: the deduplicating runner has enough time between calls for each command to run independently.
		AtomicInteger runs = new AtomicInteger();
		Runnable testRunnable = runs::getAndIncrement;
		Duration delay = Duration.ofMillis(10);
		TestDelayedExecutor executor = new TestDelayedExecutor();
		DeduplicatingRunner deduplicatingRunner = new DeduplicatingRunner(delay, executor);

		// Operation: schedule and fully elapse the delay several times.
		for (int i = 0; i < 5; i++) {
			deduplicatingRunner.run(testRunnable);
			executor.elapse(delay);
		}

		// Assertions: spaced-out calls are not deduplicated together.
		Assertions.assertEquals(5, runs.get());
	}
}
