package mezz.jei.test;

import mezz.jei.common.util.IDelayedExecutor;
import mezz.jei.core.util.Pair;

import java.time.Duration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

class TestDelayedExecutor implements IDelayedExecutor {
	private final List<Pair<Duration, Runnable>> queue = new LinkedList<>();
	private Duration elapsed = Duration.ofMillis(0);

	public void elapse(Duration duration) {
		elapsed = elapsed.plus(duration);

		Iterator<Pair<Duration, Runnable>> iterator = queue.iterator();
		while (iterator.hasNext()) {
			Pair<Duration, Runnable> item = iterator.next();
			Duration scheduled = item.first();
			if (elapsed.compareTo(scheduled) >= 0) {
				item.second().run();
				iterator.remove();
			}
		}
	}

	@Override
	public Future<?> schedule(Runnable command, Duration delay) {
		if (delay.isNegative() || delay.isZero()) {
			throw new IllegalArgumentException("delay must be positive");
		} else {
			FutureTask<Object> future = new FutureTask<>(command, null);
			queue.add(new Pair<>(elapsed.plus(delay), future));
			return future;
		}
	}
}
