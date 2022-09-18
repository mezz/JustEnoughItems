package mezz.jei.common.config.file;

import mezz.jei.common.config.file.serializers.DeserializeResult;
import mezz.jei.common.config.file.serializers.IConfigValueSerializer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ConfigValue<T> {
    private final String name;
    private final String description;
    private final T defaultValue;
    private final IConfigValueSerializer<T> serializer;
    private volatile T currentValue;
    @Nullable
    private Runnable loadCallback;

    public ConfigValue(String name, T defaultValue, IConfigValueSerializer<T> serializer, String description) {
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;
        this.currentValue = defaultValue;
        this.serializer = serializer;
    }

    public void setLoadCallback(Runnable loadCallback) {
        this.loadCallback = loadCallback;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public T getValue() {
        if (loadCallback != null) {
            loadCallback.run();
        }
        return currentValue;
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
}
