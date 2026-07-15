package mezz.jei.common.config;

import mezz.jei.api.runtime.config.IJeiConfigValue;
import mezz.jei.common.search.SearchMode;

public interface IIngredientFilterConfig {
	IJeiConfigValue<SearchMode> modNameSearchMode();

	IJeiConfigValue<SearchMode> tooltipSearchMode();

	IJeiConfigValue<SearchMode> tagSearchMode();

	IJeiConfigValue<SearchMode> colorSearchMode();

	IJeiConfigValue<SearchMode> identifierSearchMode();

	IJeiConfigValue<SearchMode> creativeTabSearchMode();

	IJeiConfigValue<Boolean> searchAdvancedTooltips();

	IJeiConfigValue<Boolean> searchModIds();

	IJeiConfigValue<Boolean> searchModAliases();

	IJeiConfigValue<Boolean> searchIngredientAliases();

	IJeiConfigValue<Boolean> searchShortModNames();
}
