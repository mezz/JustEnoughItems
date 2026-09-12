package mezz.jei.test.lib;

import net.mezzdev.config.api.value.IConfigValue;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.config.SearchMode;

public class TestIngredientFilterConfig implements IIngredientFilterConfig {
	private final IConfigValue<SearchMode> modNameSearchMode = value("modNameSearchMode", SearchMode.ENABLED);
	private final IConfigValue<SearchMode> tooltipSearchMode = value("tooltipSearchMode", SearchMode.ENABLED);
	private final IConfigValue<SearchMode> tagSearchMode = value("tagSearchMode", SearchMode.ENABLED);
	private final IConfigValue<SearchMode> colorSearchMode = value("colorSearchMode", SearchMode.REQUIRE_PREFIX);
	private final IConfigValue<SearchMode> resourceLocationSearchMode = value("resourceLocationSearchMode", SearchMode.ENABLED);
	private final IConfigValue<SearchMode> creativeTabSearchMode = value("creativeTabSearchMode", SearchMode.DISABLED);
	private final IConfigValue<Boolean> searchAdvancedTooltips = value("searchAdvancedTooltips", false);
	private final IConfigValue<Boolean> searchModIds = value("searchModIds", false);
	private final IConfigValue<Boolean> searchModAliases = value("searchModAliases", false);
	private final IConfigValue<Boolean> searchIngredientAliases = value("searchIngredientAliases", false);
	private final IConfigValue<Boolean> searchShortModNames = value("searchShortModNames", false);

	private static <T> IConfigValue<T> value(String name, T value) {
		return new TestJeiConfigValue<>(name, value);
	}

	@Override
	public IConfigValue<SearchMode> modNameSearchMode() {
		return modNameSearchMode;
	}

	@Override
	public IConfigValue<SearchMode> tooltipSearchMode() {
		return tooltipSearchMode;
	}

	@Override
	public IConfigValue<SearchMode> tagSearchMode() {
		return tagSearchMode;
	}

	@Override
	public IConfigValue<SearchMode> colorSearchMode() {
		return colorSearchMode;
	}

	@Override
	public IConfigValue<SearchMode> resourceLocationSearchMode() {
		return resourceLocationSearchMode;
	}

	@Override
	public IConfigValue<SearchMode> creativeTabSearchMode() {
		return creativeTabSearchMode;
	}

	@Override
	public IConfigValue<Boolean> searchAdvancedTooltips() {
		return searchAdvancedTooltips;
	}

	@Override
	public IConfigValue<Boolean> searchModIds() {
		return searchModIds;
	}

	@Override
	public IConfigValue<Boolean> searchModAliases() {
		return searchModAliases;
	}

	@Override
	public IConfigValue<Boolean> searchIngredientAliases() {
		return searchIngredientAliases;
	}

	@Override
	public IConfigValue<Boolean> searchShortModNames() {
		return searchShortModNames;
	}
}
