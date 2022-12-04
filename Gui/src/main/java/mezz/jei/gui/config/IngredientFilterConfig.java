package mezz.jei.gui.config;

import mezz.jei.core.config.file.IConfigCategoryBuilder;
import mezz.jei.core.config.file.IConfigSchemaBuilder;
import mezz.jei.core.search.SearchMode;

import java.util.function.Supplier;

public class IngredientFilterConfig implements IIngredientFilterConfig {
	public final Supplier<SearchMode> modNameSearchMode;
	public final Supplier<SearchMode> tooltipSearchMode;
	public final Supplier<SearchMode> tagSearchMode;
	public final Supplier<SearchMode> creativeTabSearchMode;
	public final Supplier<SearchMode> colorSearchMode;
	public final Supplier<SearchMode> resourceLocationSearchMode;
	public final Supplier<Boolean> searchAdvancedTooltips;

	public IngredientFilterConfig(IConfigSchemaBuilder builder) {
		IConfigCategoryBuilder search = builder.addCategory("search");
		modNameSearchMode = search.addEnum(
			"ModNameSearchMode",
			SearchMode.REQUIRE_PREFIX,
			"Search mode for Mod Names (prefix: @)"
		);
		tooltipSearchMode = search.addEnum(
			"TooltipSearchMode",
			SearchMode.ENABLED,
			"Search mode for Tooltips (prefix: #)"
		);
		tagSearchMode = search.addEnum(
			"TagSearchMode",
			SearchMode.REQUIRE_PREFIX,
			"Search mode for Tag Names (prefix: $)"
		);
		creativeTabSearchMode = search.addEnum(
			"CreativeTabSearchMode",
			SearchMode.DISABLED,
			"Search mode for Creative Tab Names (prefix: %)"
		);
		colorSearchMode = search.addEnum(
			"ColorSearchMode",
			SearchMode.DISABLED,
			"Search mode for Colors (prefix: ^)"
		);
		resourceLocationSearchMode = search.addEnum(
			"ResourceLocationSearchMode",
			SearchMode.DISABLED,
			"Search mode for resources locations (prefix: &)"
		);
		searchAdvancedTooltips = search.addBoolean(
			"SearchAdvancedTooltips",
			false,
			"Search advanced tooltips (visible with F3+H)"
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
	public SearchMode getCreativeTabSearchMode() {
		return creativeTabSearchMode.get();
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
	public boolean getSearchAdvancedTooltips() {
		return searchAdvancedTooltips.get();
	}

}
