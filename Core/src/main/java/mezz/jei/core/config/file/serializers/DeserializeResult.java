package mezz.jei.core.config.file.serializers;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class DeserializeResult<T> {
    private final @Nullable T result;
    private final List<String> errors;

    public DeserializeResult(T result) {
        this(result, List.of());
    }

    public DeserializeResult(@Nullable T result, String error) {
        this(result, List.of(error));
    }

    public DeserializeResult(@Nullable T result, List<String> errors) {
        this.result = result;
        this.errors = errors;
    }

    public @Nullable T getResult() {
        return result;
    }

    public List<String> getErrors() {
        return errors;
    }
}
