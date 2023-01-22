package mezz.jei.common.config.file.serializers;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class SerializeResult {
    private final @Nullable String result;
    private final List<String> errors;

    public SerializeResult(String result) {
        this(result, List.of());
    }

    public SerializeResult(@Nullable String result, String error) {
        this(result, List.of(error));
    }

    public SerializeResult(@Nullable String result, List<String> errors) {
        this.result = result;
        this.errors = errors;
    }

    public @Nullable String getResult() {
        return result;
    }

    public List<String> getErrors() {
        return errors;
    }
}
