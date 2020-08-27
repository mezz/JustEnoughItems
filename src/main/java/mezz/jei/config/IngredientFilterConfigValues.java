package mezz.jei.config;

import mezz.jei.api.constants.ModIds;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class IngredientFilterConfigValues {
	/*
	public SearchMode modNameSearchMode = SearchMode.REQUIRE_PREFIX;
	public SearchMode tooltipSearchMode = SearchMode.ENABLED;
	public SearchMode tagSearchMode = SearchMode.REQUIRE_PREFIX;
	public SearchMode creativeTabSearchMode = SearchMode.REQUIRE_PREFIX;
	public SearchMode colorSearchMode = SearchMode.REQUIRE_PREFIX;
	public SearchMode resourceIdSearchMode = SearchMode.REQUIRE_PREFIX;
	public boolean searchAdvancedTooltips = false;*/

	public ForgeConfigSpec.EnumValue<SearchMode> modNameSearchMode;
	public ForgeConfigSpec.EnumValue<SearchMode> tooltipSearchMode;
	public ForgeConfigSpec.EnumValue<SearchMode> tagSearchMode;
	public ForgeConfigSpec.EnumValue<SearchMode> creativeTabSearchMode;
	public ForgeConfigSpec.EnumValue<SearchMode> colorSearchMode;
	public ForgeConfigSpec.EnumValue<SearchMode> resourceIdSearchMode;
	public ForgeConfigSpec.BooleanValue searchAdvancedTooltips;

	public IngredientFilterConfigValues(ForgeConfigSpec.Builder builder) {
		builder.push("filter");
		modNameSearchMode = builder.defineEnum("modNameSearchMode", SearchMode.REQUIRE_PREFIX);
		tooltipSearchMode = builder.defineEnum("tooltipSearchMode", SearchMode.ENABLED);
		tagSearchMode = builder.defineEnum("tagSearchMode", SearchMode.REQUIRE_PREFIX);
		creativeTabSearchMode = builder.defineEnum("creativeTabSearchMode", SearchMode.REQUIRE_PREFIX);
		colorSearchMode = builder.defineEnum("colorSearchMode", SearchMode.REQUIRE_PREFIX);
		resourceIdSearchMode = builder.defineEnum("resourceIdSearchMode", SearchMode.REQUIRE_PREFIX);
		searchAdvancedTooltips = builder.define("searchAdvancedTooltips", false);
		builder.pop();
	}
}
