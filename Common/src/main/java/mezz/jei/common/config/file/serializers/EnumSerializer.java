package mezz.jei.common.config.file.serializers;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class EnumSerializer<T extends Enum<T>> implements IConfigValueSerializer<T> {
    private final Class<T> enumClass;
    private final Collection<T> validValues;

    public EnumSerializer(Class<T> enumClass) {
        this.enumClass = enumClass;
        this.validValues = List.of(enumClass.getEnumConstants());
    }

    public EnumSerializer(Class<T> enumClass, Collection<T> validValues) {
        this.enumClass = enumClass;
        this.validValues = validValues;
    }

    @Override
    public String serialize(T value) {
        return value.name();
    }

    @Override
    public DeserializeResult<T> deserialize(String string) {
        string = string.trim();
        if (string.startsWith("\"") && string.endsWith("\"")) {
            string = string.substring(1, string.length() - 1);
        }
        try {
            T value = Enum.valueOf(enumClass, string);
            return new DeserializeResult<>(value);
        } catch (IllegalArgumentException e) {
            return new DeserializeResult<>(null, "Invalid enum name: %s".formatted(e.getMessage()));
        }
    }

    @Override
    public String getValidValuesDescription() {
        String names = validValues.stream()
            .map(Enum::name)
            .collect(Collectors.joining(", "));

        return "[%s]".formatted(names);
    }
}
