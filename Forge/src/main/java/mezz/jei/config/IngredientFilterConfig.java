package mezz.jei.config;

import mezz.jei.core.search.SearchMode;
import net.minecraftforge.common.ForgeConfigSpec;

public class IngredientFilterConfig implements IIngredientFilterConfig {
	public final ForgeConfigSpec.EnumValue<SearchMode> modNameSearchMode;
	public final ForgeConfigSpec.EnumValue<SearchMode> tooltipSearchMode;
	public final ForgeConfigSpec.EnumValue<SearchMode> tagSearchMode;
	public final ForgeConfigSpec.EnumValue<SearchMode> creativeTabSearchMode;
	public final ForgeConfigSpec.EnumValue<SearchMode> colorSearchMode;
	public final ForgeConfigSpec.EnumValue<SearchMode> resourceLocationSearchMode;
	public final ForgeConfigSpec.BooleanValue searchAdvancedTooltips;

	public IngredientFilterConfig(ForgeConfigSpec.Builder builder) {
		builder.push("search");
		{
			builder.comment("Search mode for Mod Names (prefix: @)");
			modNameSearchMode = builder.defineEnum("ModNameSearchMode", SearchMode.REQUIRE_PREFIX);
			builder.comment("Search mode for Tooltips (prefix: #)");
			tooltipSearchMode = builder.defineEnum("TooltipSearchMode", SearchMode.ENABLED);
			builder.comment("Search mode for Tag Names (prefix: $)");
			tagSearchMode = builder.defineEnum("TagSearchMode", SearchMode.REQUIRE_PREFIX);
			builder.comment("Search mode for Creative Tab Names (prefix: %)");
			creativeTabSearchMode = builder.defineEnum("CreativeTabSearchMode", SearchMode.DISABLED);
			builder.comment("Search mode for Colors (prefix: ^)");
			colorSearchMode = builder.defineEnum("ColorSearchMode", SearchMode.DISABLED);
			builder.comment("Search mode for resources locations (prefix: &)");
			resourceLocationSearchMode = builder.defineEnum("ResourceLocationSearchMode", SearchMode.DISABLED);
			builder.comment("Search advanced tooltips (visible with F3+H)");
			searchAdvancedTooltips = builder.define("SearchAdvancedTooltips", false);
		}
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
	public SearchMode getResourceLocationSearchMode() {
		return resourceLocationSearchMode.get();
	}

	@Override
	public boolean getSearchAdvancedTooltips() {
		return searchAdvancedTooltips.get();
	}

}
