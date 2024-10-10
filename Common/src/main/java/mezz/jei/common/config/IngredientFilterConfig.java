package mezz.jei.common.config;

import mezz.jei.common.config.file.IConfigCategoryBuilder;
import mezz.jei.common.config.file.IConfigSchemaBuilder;
import mezz.jei.core.search.SearchMode;

import java.util.function.Supplier;

public class IngredientFilterConfig implements IIngredientFilterConfig {
	public final Supplier<SearchMode> modNameSearchMode;
	public final Supplier<SearchMode> tooltipSearchMode;
	public final Supplier<SearchMode> tagSearchMode;
	public final Supplier<SearchMode> colorSearchMode;
	public final Supplier<SearchMode> resourceLocationSearchMode;
	public final Supplier<SearchMode> creativeTabSearchMode;
	public final Supplier<Boolean> searchAdvancedTooltips;
	public final Supplier<Boolean> searchModIds;
	public final Supplier<Boolean> searchModAliases;
	public final Supplier<Boolean> searchShortModNames;
	public final Supplier<Boolean> searchIngredientAliases;

	public IngredientFilterConfig(IConfigSchemaBuilder builder) {
		IConfigCategoryBuilder search = builder.addCategory("search");
		modNameSearchMode = search.addEnum("modNameSearchMode", SearchMode.REQUIRE_PREFIX);
		tooltipSearchMode = search.addEnum("tooltipSearchMode", SearchMode.ENABLED);
		tagSearchMode = search.addEnum("tagSearchMode", SearchMode.REQUIRE_PREFIX);
		colorSearchMode = search.addEnum("colorSearchMode", SearchMode.DISABLED);
		resourceLocationSearchMode = search.addEnum("resourceLocationSearchMode", SearchMode.DISABLED);
		creativeTabSearchMode = search.addEnum("creativeTabSearchMode", SearchMode.DISABLED);
		searchAdvancedTooltips = search.addBoolean("searchAdvancedTooltips", false);
		searchModIds = search.addBoolean("searchModIds", true);
		searchModAliases = search.addBoolean("searchModAliases", true);
		searchShortModNames = search.addBoolean("searchShortModNames", false);
		searchIngredientAliases = search.addBoolean("searchIngredientAliases", true);
	}

	@Override
	public SearchMode getModNameSearchMode() {
		return modNameSearchMode.get();
	}

	@Override
	public SearchMode getTooltipSearchMode() {
		return tooltipSearchMode.get();
	}

	@Override
	public SearchMode getTagSearchMode() {
		return tagSearchMode.get();
	}

	@Override
	public SearchMode getColorSearchMode() {
		return colorSearchMode.get();
	}

	@Override
	public SearchMode getResourceLocationSearchMode() {
		return resourceLocationSearchMode.get();
	}

	@Override
	public SearchMode getCreativeTabSearchMode() {
		return creativeTabSearchMode.get();
	}

	@Override
	public boolean getSearchAdvancedTooltips() {
		return searchAdvancedTooltips.get();
	}

	@Override
	public boolean getSearchModIds() {
		return searchModIds.get();
	}

	@Override
	public boolean getSearchModAliases() {
		return searchModAliases.get();
	}

	@Override
	public boolean getSearchIngredientAliases() {
		return searchIngredientAliases.get();
	}

	@Override
	public boolean getSearchShortModNames() {
		return searchShortModNames.get();
	}
}
