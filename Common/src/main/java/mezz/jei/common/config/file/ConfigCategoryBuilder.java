package mezz.jei.common.config.file;

import mezz.jei.api.runtime.config.IJeiConfigValueSerializer;
import mezz.jei.common.config.file.serializers.EnumSerializer;
import mezz.jei.common.config.file.serializers.BooleanSerializer;
import mezz.jei.common.config.file.serializers.IntegerSerializer;

import java.util.ArrayList;
import java.util.List;

public class ConfigCategoryBuilder implements IConfigCategoryBuilder {
	private final String name;
	private final String localizationPath;
	private final List<ConfigValue<?>> values = new ArrayList<>();

	public ConfigCategoryBuilder(String localizationPath, String name) {
		this.name = name;
		this.localizationPath = localizationPath + "." + name;
	}

	public String getName() {
		return name;
	}

	public <T> ConfigValue<T> addValue(ConfigValue<T> value) {
		this.values.add(value);
		return value;
	}

	@Override
	public ConfigValue<Boolean> addBoolean(String name, boolean defaultValue) {
		return addValue(new ConfigValue<>(localizationPath, name, defaultValue, BooleanSerializer.INSTANCE));
	}

	@Override
	public <T extends Enum<T>> ConfigValue<T> addEnum(String name, T defaultValue) {
		EnumSerializer<T> serializer = new EnumSerializer<>(defaultValue.getDeclaringClass());
		return addValue(new ConfigValue<>(localizationPath, name, defaultValue, serializer));
	}

	@Override
	public ConfigValue<Integer> addInteger(String name, int defaultValue, int minValue, int maxValue) {
		IntegerSerializer serializer = new IntegerSerializer(minValue, maxValue);
		return addValue(new ConfigValue<>(localizationPath, name, defaultValue, serializer));
	}

	@Override
	public <T> ConfigValue<List<T>> addList(String name, List<T> defaultValue, IJeiConfigValueSerializer<List<T>> listSerializer) {
		return addValue(new ConfigValue<>(localizationPath, name, defaultValue, listSerializer));
	}

	public ConfigCategory build(ConfigSchema schema) {
		for (ConfigValue<?> value : values) {
			value.setSchema(schema);
		}
		return new ConfigCategory(name, values);
	}
}
