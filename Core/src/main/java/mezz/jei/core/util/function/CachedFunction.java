package mezz.jei.core.util.function;

import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class CachedFunction<T, R> implements Function<T, R> {
    private final Function<T, R> function;
    @Nullable
    private T previousValue;
    @Nullable
    private R cachedResult;

    public CachedFunction(Function<T, R> function) {
        this.function = function;
    }

    @Override
    public R apply(T currentValue) {
        if (currentValue.equals(previousValue)) {
            assert cachedResult != null;
            return cachedResult;
        }
        cachedResult = function.apply(currentValue);
        previousValue = currentValue;
        return cachedResult;
    }
}
