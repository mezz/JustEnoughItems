package mezz.jei.core.util.function;

import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

public class CachedSupplierTransformer<T, R> implements Supplier<R> {
    private final Supplier<T> supplier;
    private final Function<T, R> transformer;
    @Nullable
    private T previousValue;
    @Nullable
    private R cachedResult;

    public CachedSupplierTransformer(Supplier<T> supplier, Function<T, R> transformer) {
        this.supplier = supplier;
        this.transformer = transformer;
    }

    @Override
    public R get() {
        T currentValue = supplier.get();
        if (cachedResult != null && currentValue.equals(previousValue)) {
            return cachedResult;
        }
        cachedResult = transformer.apply(currentValue);
        previousValue = currentValue;
        return cachedResult;
    }
}
