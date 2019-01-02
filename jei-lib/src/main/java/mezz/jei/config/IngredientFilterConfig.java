package mezz.jei.config;

public class IngredientFilterConfig implements IIngredientFilterConfig {
	public static final String CATEGORY_SEARCH = "search";

	private final IngredientFilterConfigValues defaultValues = new IngredientFilterConfigValues();
	private final IngredientFilterConfigValues values = new IngredientFilterConfigValues();
	private final LocalizedConfiguration config;

	public IngredientFilterConfig(LocalizedConfiguration config) {
		this.config = config;
	}

	@Override
	public SearchMode getModNameSearchMode() {
		return values.modNameSearchMode;
	}

	@Override
	public SearchMode getTooltipSearchMode() {
		return values.tooltipSearchMode;
	}

	@Override
	public SearchMode getTagSearchMode() {
		return values.tagSearchMode;
	}

	@Override
	public SearchMode getCreativeTabSearchMode() {
		return values.creativeTabSearchMode;
	}

	@Override
	public SearchMode getColorSearchMode() {
		return values.colorSearchMode;
	}

	@Override
	public SearchMode getResourceIdSearchMode() {
		return values.resourceIdSearchMode;
	}

	@Override
	public boolean getSearchAdvancedTooltips() {
		return values.searchAdvancedTooltips;
	}

	public boolean syncConfig() {
		boolean needsReload = false;

		config.addCategory(CATEGORY_SEARCH);

		SearchMode[] searchModes = SearchMode.values();

		values.modNameSearchMode = config.getEnum("modNameSearchMode", CATEGORY_SEARCH, defaultValues.modNameSearchMode, searchModes);
		values.tooltipSearchMode = config.getEnum("tooltipSearchMode", CATEGORY_SEARCH, defaultValues.tooltipSearchMode, searchModes);
		values.tagSearchMode = config.getEnum("tagSearchMode", CATEGORY_SEARCH, defaultValues.tagSearchMode, searchModes);
		values.creativeTabSearchMode = config.getEnum("creativeTabSearchMode", CATEGORY_SEARCH, defaultValues.creativeTabSearchMode, searchModes);
		values.colorSearchMode = config.getEnum("colorSearchMode", CATEGORY_SEARCH, defaultValues.colorSearchMode, searchModes);
		values.resourceIdSearchMode = config.getEnum("resourceIdSearchMode", CATEGORY_SEARCH, defaultValues.resourceIdSearchMode, searchModes);
		if (config.getCategory(CATEGORY_SEARCH).hasChanged()) {
			needsReload = true;
		}

		values.searchAdvancedTooltips = config.getBoolean("searchAdvancedTooltips", CATEGORY_SEARCH, defaultValues.searchAdvancedTooltips);

		final boolean configChanged = config.hasChanged();
		if (configChanged) {
			// TODO 1.13
//			config.save();
		}
		return needsReload;
	}

}
