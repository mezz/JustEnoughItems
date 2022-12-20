package mezz.jei.core.config.file.serializers;

public class IntegerSerializer implements IConfigValueSerializer<Integer> {
    public static final IntegerSerializer ANY = new IntegerSerializer(Integer.MIN_VALUE, Integer.MAX_VALUE);
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
}
