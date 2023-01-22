package mezz.jei.common.config.file;

import mezz.jei.api.runtime.config.IJeiConfigValue;
import mezz.jei.common.config.file.serializers.DeserializeResult;
import mezz.jei.common.config.file.serializers.IConfigValueSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ConfigValue<T> implements IJeiConfigValue<T> {
    private static final Logger LOGGER = LogManager.getLogger();

    private final String name;
    private final String description;
    private final T defaultValue;
    private final IConfigValueSerializer<T> serializer;
    private volatile T currentValue;
    @Nullable
    private IConfigSchema schema;

    public ConfigValue(String name, T defaultValue, IConfigValueSerializer<T> serializer, String description) {
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;
        this.currentValue = defaultValue;
        this.serializer = serializer;
    }

    public void setSchema(IConfigSchema schema) {
        this.schema = schema;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public T getDefaultValue() {
        return defaultValue;
    }

    @Override
    public T getValue() {
        if (schema != null) {
            schema.loadIfNeeded();
        }
        return currentValue;
    }

    @Override
    public String getValidValuesDescription() {
        return this.serializer.getValidValuesDescription();
    }

    @Override
    public boolean isValid(T value) {
        return this.serializer.isValid(value);
    }

    public IConfigValueSerializer<T> getSerializer() {
        return serializer;
    }

    public List<String> setFromSerializedValue(String value) {
        DeserializeResult<T> deserializeResult = serializer.deserialize(value);
        T result = deserializeResult.getResult();
        if (result != null) {
            currentValue = result;
        }
        return deserializeResult.getErrors();
    }

    @Override
    public boolean set(T value) {
        if (!serializer.isValid(value)) {
            LOGGER.error("Tried to set invalid value : {}\n{}", value,  serializer.getValidValuesDescription());
            return false;
        }
        if (!currentValue.equals(value)) {
            currentValue = value;
            if (schema != null) {
                schema.markDirty();
            }
            return true;
        }
        return false;
    }
}
