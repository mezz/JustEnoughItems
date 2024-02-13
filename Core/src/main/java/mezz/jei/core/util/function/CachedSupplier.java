package mezz.jei.core.util.function;

import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class CachedSupplier<T> implements Supplier<T> {
	private final Supplier<T> supplier;
	@Nullable
	private T cachedResult;

	public CachedSupplier(Supplier<T> supplier) {
		this.supplier = supplier;
	}

	@Override
	public T get() {
		if (cachedResult == null) {
			cachedResult = supplier.get();
		}
		return cachedResult;
	}
}
