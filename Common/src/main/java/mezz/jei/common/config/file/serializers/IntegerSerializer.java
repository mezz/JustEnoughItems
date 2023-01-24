package mezz.jei.common.config.file.serializers;

import mezz.jei.api.runtime.config.IJeiConfigValueSerializer;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class IntegerSerializer implements IJeiConfigValueSerializer<Integer> {
    private final int min;
    private final int max;

    public IntegerSerializer(int min, int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public String serialize(Integer value) {
        return value.toString();
    }

    @Override
    public DeserializeResult<Integer> deserialize(String string) {
        string = string.trim();
        try {
            int value = Integer.parseInt(string);
            if (!isValid(value)) {
                String errorMessage = "Invalid integer. Must be: " + getValidValuesDescription();
                return new DeserializeResult<>(null, errorMessage);
            }
            return new DeserializeResult<>(value);
        } catch (NumberFormatException e) {
            String errorMessage = "Unable to parse int: '%s' with error:\n%s".formatted(string, e.getMessage());
            return new DeserializeResult<>(null, errorMessage);
        }
    }

    @Override
    public String getValidValuesDescription() {
        if (min == Integer.MIN_VALUE && max == Integer.MAX_VALUE) {
            return "Any integer";
        }
        if (max == Integer.MAX_VALUE) {
            return "Any integer greater than or equal to %s".formatted(min);
        }

        return "An integer in the range [%s, %s] (inclusive)".formatted(min, max);
    }

    @Override
    public boolean isValid(Integer value) {
        return value >= min && value <= max;
    }

    @Override
    public Optional<Collection<Integer>> getAllValidValues() {
        int count = max - min + 1;
        if (count > 0 && count < 20) {
            List<Integer> values = IntStream.rangeClosed(min, max)
                .boxed()
                .toList();
            return Optional.of(values);
        }
        return Optional.empty();
    }
}
