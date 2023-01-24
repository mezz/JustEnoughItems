package mezz.jei.common.config.file;

import mezz.jei.api.runtime.config.IJeiConfigValueSerializer;

import java.util.List;
import java.util.function.Supplier;

public interface IConfigCategoryBuilder {
    Supplier<Boolean> addBoolean(String name, boolean defaultValue, String description);
    Supplier<Integer> addInteger(String name, int defaultValue, int minValue, int maxValue, String description);
    <T extends Enum<T>> Supplier<T> addEnum(String name, T defaultValue, String description);
    <T> Supplier<List<T>> addList(String name, List<T> defaultValue, IJeiConfigValueSerializer<List<T>> listSerializer, String description);
}
