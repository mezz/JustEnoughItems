package mezz.jei.common.config;

import net.mezzdev.config.api.value.IConfigValue;
import net.mezzdev.config.api.schema.builder.IConfigCategoryBuilder;

public class IngredientFilterConfig implements IIngredientFilterConfig {
	private final IConfigValue<SearchMode> modNameSearchMode;
	private final IConfigValue<SearchMode> tooltipSearchMode;
	private final IConfigValue<SearchMode> tagSearchMode;
	private final IConfigValue<SearchMode> colorSearchMode;
	private final IConfigValue<SearchMode> resourceLocationSearchMode;
	private final IConfigValue<SearchMode> creativeTabSearchMode;
	private final IConfigValue<Boolean> searchAdvancedTooltips;
	private final IConfigValue<Boolean> searchModIds;
	private final IConfigValue<Boolean> searchModAliases;
	private final IConfigValue<Boolean> searchShortModNames;
	private final IConfigValue<Boolean> searchIngredientAliases;

	public IngredientFilterConfig(IConfigCategoryBuilder search) {
		modNameSearchMode = search.addEnum("modNameSearchMode", SearchMode.REQUIRE_PREFIX)
			.build();
		tagSearchMode = search.addEnum("tagSearchMode", SearchMode.REQUIRE_PREFIX)
			.build();
		tooltipSearchMode = search.addEnum("tooltipSearchMode", SearchMode.ENABLED)
			.build();
		colorSearchMode = search.addEnum("colorSearchMode", SearchMode.DISABLED)
			.build();
		resourceLocationSearchMode = search.addEnum("resourceLocationSearchMode", SearchMode.DISABLED)
			.build();
		creativeTabSearchMode = search.addEnum("creativeTabSearchMode", SearchMode.DISABLED)
			.build();
		searchAdvancedTooltips = search.addBoolean("searchAdvancedTooltips", false)
			.build();
		searchModIds = search.addBoolean("searchModIds", true)
			.build();
		searchModAliases = search.addBoolean("searchModAliases", true)
			.build();
		searchShortModNames = search.addBoolean("searchShortModNames", false)
			.build();
		searchIngredientAliases = search.addBoolean("searchIngredientAliases", true)
			.build();
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
