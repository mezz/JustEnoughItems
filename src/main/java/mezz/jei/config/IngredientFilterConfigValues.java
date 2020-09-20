package mezz.jei.config;

import mezz.jei.api.constants.ModIds;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class IngredientFilterConfigValues {
	public SearchMode modNameSearchMode = SearchMode.REQUIRE_PREFIX;
	public SearchMode tooltipSearchMode = SearchMode.ENABLED;
	public SearchMode tagSearchMode = SearchMode.REQUIRE_PREFIX;
	public SearchMode creativeTabSearchMode = SearchMode.REQUIRE_PREFIX;
	public SearchMode colorSearchMode = SearchMode.REQUIRE_PREFIX;
	public SearchMode resourceIdSearchMode = SearchMode.REQUIRE_PREFIX;
	public boolean searchAdvancedTooltips = false;
}
