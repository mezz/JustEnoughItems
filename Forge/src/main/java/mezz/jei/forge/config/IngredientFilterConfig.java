package mezz.jei.forge.config;

import mezz.jei.core.config.IIngredientFilterConfig;
import mezz.jei.core.search.SearchMode;
import net.minecraftforge.common.ForgeConfigSpec;

public class IngredientFilterConfig implements IIngredientFilterConfig {
    private final ForgeConfigSpec.EnumValue<SearchMode> modNameSearchMode;
    private final ForgeConfigSpec.EnumValue<SearchMode> tooltipSearchMode;
    private final ForgeConfigSpec.EnumValue<SearchMode> tagSearchMode;
    private final ForgeConfigSpec.EnumValue<SearchMode> creativeTabSearchMode;
    private final ForgeConfigSpec.EnumValue<SearchMode> colorSearchMode;
    private final ForgeConfigSpec.EnumValue<SearchMode> resourceLocationSearchMode;
    private final ForgeConfigSpec.BooleanValue searchAdvTooltips;

    public IngredientFilterConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Search mode for Mod Names (prefix: @)");
        modNameSearchMode = builder.defineEnum("mod_name_search_mode", SearchMode.REQUIRE_PREFIX);

        builder.comment("Search mode for Tooltips (prefix: #)");
        tooltipSearchMode = builder.defineEnum("tooltip_search_mode", SearchMode.ENABLED);

        builder.comment("Search mode for Tag Names (prefix: $)");
        tagSearchMode = builder.defineEnum("tag_search_mode", SearchMode.REQUIRE_PREFIX);

        builder.comment("Search mode for Creative Tab Names (prefix: %)");
        creativeTabSearchMode = builder.defineEnum("creative_tab_search_mode", SearchMode.DISABLED);

        builder.comment("Search mode for Colors (prefix: ^)");
        colorSearchMode = builder.defineEnum("color_search_mode", SearchMode.DISABLED);

        builder.comment("Search mode for resources locations (prefix: &)");
        resourceLocationSearchMode = builder.defineEnum("resource_location_search_mode", SearchMode.DISABLED);

        builder.comment("Search advanced tooltips (visible with F3+H)");
        searchAdvTooltips = builder.define("search_adv_tooltips", false);
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
        return searchAdvTooltips.get();
    }
}
