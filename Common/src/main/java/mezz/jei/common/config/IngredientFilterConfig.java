package mezz.jei.common.config;

import mezz.jei.common.config.file.ConfigValue;
import mezz.jei.common.config.file.IConfigCategoryBuilder;
import mezz.jei.common.config.file.IConfigSchemaBuilder;
import mezz.jei.common.search.SearchMode;

public class IngredientFilterConfig implements IIngredientFilterConfig {
	private final ConfigValue<SearchMode> modNameSearchMode;
	private final ConfigValue<SearchMode> tooltipSearchMode;
	private final ConfigValue<SearchMode> tagSearchMode;
	private final ConfigValue<SearchMode> colorSearchMode;
	private final ConfigValue<SearchMode> identifierSearchMode;
	private final ConfigValue<SearchMode> creativeTabSearchMode;
	private final ConfigValue<Boolean> searchAdvancedTooltips;
	private final ConfigValue<Boolean> searchModIds;
	private final ConfigValue<Boolean> searchModAliases;
	private final ConfigValue<Boolean> searchShortModNames;
	private final ConfigValue<Boolean> searchIngredientAliases;

	public IngredientFilterConfig(IConfigSchemaBuilder builder) {
		IConfigCategoryBuilder search = builder.addCategory("search");
		modNameSearchMode = search.addEnum("modNameSearchMode", SearchMode.REQUIRE_PREFIX);
		tagSearchMode = search.addEnum("tagSearchMode", SearchMode.REQUIRE_PREFIX);
		tooltipSearchMode = search.addEnum("tooltipSearchMode", SearchMode.ENABLED);
		colorSearchMode = search.addEnum("colorSearchMode", SearchMode.DISABLED);
		identifierSearchMode = search.addEnum("identifierSearchMode", SearchMode.DISABLED);
		creativeTabSearchMode = search.addEnum("creativeTabSearchMode", SearchMode.DISABLED);
		searchAdvancedTooltips = search.addBoolean("searchAdvancedTooltips", false);
		searchModIds = search.addBoolean("searchModIds", true);
		searchModAliases = search.addBoolean("searchModAliases", true);
		searchShortModNames = search.addBoolean("searchShortModNames", false);
		searchIngredientAliases = search.addBoolean("searchIngredientAliases", true);
	}

	@Override
	public ConfigValue<SearchMode> modNameSearchMode() {
		return modNameSearchMode;
	}

	@Override
	public ConfigValue<SearchMode> tooltipSearchMode() {
		return tooltipSearchMode;
	}

	@Override
	public ConfigValue<SearchMode> tagSearchMode() {
		return tagSearchMode;
	}

	@Override
	public ConfigValue<SearchMode> colorSearchMode() {
		return colorSearchMode;
	}

	@Override
	public ConfigValue<SearchMode> identifierSearchMode() {
		return identifierSearchMode;
	}

	@Override
	public ConfigValue<SearchMode> creativeTabSearchMode() {
		return creativeTabSearchMode;
	}

	@Override
	public ConfigValue<Boolean> searchAdvancedTooltips() {
		return searchAdvancedTooltips;
	}

	@Override
	public ConfigValue<Boolean> searchModIds() {
		return searchModIds;
	}

	@Override
	public ConfigValue<Boolean> searchModAliases() {
		return searchModAliases;
	}

	@Override
	public ConfigValue<Boolean> searchIngredientAliases() {
		return searchIngredientAliases;
	}

	@Override
	public ConfigValue<Boolean> searchShortModNames() {
		return searchShortModNames;
	}
}
