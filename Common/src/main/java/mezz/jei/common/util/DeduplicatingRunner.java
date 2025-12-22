package mezz.jei.common.util;

import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.concurrent.Future;

/**
 * This will only run once `delay` has elapsed, without additional runs being called.
 */
public class DeduplicatingRunner {
	private final IDelayedExecutor executor;
	private final Duration delay;
	private @Nullable Future<?> future;

	public DeduplicatingRunner(Duration delay) {
		this(delay, DelayedExecutor.getInstance());
	}

	public DeduplicatingRunner(Duration delay, IDelayedExecutor executor) {
		this.delay = delay;
		this.executor = executor;
	}

	public synchronized void run(Runnable runnable) {
		if (future != null) {
			future.cancel(false);
		}
		future = executor.schedule(runnable, delay);
	}
}
