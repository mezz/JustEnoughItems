package mezz.jei.common.config.file;

import mezz.jei.api.runtime.config.IJeiConfigValueSerializer;

import java.util.List;

public interface IConfigCategoryBuilder {
	ConfigValue<Boolean> addBoolean(String path, boolean defaultValue);
	ConfigValue<Integer> addInteger(String path, int defaultValue, int minValue, int maxValue);
	<T extends Enum<T>> ConfigValue<T> addEnum(String path, T defaultValue);
	<T> ConfigValue<List<T>> addList(String path, List<T> defaultValue, IJeiConfigValueSerializer<List<T>> listSerializer);
}
