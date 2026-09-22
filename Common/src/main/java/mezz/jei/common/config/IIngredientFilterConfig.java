package mezz.jei.common.config;

import mezz.jei.common.Internal;
import net.mezzdev.config.api.value.IConfigValue;

import java.util.List;

public interface IIngredientFilterConfig {
	IConfigValue<SearchMode> modNameSearchMode();

	IConfigValue<SearchMode> tooltipSearchMode();

	IConfigValue<SearchMode> tagSearchMode();

	IConfigValue<SearchMode> colorSearchMode();

	IConfigValue<SearchMode> resourceLocationSearchMode();

	IConfigValue<SearchMode> creativeTabSearchMode();

	IConfigValue<Boolean> searchAdvancedTooltips();

	IConfigValue<Boolean> searchModIds();

	IConfigValue<Boolean> searchModAliases();

	IConfigValue<Boolean> searchIngredientAliases();

	IConfigValue<Boolean> searchShortModNames();

	default SearchMode getModNameSearchMode() {
		return modNameSearchMode().get();
	}

	default SearchMode getTooltipSearchMode() {
		return tooltipSearchMode().get();
	}

	default SearchMode getTagSearchMode() {
		return tagSearchMode().get();
	}

	default SearchMode getColorSearchMode() {
		return colorSearchMode().get();
	}

	default SearchMode getResourceLocationSearchMode() {
		return resourceLocationSearchMode().get();
	}

	default SearchMode getCreativeTabSearchMode() {
		return creativeTabSearchMode().get();
	}

	default boolean getSearchAdvancedTooltips() {
		return searchAdvancedTooltips().get();
	}

	default boolean getSearchModIds() {
		return searchModIds().get();
	}

	default boolean getSearchModAliases() {
		return searchModAliases().get();
	}

	default boolean getSearchIngredientAliases() {
		return searchIngredientAliases().get();
	}

	default boolean getSearchShortModNames() {
		return searchShortModNames().get();
	}

	default void addSearchConfigListener(Runnable listener) {
		List.of(
				modNameSearchMode(), tooltipSearchMode(), tagSearchMode(), colorSearchMode(),
				resourceLocationSearchMode(), creativeTabSearchMode(), searchAdvancedTooltips(),
				searchModIds(), searchModAliases(), searchIngredientAliases(), searchShortModNames()
			)
			.forEach(value -> Internal.registerRuntimeListenerRemoval(value.addListener(change -> listener.run())));
	}
}
