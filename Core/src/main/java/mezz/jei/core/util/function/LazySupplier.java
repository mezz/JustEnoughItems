package mezz.jei.core.util.function;

import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class LazySupplier<T> implements Supplier<T> {
    private final Supplier<T> supplier;
    @Nullable
    private T cachedResult;

    public LazySupplier(Supplier<T> supplier) {
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
