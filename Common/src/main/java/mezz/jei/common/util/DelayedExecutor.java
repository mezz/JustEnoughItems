package mezz.jei.common.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class DelayedExecutor implements IDelayedExecutor {
	private static final Logger LOGGER = LogManager.getLogger();

	private final ScheduledThreadPoolExecutor service;
	private final Duration shutdownTimeout;
	private final Set<ScheduledTask> scheduledTasks = ConcurrentHashMap.newKeySet();

	public DelayedExecutor(Duration shutdownTimeout) {
		var threadFactory = new ThreadFactoryBuilder()
			.setNameFormat("JEI Delayed Executor %d")
			.build();
		var service = new ScheduledThreadPoolExecutor(
			1,
			threadFactory
		);
		service.setRemoveOnCancelPolicy(true);
		service.setExecuteExistingDelayedTasksAfterShutdownPolicy(true);
		this.service = service;
		this.shutdownTimeout = shutdownTimeout;
	}

	@Override
	public Future<?> schedule(Runnable command, Duration delay) {
		ScheduledTask scheduledTask = new ScheduledTask(command);
		Future<?> future = service.schedule(scheduledTask, delay.toMillis(), TimeUnit.MILLISECONDS);
		scheduledTask.setFuture(future);
		scheduledTasks.add(scheduledTask);
		return new TrackedFuture<>(future, scheduledTask);
	}

	public void shutdown() {
		runScheduledTasksImmediately();
		service.shutdown();
		try {
			if (!service.awaitTermination(shutdownTimeout.toMillis(), TimeUnit.MILLISECONDS)) {
				forceShutdown("Timed out waiting for delayed tasks to finish.");
			}
		} catch (InterruptedException _) {
			forceShutdown("Interrupted while waiting for delayed tasks to finish.");
			Thread.currentThread().interrupt();
		}
	}

	private void runScheduledTasksImmediately() {
		for (ScheduledTask scheduledTask : scheduledTasks) {
			scheduledTasks.remove(scheduledTask);
			if (scheduledTask.cancel()) {
				try {
					service.execute(scheduledTask.command());
				} catch (RejectedExecutionException e) {
					LOGGER.error("Failed to execute delayed task during shutdown.", e);
				}
			}
		}
	}

	private void forceShutdown(String message) {
		List<Runnable> droppedTasks = service.shutdownNow();
		if (droppedTasks.isEmpty()) {
			LOGGER.error("{} Forcing shutdown.", message);
		} else {
			LOGGER.error("{} Forcing shutdown. {} delayed tasks never started.", message, droppedTasks.size());
		}
	}

	private final class TrackedFuture<T> implements DelegatedFuture<T> {
		private final Future<T> delegate;
		private final ScheduledTask scheduledTask;

		private TrackedFuture(Future<T> delegate, ScheduledTask scheduledTask) {
			this.delegate = delegate;
			this.scheduledTask = scheduledTask;
		}

		@Override
		public Future<T> getDelegate() {
			return delegate;
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			boolean canceled = delegate.cancel(mayInterruptIfRunning);
			if (canceled) {
				scheduledTasks.remove(scheduledTask);
			}
			return canceled;
		}
	}

	private final class ScheduledTask implements Runnable {
		private final Runnable command;
		private @Nullable volatile Future<?> future;

		private ScheduledTask(Runnable command) {
			this.command = command;
		}

		@Override
		public void run() {
			try {
				command.run();
			} finally {
				scheduledTasks.remove(this);
			}
		}

		public Runnable command() {
			return command;
		}

		public void setFuture(Future<?> future) {
			this.future = future;
		}

		public boolean cancel() {
			Future<?> future = this.future;
			if (future == null) {
				return true;
			}
			future.cancel(false);
			return future.isCancelled();
		}
	}

}
