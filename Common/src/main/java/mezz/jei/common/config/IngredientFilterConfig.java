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
	public final Supplier<Boolean> searchShortModNames;
	public final Supplier<Boolean> searchIngredientAliases;

	public IngredientFilterConfig(IConfigSchemaBuilder builder) {
		IConfigCategoryBuilder search = builder.addCategory("search");
		modNameSearchMode = search.addEnum(
			"ModNameSearchMode",
			SearchMode.REQUIRE_PREFIX,
			"Search mode for mod names (prefix: @)."
		);
		tooltipSearchMode = search.addEnum(
			"TooltipSearchMode",
			SearchMode.ENABLED,
			"Search mode for tooltips (prefix: #)."
		);
		tagSearchMode = search.addEnum(
			"TagSearchMode",
			SearchMode.REQUIRE_PREFIX,
			"Search mode for tags (prefix: $)."
		);
		colorSearchMode = search.addEnum(
			"ColorSearchMode",
			SearchMode.DISABLED,
			"Search mode for colors (prefix: ^)."
		);
		resourceLocationSearchMode = search.addEnum(
			"ResourceLocationSearchMode",
			SearchMode.DISABLED,
			"Search mode for resource locations (prefix: &)."
		);
		creativeTabSearchMode = search.addEnum(
			"CreativeTabSearchMode",
			SearchMode.DISABLED,
			"Search mode for creative mode tab names (prefix: %)."
		);
		searchAdvancedTooltips = search.addBoolean(
			"SearchAdvancedTooltips",
			false,
			"Search in advanced tooltips (visible with F3 + H)."
		);
		searchModIds = search.addBoolean(
			"SearchModIds",
			true,
			"Search mod IDs in addition to mod names."
		);
		searchShortModNames = search.addBoolean(
			"SearchShortModNames",
			true,
			"Search by the shorthand first letters of a mod's name."
		);
		searchIngredientAliases = search.addBoolean(
			"SearchIngredientAliases",
			true,
			"Search ingredient aliases (alternative names) that are added by plugins, in addition to ingredient names."
		);
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
	public boolean getSearchIngredientAliases() {
		return searchIngredientAliases.get();
	}

	@Override
	public boolean getSearchShortModNames() {
		return searchShortModNames.get();
	}
}
