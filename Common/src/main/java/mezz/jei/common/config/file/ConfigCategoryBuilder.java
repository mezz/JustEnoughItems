package mezz.jei.common.config.file;

import mezz.jei.api.runtime.config.IJeiConfigValueSerializer;
import mezz.jei.common.config.file.serializers.EnumSerializer;
import mezz.jei.common.config.file.serializers.BooleanSerializer;
import mezz.jei.common.config.file.serializers.IntegerSerializer;

import java.util.ArrayList;
import java.util.List;

public class ConfigCategoryBuilder implements IConfigCategoryBuilder {
	private final String name;
	private final List<ConfigValue<?>> values = new ArrayList<>();

	public ConfigCategoryBuilder(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public <T> ConfigValue<T> addValue(ConfigValue<T> value) {
		this.values.add(value);
		return value;
	}

	@Override
	public ConfigValue<Boolean> addBoolean(String name, boolean defaultValue, String description) {
		return addValue(new ConfigValue<>(name, defaultValue, BooleanSerializer.INSTANCE, description));
	}

	@Override
	public <T extends Enum<T>> ConfigValue<T> addEnum(String name, T defaultValue, String description) {
		EnumSerializer<T> serializer = new EnumSerializer<>(defaultValue.getDeclaringClass());
		return addValue(new ConfigValue<>(name, defaultValue, serializer, description));
	}

	@Override
	public ConfigValue<Integer> addInteger(String name, int defaultValue, int minValue, int maxValue, String description) {
		IntegerSerializer serializer = new IntegerSerializer(minValue, maxValue);
		return addValue(new ConfigValue<>(name, defaultValue, serializer, description));
	}

	@Override
	public <T> ConfigValue<List<T>> addList(String name, List<T> defaultValue, IJeiConfigValueSerializer<List<T>> listSerializer, String description) {
		return addValue(new ConfigValue<>(name, defaultValue, listSerializer, description));
	}

	public ConfigCategory build(ConfigSchema schema) {
		for (ConfigValue<?> value : values) {
			value.setSchema(schema);
		}
		return new ConfigCategory(name, values);
	}
}
