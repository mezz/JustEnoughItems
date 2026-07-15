package mezz.jei.test.lib;

import mezz.jei.api.runtime.config.IJeiConfigValue;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.search.SearchMode;

public class TestIngredientFilterConfig implements IIngredientFilterConfig {
	private final IJeiConfigValue<SearchMode> modNameSearchMode = value("modNameSearchMode", SearchMode.ENABLED);
	private final IJeiConfigValue<SearchMode> tooltipSearchMode = value("tooltipSearchMode", SearchMode.ENABLED);
	private final IJeiConfigValue<SearchMode> tagSearchMode = value("tagSearchMode", SearchMode.ENABLED);
	private final IJeiConfigValue<SearchMode> colorSearchMode = value("colorSearchMode", SearchMode.DISABLED);
	private final IJeiConfigValue<SearchMode> resourceLocationSearchMode = value("resourceLocationSearchMode", SearchMode.ENABLED);
	private final IJeiConfigValue<SearchMode> creativeTabSearchMode = value("creativeTabSearchMode", SearchMode.DISABLED);
	private final IJeiConfigValue<Boolean> searchAdvancedTooltips = value("searchAdvancedTooltips", false);
	private final IJeiConfigValue<Boolean> searchModIds = value("searchModIds", false);
	private final IJeiConfigValue<Boolean> searchModAliases = value("searchModAliases", false);
	private final IJeiConfigValue<Boolean> searchIngredientAliases = value("searchIngredientAliases", false);
	private final IJeiConfigValue<Boolean> searchShortModNames = value("searchShortModNames", false);

	private static <T> IJeiConfigValue<T> value(String name, T value) {
		return new TestJeiConfigValue<>(name, value);
	}

	@Override
	public IJeiConfigValue<SearchMode> modNameSearchMode() {
		return modNameSearchMode;
	}

	@Override
	public IJeiConfigValue<SearchMode> tooltipSearchMode() {
		return tooltipSearchMode;
	}

	@Override
	public IJeiConfigValue<SearchMode> tagSearchMode() {
		return tagSearchMode;
	}

	@Override
	public IJeiConfigValue<SearchMode> colorSearchMode() {
		return colorSearchMode;
	}

	@Override
	public IJeiConfigValue<SearchMode> resourceLocationSearchMode() {
		return resourceLocationSearchMode;
	}

	@Override
	public IJeiConfigValue<SearchMode> creativeTabSearchMode() {
		return creativeTabSearchMode;
	}

	@Override
	public IJeiConfigValue<Boolean> searchAdvancedTooltips() {
		return searchAdvancedTooltips;
	}

	@Override
	public IJeiConfigValue<Boolean> searchModIds() {
		return searchModIds;
	}

	@Override
	public IJeiConfigValue<Boolean> searchModAliases() {
		return searchModAliases;
	}

	@Override
	public IJeiConfigValue<Boolean> searchIngredientAliases() {
		return searchIngredientAliases;
	}

	@Override
	public IJeiConfigValue<Boolean> searchShortModNames() {
		return searchShortModNames;
	}
}
