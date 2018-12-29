package mezz.jei.config;

/** Search settings shared by the configured filter and its indexing tasks. */
public class IngredientFilterConfig implements IIngredientFilterConfig {
	private final ConfigValues values;

	public IngredientFilterConfig(ConfigValues values) {
		this.values = values;
	}

	@Override
	public SearchMode getModNameSearchMode() {
		return SearchMode.valueOf(values.modNameSearchMode.name());
	}

	@Override
	public SearchMode getTooltipSearchMode() {
		return SearchMode.valueOf(values.tooltipSearchMode.name());
	}

	@Override
	public SearchMode getOreDictSearchMode() {
		return SearchMode.valueOf(values.oreDictSearchMode.name());
	}

	@Override
	public SearchMode getCreativeTabSearchMode() {
		return SearchMode.valueOf(values.creativeTabSearchMode.name());
	}

	@Override
	public SearchMode getColorSearchMode() {
		return SearchMode.valueOf(values.colorSearchMode.name());
	}

	@Override
	public SearchMode getResourceIdSearchMode() {
		return SearchMode.valueOf(values.resourceIdSearchMode.name());
	}

	@Override
	public boolean getSearchAdvancedTooltips() {
		return values.searchAdvancedTooltips;
	}
}
