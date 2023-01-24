package mezz.jei.common.config.file.serializers;

import mezz.jei.api.runtime.config.IJeiConfigValueSerializer;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class BooleanSerializer implements IJeiConfigValueSerializer<Boolean> {
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

    @Override
    public boolean isValid(Boolean value) {
        return true;
    }

    @Override
    public Optional<Collection<Boolean>> getAllValidValues() {
        return Optional.of(List.of(true, false));
    }
}
