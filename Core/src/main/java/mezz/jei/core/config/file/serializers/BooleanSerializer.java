package mezz.jei.core.config.file.serializers;

public class BooleanSerializer implements IConfigValueSerializer<Boolean> {
    public static final BooleanSerializer INSTANCE = new BooleanSerializer();

    private BooleanSerializer() {}

    @Override
    public String serialize(Boolean value) {
        return value.toString();
    }

    @Override
    public DeserializeResult<Boolean> deserialize(String string) {
        string = string.trim();
        if ("true".equalsIgnoreCase(string)) {
            return new DeserializeResult<>(true);
        }
        if ("false".equalsIgnoreCase(string)) {
            return new DeserializeResult<>(false);
        }
        return new DeserializeResult<>(null, "string must be 'true' or 'false'");
    }

    @Override
    public String getValidValuesDescription() {
        return "[true, false]";
    }
}
