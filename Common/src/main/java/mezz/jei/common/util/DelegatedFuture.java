package mezz.jei.common.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public interface DelegatedFuture<T> extends Future<T> {
	Future<T> getDelegate();

	@Override
	default boolean cancel(boolean mayInterruptIfRunning) {
		return getDelegate().cancel(mayInterruptIfRunning);
	}

	@Override
	default boolean isCancelled() {
		return getDelegate().isCancelled();
	}

	@Override
	default boolean isDone() {
		return getDelegate().isDone();
	}

	@Override
	default T get() throws InterruptedException, ExecutionException {
		return getDelegate().get();
	}

	@Override
	default T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return getDelegate().get(timeout, unit);
	}

	@Override
	default T resultNow() {
		return getDelegate().resultNow();
	}

	@Override
	default Throwable exceptionNow() {
		return getDelegate().exceptionNow();
	}

	@Override
	default State state() {
		return getDelegate().state();
	}
}
