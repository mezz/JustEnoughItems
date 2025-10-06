package mezz.jei.common.config.file;

import mezz.jei.api.runtime.config.IJeiConfigCategory;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ConfigCategory implements IJeiConfigCategory {
	private final String name;
	private final Component localizedName;
	private final Component description;
	@Unmodifiable
	private final Map<String, ConfigValue<?>> valueMap;

	public ConfigCategory(String localizationPath, String name, List<ConfigValue<?>> values) {
		this.name = name;
		this.localizedName = Component.translatable(localizationPath);
		this.description = Component.translatable(localizationPath + ".description");
		Map<String, ConfigValue<?>> map = new LinkedHashMap<>();
		for (ConfigValue<?> value : values) {
			map.put(value.getName(), value);
		}
		this.valueMap = Collections.unmodifiableMap(map);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Component getLocalizedName() {
		return localizedName;
	}

	@Override
	public Component getDescription() {
		return description;
	}

	public Optional<ConfigValue<?>> getConfigValue(String configValueName) {
		ConfigValue<?> configValue = valueMap.get(configValueName);
		return Optional.ofNullable(configValue);
	}

	@Override
	@Unmodifiable
	public Collection<ConfigValue<?>> getConfigValues() {
		return this.valueMap.values();
	}

	public Set<String> getValueNames() {
		return this.valueMap.keySet();
	}
}
