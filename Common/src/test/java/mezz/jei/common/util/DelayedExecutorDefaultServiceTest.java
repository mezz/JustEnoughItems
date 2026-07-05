package mezz.jei.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class DelayedExecutorDefaultServiceTest {
	private static final Duration LONG_DELAY = Duration.ofSeconds(20);

	@Test
	public void testShutdownDoesNotKeepUnclaimedDelayedTasksAlive() throws Exception {
		// Setup: a delayed task is accepted by the default scheduler but is not tracked by DelayedExecutor.
		ScheduledThreadPoolExecutor service = DelayedExecutor.createDefaultService();
		try {
			Assertions.assertFalse(
				service.getExecuteExistingDelayedTasksAfterShutdownPolicy(),
				"Default service must not keep delayed tasks queued after shutdown."
			);
			AtomicBoolean ran = new AtomicBoolean(false);
			service.schedule(() -> ran.set(true), LONG_DELAY.toMillis(), TimeUnit.MILLISECONDS);

			// Operation: shut down the scheduler before the task's delay elapses.
			service.shutdown();

			// Assertions: unclaimed delayed tasks do not keep shutdown waiting and cannot run after shutdown.
			Assertions.assertTrue(
				service.awaitTermination(100, TimeUnit.MILLISECONDS),
				"Default service shutdown should not wait for unclaimed delayed tasks."
			);
			Assertions.assertFalse(
				ran.get(),
				"Unclaimed delayed tasks should not run from the scheduler after shutdown."
			);
		} finally {
			service.shutdownNow();
		}
	}

	@Test
	public void testRetainedDelayedTasksKeepShutdownAlive() throws Exception {
		// This contrast test shows the failure mode protected by testShutdownDoesNotKeepUnclaimedDelayedTasksAlive.
		// It sets executeExistingDelayedTasksAfterShutdownPolicy to true to prove retained delayed tasks can block shutdown.
		// The default service must keep this policy false so shutdown is not held open by original task delays.
		// Setup: a scheduler keeps delayed tasks after shutdown, which is what DelayedExecutor's default avoids.
		ScheduledThreadPoolExecutor service = new ScheduledThreadPoolExecutor(1);
		try {
			service.setExecuteExistingDelayedTasksAfterShutdownPolicy(true);
			AtomicBoolean ran = new AtomicBoolean(false);
			service.schedule(() -> ran.set(true), LONG_DELAY.toMillis(), TimeUnit.MILLISECONDS);

			// Operation: shut down the scheduler before the task's delay elapses.
			service.shutdown();

			// Assertions: retained delayed tasks keep shutdown open until their original delay can pass.
			Assertions.assertFalse(
				service.awaitTermination(100, TimeUnit.MILLISECONDS),
				"Shutdown should keep waiting while a retained delayed task is queued."
			);
			Assertions.assertFalse(
				ran.get(),
				"The retained delayed task should not have run before its original delay."
			);
		} finally {
			service.shutdownNow();
		}
	}
}
