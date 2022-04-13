package mezz.jei.common.config.file;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConfigCategory {
    private final String name;
    @Unmodifiable
    private final Map<String, ConfigValue<?>> valueMap;

    public ConfigCategory(String name, List<ConfigValue<?>> values) {
        this.name = name;
        this.valueMap = values.stream()
            .collect(Collectors.toMap(ConfigValue::getName, Function.identity()));
    }

    public String getName() {
        return name;
    }

    @Nullable
    public ConfigValue<?> getConfigValue(String configValueName) {
        return valueMap.get(configValueName);
    }

    public Collection<ConfigValue<?>> getConfigValues() {
        return this.valueMap.values();
    }

    public Set<String> getValueNames() {
        return this.valueMap.keySet();
    }
}
