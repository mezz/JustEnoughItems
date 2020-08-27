package mezz.jei.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class IngredientFilterConfig implements IIngredientFilterConfig {
	public static final String CATEGORY_SEARCH = "search";

	private final IngredientFilterConfigValues values;
	//private final LocalizedConfiguration config;

	public IngredientFilterConfig(ForgeConfigSpec.Builder builder) {
		this.values = new IngredientFilterConfigValues(builder);
	}

	@Override
	public SearchMode getModNameSearchMode() {
		return values.modNameSearchMode.get();
	}

	@Override
	public SearchMode getTooltipSearchMode() {
		return values.tooltipSearchMode.get();
	}

	@Override
	public SearchMode getTagSearchMode() {
		return values.tagSearchMode.get();
	}

	@Override
	public SearchMode getCreativeTabSearchMode() {
		return values.creativeTabSearchMode.get();
	}

	@Override
	public SearchMode getColorSearchMode() {
		return values.colorSearchMode.get();
	}

	@Override
	public SearchMode getResourceIdSearchMode() {
		return values.resourceIdSearchMode.get();
	}

	@Override
	public boolean getSearchAdvancedTooltips() {
		return values.searchAdvancedTooltips.get();
	}

	public boolean syncConfig() {
		boolean needsReload = false;
		/*
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
		}*/
		return needsReload;
	}

}
