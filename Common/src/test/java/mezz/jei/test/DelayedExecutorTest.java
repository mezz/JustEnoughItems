package mezz.jei.test;

import mezz.jei.common.util.DelayedExecutor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class DelayedExecutorTest {
	private static final Duration SHUTDOWN_TIMEOUT = Duration.ofSeconds(2);
	private static final Duration LONG_DELAY = SHUTDOWN_TIMEOUT.multipliedBy(10);
	private static final Duration FAST_SHUTDOWN_THRESHOLD = SHUTDOWN_TIMEOUT.dividedBy(10);

	private final DelayedExecutor executor = new DelayedExecutor(SHUTDOWN_TIMEOUT);

	@AfterEach
	public void tearDown() {
		executor.shutdown();
	}

	@Test
	public void testScheduledCommandRuns() throws Exception {
		AtomicBoolean ran = new AtomicBoolean(false);

		executor
			.schedule(() -> ran.set(true), Duration.ZERO)
			.get(1, TimeUnit.SECONDS);

		Assertions.assertTrue(ran.get());
	}

	@Test
	public void testNegativeDelayRunsImmediately() throws Exception {
		AtomicBoolean ran = new AtomicBoolean(false);

		executor
			.schedule(() -> ran.set(true), Duration.ofMillis(-1))
			.get(1, TimeUnit.SECONDS);

		Assertions.assertTrue(ran.get(), "ScheduledThreadPoolExecutor treats negative delays as immediate execution.");
	}

	@Test
	public void testCancellingScheduledCommand() throws Exception {
		AtomicBoolean ran = new AtomicBoolean(false);

		Future<?> future = executor
			.schedule(() -> ran.set(true), Duration.ofSeconds(1));

		future.cancel(false);

		Assertions.assertThrows(CancellationException.class, () -> future.get(2, TimeUnit.SECONDS));

		Assertions.assertFalse(ran.get());
	}

	@Test
	public void testShutdownRunsScheduledCommands() {
		AtomicBoolean ran = new AtomicBoolean(false);

		executor.schedule(() -> ran.set(true), LONG_DELAY);

		Assertions.assertTimeout(
			FAST_SHUTDOWN_THRESHOLD,
			executor::shutdown,
			"Shutdown should promote delayed commands instead of waiting for their original delay."
		);

		Assertions.assertTrue(ran.get(), "Shutdown should run pending delayed commands.");
	}

	@Test
	public void testShutdownRunsAllScheduledCommands() {
		AtomicInteger runs = new AtomicInteger();

		executor.schedule(runs::incrementAndGet, Duration.ZERO);
		executor.schedule(runs::incrementAndGet, LONG_DELAY.plusSeconds(1));
		executor.schedule(runs::incrementAndGet, LONG_DELAY.plusSeconds(2));

		executor.shutdown();

		Assertions.assertEquals(3, runs.get(), "Shutdown should run every pending delayed command.");
	}

	@Test
	public void testCanceledCommandDoesNotRunOnShutdown() {
		AtomicBoolean ran = new AtomicBoolean(false);

		executor
			.schedule(() -> ran.set(true), LONG_DELAY)
			.cancel(false);
		executor.shutdown();

		Assertions.assertFalse(ran.get(), "Canceled commands should not be restored by shutdown.");
	}

	@Test
	public void testShutdownWithoutScheduledCommands() {
		executor.shutdown();
	}

	@Test
	public void testScheduledCommandFailureIsReported() {
		IllegalStateException error = new IllegalStateException("test failure");

		Runnable throwError = () -> { throw error; };

		ExecutionException exception = Assertions.assertThrows(
			ExecutionException.class,
			() -> executor.schedule(throwError, Duration.ZERO).get()
		);

		Assertions.assertSame(error, exception.getCause(), "Task failures should be reported through the scheduled Future.");
	}
}
