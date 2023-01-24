package mezz.jei.common.config.file.serializers;

import mezz.jei.api.runtime.config.IJeiConfigValueSerializer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public final class DeserializeResult<T> implements IJeiConfigValueSerializer.IDeserializeResult<T> {
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

    @Override
    public Optional<T> getResult() {
        return Optional.ofNullable(result);
    }

    @Override
    public List<String> getErrors() {
        return errors;
    }
}
