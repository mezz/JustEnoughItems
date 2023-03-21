package mezz.jei.common.config.file.serializers;

import mezz.jei.api.runtime.config.IJeiConfigValueSerializer;

import java.util.Collection;
import java.util.Optional;

public class StringSerializer implements IJeiConfigValueSerializer<String> {
    @Override
    public String serialize(String value) {
        return value;
    }

    @Override
    public IDeserializeResult<String> deserialize(String string) {
        string = string.trim();
        if (string.startsWith("\"") && string.endsWith("\"")) {
            string = string.substring(1, string.length() - 1);
        };
        return new DeserializeResult<>(string);
    }

    @Override
    public boolean isValid(String value) {
        return true;
    }

    @Override
    public Optional<Collection<String>> getAllValidValues() {
        return Optional.empty();
    }

    @Override
    public String getValidValuesDescription() {
        return "";
    }
}
