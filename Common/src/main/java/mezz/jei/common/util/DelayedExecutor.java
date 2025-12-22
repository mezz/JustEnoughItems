package mezz.jei.common.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

final class DelayedExecutor implements IDelayedExecutor {
	private static @Nullable DelayedExecutor INSTANCE;

	public static DelayedExecutor getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new DelayedExecutor();
		}
		return INSTANCE;
	}

	private final ScheduledThreadPoolExecutor service;

	private DelayedExecutor() {
		var threadFactory = new ThreadFactoryBuilder()
			.setNameFormat("JEI Deduplicating Run Executor %d")
			.build();
		var service = new ScheduledThreadPoolExecutor(
			1,
			threadFactory
		);
		service.setRemoveOnCancelPolicy(true);
		this.service = service;
	}

	@Override
	public Future<?> schedule(Runnable command, Duration delay) {
		return service.schedule(command, delay.toMillis(), TimeUnit.MILLISECONDS);
	}
}
