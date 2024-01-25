package mezz.jei.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class IngredientFilterConfig implements IIngredientFilterConfig {
	// Forge config
	public final ForgeConfigSpec.EnumValue<SearchMode> modNameSearchMode;
	public final ForgeConfigSpec.EnumValue<SearchMode> tooltipSearchMode;
	public final ForgeConfigSpec.EnumValue<SearchMode> tagSearchMode;
	public final ForgeConfigSpec.EnumValue<SearchMode> creativeTabSearchMode;
	public final ForgeConfigSpec.EnumValue<SearchMode> colorSearchMode;
	public final ForgeConfigSpec.EnumValue<SearchMode> resourceIdSearchMode;
	public final ForgeConfigSpec.BooleanValue searchAdvancedTooltips;
	public final ForgeConfigSpec.BooleanValue searchIngredientAliases;
	public final ForgeConfigSpec.BooleanValue searchModAliases;

	public IngredientFilterConfig(ForgeConfigSpec.Builder builder) {
		builder.push("search");
		builder.comment("Search mode for mod names (prefix: @).");
		modNameSearchMode = builder.defineEnum("ModNameSearchMode", SearchMode.REQUIRE_PREFIX);
		builder.comment("Search mode for tooltips (prefix: #).");
		tooltipSearchMode = builder.defineEnum("TooltipSearchMode", SearchMode.ENABLED);
		builder.comment("Search mode for tags (prefix: $).");
		tagSearchMode = builder.defineEnum("TagSearchMode", SearchMode.REQUIRE_PREFIX);
		builder.comment("Search mode for creative mode tab names (prefix: %).");
		creativeTabSearchMode = builder.defineEnum("CreativeTabSearchMode", SearchMode.DISABLED);
		builder.comment("Search mode for colors (prefix: ^).");
		colorSearchMode = builder.defineEnum("ColorSearchMode", SearchMode.DISABLED);
		builder.comment("Search mode for resource IDs (prefix: &).");
		resourceIdSearchMode = builder.defineEnum("ResourceIdSearchMode", SearchMode.DISABLED);
		builder.comment("Search in advanced tooltips (visible with F3 + H).");
		searchAdvancedTooltips = builder.define("SearchAdvancedTooltips", false);
		builder.comment("Search ingredient aliases (alternative names) that are added by plugins.");
		searchIngredientAliases = builder.define("SearchIngredientAliases", true);
		builder.comment("Search mod aliases in addition to mod names.");
		searchModAliases = builder.define("SearchModAliases", true);
		builder.pop();
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
	public SearchMode getResourceIdSearchMode() {
		return resourceIdSearchMode.get();
	}

	@Override
	public boolean getSearchAdvancedTooltips() {
		return searchAdvancedTooltips.get();
	}

	@Override
	public boolean getSearchIngredientAliases() {
		return searchIngredientAliases.get();
	}

	@Override
	public boolean getSearchModAliases() {
		return searchModAliases.get();
	}

}
