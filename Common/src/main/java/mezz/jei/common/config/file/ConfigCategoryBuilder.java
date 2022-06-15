package mezz.jei.common.config.file;

import mezz.jei.common.config.file.serializers.BooleanSerializer;
import mezz.jei.common.config.file.serializers.EnumSerializer;
import mezz.jei.common.config.file.serializers.IConfigValueSerializer;
import mezz.jei.common.config.file.serializers.IntegerSerializer;
import mezz.jei.common.config.file.serializers.ListSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ConfigCategoryBuilder {
    private final String name;
    private final List<ConfigValue<?>> values = new ArrayList<>();

    public ConfigCategoryBuilder(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public <T> Supplier<T> addValue(ConfigValue<T> value) {
        this.values.add(value);
        return value::getValue;
    }

    public Supplier<Boolean> addBoolean(String name, boolean defaultValue, String description) {
        return addValue(new ConfigValue<>(name, defaultValue, BooleanSerializer.INSTANCE, description));
    }

    public <T extends Enum<T>> Supplier<T> addEnum(String name, T defaultValue, String description) {
        EnumSerializer<T> serializer = new EnumSerializer<>(defaultValue.getDeclaringClass());
        return addValue(new ConfigValue<>(name, defaultValue, serializer, description));
    }

    public Supplier<Integer> addInteger(String name, int defaultValue, int minValue, int maxValue, String description) {
        IntegerSerializer serializer = new IntegerSerializer(minValue, maxValue);
        return addValue(new ConfigValue<>(name, defaultValue, serializer, description));
    }

    public <T> Supplier<List<T>> addList(String name, List<T> defaultValue, IConfigValueSerializer<T> valueSerializer, String description) {
        ListSerializer<T> serializer = new ListSerializer<>(valueSerializer);
        return addValue(new ConfigValue<>(name, defaultValue, serializer, description));
    }

    public ConfigCategory build(ConfigSchema schema) {
        for (ConfigValue<?> value : values) {
            value.setSchema(schema);
        }
        return new ConfigCategory(name, values);
    }
}
