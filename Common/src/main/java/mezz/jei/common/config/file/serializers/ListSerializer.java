package mezz.jei.common.config.file.serializers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ListSerializer<T> implements IConfigValueSerializer<List<T>> {
    private final IConfigValueSerializer<T> valueSerializer;

    public ListSerializer(IConfigValueSerializer<T> valueSerializer) {
        this.valueSerializer = valueSerializer;
    }

    @Override
    public String serialize(List<T> values) {
        return values.stream()
            .map(valueSerializer::serialize)
            .collect(Collectors.joining(", "));
    }

    @Override
    public DeserializeResult<List<T>> deserialize(String string) {
        string = string.trim();
        if (string.startsWith("[")) {
            if (!string.endsWith("]")) {
                String errorMessage = """
                    No closing brace found.
                    List must have no braces, or be wrapped in [ and ].""";
                return new DeserializeResult<>(null, errorMessage);
            }
            string = string.substring(1, string.length() - 1);
        }
        String[] split = string.split(",");

        List<String> errors = new ArrayList<>();
        List<T> results = Arrays.stream(split)
            .map(String::trim)
            .map(valueSerializer::deserialize)
            .<T>mapMulti((r, c) -> {
                T result = r.getResult();
                if (result != null) {
                    c.accept(result);
                }
                errors.addAll(r.getErrors());
            })
            .toList();

        return new DeserializeResult<>(results, errors);
    }

    @Override
    public String getValidValuesDescription() {
        return "A comma-separated list containing values of:\n%s".formatted(valueSerializer.getValidValuesDescription());
    }
}
