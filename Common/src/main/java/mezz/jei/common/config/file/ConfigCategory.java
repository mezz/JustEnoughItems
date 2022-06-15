package mezz.jei.common.config.file;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConfigCategory {
    private final String name;
    @Unmodifiable
    private final Map<String, ConfigValue<?>> valueMap;

    public ConfigCategory(String name, List<ConfigValue<?>> values) {
        this.name = name;
        Map<String, ConfigValue<?>> map = new LinkedHashMap<>();
        for (ConfigValue<?> value : values) {
            map.put(value.getName(), value);
        }
        this.valueMap = Collections.unmodifiableMap(map);
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
