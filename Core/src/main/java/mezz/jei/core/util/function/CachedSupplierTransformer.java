package mezz.jei.core.util.function;

import java.util.function.Function;
import java.util.function.Supplier;

public class CachedSupplierTransformer<T, R> implements Supplier<R> {
    private final Supplier<T> supplier;
    private final CachedFunction<T, R> cachedFunction;

    public CachedSupplierTransformer(Supplier<T> supplier, Function<T, R> function) {
        this.supplier = supplier;
        this.cachedFunction = new CachedFunction<>(function);
    }

    @Override
    public R get() {
        T currentValue = supplier.get();
        return cachedFunction.apply(currentValue);
    }
}
