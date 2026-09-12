package mezz.jei.common.config;

import net.mezzdev.config.api.value.IConfigValue;

public interface IIngredientFilterConfig {
	IConfigValue<SearchMode> modNameSearchMode();

	IConfigValue<SearchMode> tooltipSearchMode();

	IConfigValue<SearchMode> tagSearchMode();

	IConfigValue<SearchMode> colorSearchMode();

	IConfigValue<SearchMode> identifierSearchMode();

	IConfigValue<SearchMode> creativeTabSearchMode();

	IConfigValue<Boolean> searchAdvancedTooltips();

	IConfigValue<Boolean> searchModIds();

	IConfigValue<Boolean> searchModAliases();

	IConfigValue<Boolean> searchIngredientAliases();

	IConfigValue<Boolean> searchShortModNames();
}
