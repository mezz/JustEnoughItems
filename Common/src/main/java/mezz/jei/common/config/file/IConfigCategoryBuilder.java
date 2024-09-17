package mezz.jei.common.config.file;


import mezz.jei.api.runtime.config.IJeiConfigValueSerializer;

import java.util.List;

public interface IConfigCategoryBuilder {
	ConfigValue<Boolean> addBoolean(String name, boolean defaultValue, String description);
	ConfigValue<Integer> addInteger(String name, int defaultValue, int minValue, int maxValue, String description);
	<T extends Enum<T>> ConfigValue<T> addEnum(String name, T defaultValue, String description);
	<T> ConfigValue<List<T>> addList(String name, List<T> defaultValue, IJeiConfigValueSerializer<List<T>> listSerializer, String description);
}
