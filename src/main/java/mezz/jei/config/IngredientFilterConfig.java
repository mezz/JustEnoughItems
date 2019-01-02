package mezz.jei.config;

import net.minecraftforge.common.config.ConfigCategory;

import static mezz.jei.config.Config.CATEGORY_SEARCH;

/** Search settings shared by the configured filter and its indexing tasks. */
public class IngredientFilterConfig implements IIngredientFilterConfig {
	private final IngredientFilterConfigValues values;
	private final IngredientFilterConfigValues defaultValues = new IngredientFilterConfigValues();

	public IngredientFilterConfig(ConfigValues values) {
		this((IngredientFilterConfigValues) values);
	}

	public IngredientFilterConfig(IngredientFilterConfigValues values) {
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

	public boolean syncConfig(LocalizedConfiguration config) {
		boolean needsReload = false;
		config.addCategory(CATEGORY_SEARCH);
		Config.migrateSearchAdvancedTooltipsConfig(config);
		ConfigCategory searchCategory = config.getCategory(CATEGORY_SEARCH);
		searchCategory.remove("atPrefixRequiredForModName");
		searchCategory.remove("prefixRequiredForTooltipSearch");
		searchCategory.remove("prefixRequiredForOreDictSearch");
		searchCategory.remove("prefixRequiredForCreativeTabSearch");
		searchCategory.remove("prefixRequiredForColorSearch");

		Config.SearchMode[] searchModes = Config.SearchMode.values();

		String loadedConfigVersion = config.getLoadedConfigVersion();
		// set new defaults moving to config version 0.3.0
		if (loadedConfigVersion != null && Config.versionCompare(loadedConfigVersion, "0.3.0") < 0) {
			config.setEnum("creativeTabSearchMode", CATEGORY_SEARCH, defaultValues.creativeTabSearchMode, searchModes);
			config.setEnum("oreDictSearchMode", CATEGORY_SEARCH, defaultValues.oreDictSearchMode, searchModes);
		}

		values.modNameSearchMode = config.getEnum("modNameSearchMode", CATEGORY_SEARCH, defaultValues.modNameSearchMode, searchModes);
		values.tooltipSearchMode = config.getEnum("tooltipSearchMode", CATEGORY_SEARCH, defaultValues.tooltipSearchMode, searchModes);
		values.oreDictSearchMode = config.getEnum("oreDictSearchMode", CATEGORY_SEARCH, defaultValues.oreDictSearchMode, searchModes);
		values.creativeTabSearchMode = config.getEnum("creativeTabSearchMode", CATEGORY_SEARCH, defaultValues.creativeTabSearchMode, searchModes);
		values.colorSearchMode = config.getEnum("colorSearchMode", CATEGORY_SEARCH, defaultValues.colorSearchMode, searchModes);
		values.resourceIdSearchMode = config.getEnum("resourceIdSearchMode", CATEGORY_SEARCH, defaultValues.resourceIdSearchMode, searchModes);
		if (config.getCategory(CATEGORY_SEARCH).hasChanged()) {
			needsReload = true;
		}

		values.searchAdvancedTooltips = config.getBoolean(CATEGORY_SEARCH, "searchAdvancedTooltips", defaultValues.searchAdvancedTooltips);

		return needsReload;
	}
}
