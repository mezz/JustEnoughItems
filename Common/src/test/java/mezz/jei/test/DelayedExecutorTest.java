package mezz.jei.test;

import mezz.jei.common.util.DelayedExecutor;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class DelayedExecutorTest {
	private static final Duration SHUTDOWN_TIMEOUT = Duration.ofSeconds(2);
	private static final Duration LONG_DELAY = SHUTDOWN_TIMEOUT.multipliedBy(10);

	private final TestScheduledExecutorService service = new TestScheduledExecutorService();
	private final DelayedExecutor executor = new DelayedExecutor(SHUTDOWN_TIMEOUT, service);

	@AfterEach
	public void tearDown() {
		executor.shutdown();
	}

	@Test
	public void testScheduledCommandRuns() throws Exception {
		// Setup: a command is scheduled to run after a short delay.
		AtomicBoolean ran = new AtomicBoolean(false);
		Future<?> future = executor.schedule(() -> ran.set(true), Duration.ofMillis(1));

		// Operation: advance the test scheduler far enough to run the command.
		service.elapse(Duration.ofMillis(1));
		future.get();

		// Assertions: the scheduled command ran exactly through its Future.
		Assertions.assertTrue(ran.get());
	}

	@Test
	public void testNegativeDelayRunsImmediately() throws Exception {
		// Setup: a command is scheduled with a negative delay, matching ScheduledExecutorService behavior.
		AtomicBoolean ran = new AtomicBoolean(false);
		Future<?> future = executor.schedule(() -> ran.set(true), Duration.ofMillis(-1));

		// Operation: process commands that are already due.
		service.elapse(Duration.ZERO);
		future.get();

		// Assertions: a negative delay is considered immediately due by the scheduler.
		Assertions.assertTrue(ran.get(), "Non-positive delays should be due immediately.");
	}

	@Test
	public void testCancellingScheduledCommand() throws Exception {
		// Setup: a delayed command is scheduled but canceled before its delay expires.
		AtomicBoolean ran = new AtomicBoolean(false);
		Future<?> future = executor
			.schedule(() -> ran.set(true), Duration.ofSeconds(1));
		future.cancel(false);

		// Operation: advance past the canceled command's scheduled time.
		service.elapse(Duration.ofSeconds(1));

		// Assertions: the command's future is canceled and the command body never runs.
		Assertions.assertThrows(CancellationException.class, future::get);
		Assertions.assertFalse(ran.get());
	}

	@Test
	public void testShutdownRunsScheduledCommands() {
		// Setup: a command is scheduled with a long delay that should not block shutdown.
		AtomicBoolean ran = new AtomicBoolean(false);
		executor.schedule(() -> ran.set(true), LONG_DELAY);

		// Operation: shut down before the original delay expires.
		executor.shutdown();

		// Assertions: shutdown flushes the tracked delayed command immediately.
		Assertions.assertTrue(ran.get(), "Tracked delayed commands accepted before shutdown should run on shutdown.");
	}

	@Test
	public void testShutdownRunsAllScheduledCommands() {
		// Setup: multiple commands are pending with different delays.
		AtomicInteger runs = new AtomicInteger();
		executor.schedule(runs::incrementAndGet, Duration.ZERO);
		executor.schedule(runs::incrementAndGet, LONG_DELAY.plusSeconds(1));
		executor.schedule(runs::incrementAndGet, LONG_DELAY.plusSeconds(2));

		// Operation: shut down while the commands are still tracked.
		executor.shutdown();

		// Assertions: every tracked command is flushed once.
		Assertions.assertEquals(3, runs.get(), "Shutdown should run every tracked delayed command.");
	}

	@Test
	public void testShutdownTaskFailureDoesNotPreventOtherCommandsRunningOnShutdown() {
		// Setup: one pending shutdown task fails, and other pending tasks still need to be flushed.
		AtomicInteger successfulRuns = new AtomicInteger();
		executor.schedule(() -> { throw new IllegalStateException("test failure"); }, LONG_DELAY);
		executor.schedule(successfulRuns::incrementAndGet, LONG_DELAY.plusMillis(1));
		executor.schedule(successfulRuns::incrementAndGet, LONG_DELAY.plusMillis(2));

		// Operation: shut down with a failing pending command.
		Assertions.assertDoesNotThrow(
			executor::shutdown,
			"Shutdown should contain task failures so it can keep flushing pending commands."
		);

		// Assertions: the failing command does not prevent the other tracked commands from running.
		Assertions.assertEquals(2, successfulRuns.get(), "A failing shutdown task should not stop later shutdown tasks.");
	}

	@Test
	public void testShutdownRunsScheduledCommandsOnExecutorThread() {
		// Setup: a delayed command records the thread used when shutdown flushes it.
		Thread shutdownThread = Thread.currentThread();
		AtomicReference<@Nullable Thread> commandThread = new AtomicReference<>();
		ScheduledThreadPoolExecutor service = new ScheduledThreadPoolExecutor(
			1,
			runnable -> new Thread(runnable, "delayed-executor-test")
		);
		service.setRemoveOnCancelPolicy(true);
		service.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
		DelayedExecutor executor = new DelayedExecutor(SHUTDOWN_TIMEOUT, service);

		try {
			executor.schedule(() -> commandThread.set(Thread.currentThread()), LONG_DELAY);

			// Operation: shut down before the original delay expires.
			executor.shutdown();

			// Assertions: shutdown flushes the command through the executor, not on the shutdown caller thread.
			Thread thread = commandThread.get();
			Assertions.assertNotNull(thread, "Shutdown should run the tracked delayed command.");
			Assertions.assertNotSame(shutdownThread, thread, "Shutdown should not run delayed commands on the caller thread.");
			Assertions.assertEquals("delayed-executor-test", thread.getName(), "Shutdown should run delayed commands on the executor thread.");
		} finally {
			service.shutdownNow();
		}
	}

	@Test
	public void testShutdownDoesNotRerunCompletedCommands() throws Exception {
		// Setup: a command has already run before shutdown begins.
		AtomicInteger runs = new AtomicInteger();
		Future<?> future = executor.schedule(runs::incrementAndGet, Duration.ofMillis(1));
		service.elapse(Duration.ofMillis(1));
		future.get();

		// Operation: shut down after the command completed.
		executor.shutdown();

		// Assertions: completed commands are no longer tracked and are not replayed during shutdown.
		Assertions.assertEquals(1, runs.get(), "Shutdown should not rerun commands that already completed.");
	}

	@Test
	public void testShutdownDuringScheduleRunsCommandOnce() {
		// Setup: shutdown is triggered after the service accepts the task but before the Future is returned.
		ShutdownDuringScheduleExecutorService service = new ShutdownDuringScheduleExecutorService(false);
		DelayedExecutor executor = new DelayedExecutor(SHUTDOWN_TIMEOUT, service);
		AtomicInteger runs = new AtomicInteger();
		service.setOnScheduleAccepted(executor::shutdown);

		try {
			// Operation: schedule a delayed command, causing shutdown before schedule returns.
			Future<?> future = executor.schedule(runs::incrementAndGet, Duration.ofMillis(50));

			// Assertions: the command is flushed once and the queued scheduled Future does not run it again.
			Assertions.assertTrue(service.isShutdown(), "The scheduler hook should have triggered shutdown.");
			Assertions.assertEquals(1, runs.get(), "Shutdown during schedule should run the command exactly once.");
			Assertions.assertTrue(future.isCancelled(), "The queued delayed task should be canceled when shutdown flushes it.");
		} finally {
			service.shutdownNow();
		}
	}

	@Test
	public void testRetainedSchedulerCopyCanRunCommandTwice() {
		// This contrast test shows the failure mode protected by testShutdownDuringScheduleRunsCommandOnce.
		// It sets executeExistingDelayedTasksAfterShutdownPolicy to true to prove a retained delayed copy can run twice.
		// The default service must keep this policy false because DelayedExecutor.shutdown owns the immediate flush.
		// Setup: shutdown races with schedule, but the scheduler keeps its delayed copy after shutdown.
		ShutdownDuringScheduleExecutorService service = new ShutdownDuringScheduleExecutorService(true);
		DelayedExecutor executor = new DelayedExecutor(SHUTDOWN_TIMEOUT, service);
		AtomicInteger runs = new AtomicInteger();
		service.setOnScheduleAccepted(executor::shutdown);

		try {
			// Operation: schedule a delayed command, causing shutdown before schedule returns.
			Future<?> future = executor.schedule(runs::incrementAndGet, Duration.ofMillis(50));

			// Assertions: shutdown flushes once, then the retained scheduler copy runs the same command again.
			Assertions.assertTrue(service.isShutdown(), "The scheduler hook should have triggered shutdown.");
			Assertions.assertEquals(2, runs.get(), "A retained scheduler copy can rerun a command already flushed by shutdown.");
			Assertions.assertFalse(future.isCancelled(), "The retained scheduler copy should not be canceled by shutdown.");
			Assertions.assertTrue(future.isDone(), "The retained scheduler copy should have completed before shutdown returned.");
		} finally {
			service.shutdownNow();
		}
	}

	@Test
	public void testCanceledCommandDoesNotRunOnShutdown() {
		// Setup: a pending delayed command is canceled before shutdown.
		AtomicBoolean ran = new AtomicBoolean(false);
		executor
			.schedule(() -> ran.set(true), LONG_DELAY)
			.cancel(false);

		// Operation: shut down after the command is canceled.
		executor.shutdown();

		// Assertions: canceled commands stay canceled and are not restored by shutdown flushing.
		Assertions.assertFalse(ran.get(), "Canceled commands should not be restored by shutdown.");
	}

	@Test
	public void testCanceledCommandDoesNotPreventOtherCommandsRunningOnShutdown() {
		// Setup: one pending command is canceled while another pending command remains tracked.
		AtomicInteger runs = new AtomicInteger();
		executor
			.schedule(runs::incrementAndGet, LONG_DELAY)
			.cancel(false);
		executor.schedule(runs::incrementAndGet, LONG_DELAY.plusMillis(1));

		// Operation: shut down with one canceled command and one pending command.
		executor.shutdown();

		// Assertions: canceling one command does not interfere with flushing the remaining pending command.
		Assertions.assertEquals(1, runs.get(), "Canceling one command should not prevent other pending commands from running.");
	}

	@Test
	public void testShutdownWithoutScheduledCommands() {
		// Setup: no commands are scheduled.

		// Operation: shut down an idle executor.
		executor.shutdown();

		// Assertions: no exception is thrown for an idle shutdown.
	}

	@Test
	public void testScheduledCommandFailureIsReported() {
		// Setup: a scheduled command throws an exception.
		IllegalStateException error = new IllegalStateException("test failure");
		Runnable throwError = () -> { throw error; };
		Future<?> future = executor.schedule(throwError, Duration.ofMillis(1));

		// Operation: run the scheduled command and read the Future result.
		service.elapse(Duration.ofMillis(1));
		ExecutionException exception = Assertions.assertThrows(
			ExecutionException.class,
			future::get
		);

		// Assertions: task failures are exposed through the returned Future.
		Assertions.assertSame(error, exception.getCause(), "Task failures should be reported through the scheduled Future.");
	}

	private static class TestScheduledExecutorService extends AbstractExecutorService implements ScheduledExecutorService {
		private static final Object RUNNABLE_RESULT = new Object();

		private final PriorityQueue<TestScheduledFuture<?>> scheduledTasks = new PriorityQueue<>();
		private Duration elapsed = Duration.ZERO;
		private boolean shutdown;

		public void elapse(Duration duration) {
			elapsed = elapsed.plus(duration);

			TestScheduledFuture<?> task = scheduledTasks.peek();
			while (task != null && task.scheduledTime.compareTo(elapsed) <= 0) {
				scheduledTasks.remove();
				if (!task.isCancelled()) {
					task.run();
				}
				task = scheduledTasks.peek();
			}
		}

		@Override
		public void shutdown() {
			shutdown = true;
			cancelScheduledTasks();
		}

		@Override
		public List<Runnable> shutdownNow() {
			shutdown = true;
			List<Runnable> tasks = new ArrayList<>(scheduledTasks);
			cancelScheduledTasks();
			return tasks;
		}

		@Override
		public boolean isShutdown() {
			return shutdown;
		}

		@Override
		public boolean isTerminated() {
			return shutdown;
		}

		@Override
		public boolean awaitTermination(long timeout, TimeUnit unit) {
			return true;
		}

		private void cancelScheduledTasks() {
			scheduledTasks.forEach(task -> task.cancel(false));
			scheduledTasks.clear();
		}

		@Override
		public void execute(Runnable command) {
			if (shutdown) {
				throw new RejectedExecutionException();
			}
			command.run();
		}

		@Override
		public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
			if (shutdown) {
				throw new RejectedExecutionException();
			}
			Duration scheduledTime = elapsed.plusNanos(unit.toNanos(delay));
			TestScheduledFuture<?> task = new TestScheduledFuture<>(command, RUNNABLE_RESULT, scheduledTime);
			scheduledTasks.add(task);
			return task;
		}

		@Override
		public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
			if (shutdown) {
				throw new RejectedExecutionException();
			}
			Duration scheduledTime = elapsed.plusNanos(unit.toNanos(delay));
			TestScheduledFuture<V> task = new TestScheduledFuture<>(callable, scheduledTime);
			scheduledTasks.add(task);
			return task;
		}

		@Override
		public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
			throw new UnsupportedOperationException();
		}

		private class TestScheduledFuture<V> extends FutureTask<V> implements ScheduledFuture<V> {
			private final Duration scheduledTime;

			public TestScheduledFuture(Runnable runnable, V result, Duration scheduledTime) {
				super(runnable, result);
				this.scheduledTime = scheduledTime;
			}

			public TestScheduledFuture(Callable<V> callable, Duration scheduledTime) {
				super(callable);
				this.scheduledTime = scheduledTime;
			}

			@Override
			public long getDelay(TimeUnit unit) {
				Duration delay = scheduledTime.minus(elapsed);
				return unit.convert(delay.toNanos(), TimeUnit.NANOSECONDS);
			}

			@Override
			public int compareTo(java.util.concurrent.Delayed other) {
				return Long.compare(
					getDelay(TimeUnit.NANOSECONDS),
					other.getDelay(TimeUnit.NANOSECONDS)
				);
			}
		}
	}

	private static class ShutdownDuringScheduleExecutorService extends ScheduledThreadPoolExecutor {
		private Runnable onScheduleAccepted = () -> {};

		public ShutdownDuringScheduleExecutorService(boolean executeDelayedTasksAfterShutdown) {
			super(1);
			setRemoveOnCancelPolicy(true);
			setExecuteExistingDelayedTasksAfterShutdownPolicy(executeDelayedTasksAfterShutdown);
		}

		public void setOnScheduleAccepted(Runnable onScheduleAccepted) {
			this.onScheduleAccepted = onScheduleAccepted;
		}

		@Override
		public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
			ScheduledFuture<?> future = super.schedule(command, delay, unit);
			onScheduleAccepted.run();
			return future;
		}
	}
}
