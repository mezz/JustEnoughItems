package mezz.jei.config;

import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.config.NameMap;
import net.minecraftforge.common.ForgeConfigSpec;

public class IngredientFilterConfig implements IIngredientFilterConfig, IJEIConfig {
	private final IngredientFilterConfigValues values;

	// Forge config
	public final ForgeConfigSpec.EnumValue<SearchMode> modNameSearchMode;
	public final ForgeConfigSpec.EnumValue<SearchMode> tooltipSearchMode;
	public final ForgeConfigSpec.EnumValue<SearchMode> tagSearchMode;
	public final ForgeConfigSpec.EnumValue<SearchMode> creativeTabSearchMode;
	public final ForgeConfigSpec.EnumValue<SearchMode> colorSearchMode;
	public final ForgeConfigSpec.EnumValue<SearchMode> resourceIdSearchMode;
	public final ForgeConfigSpec.BooleanValue searchAdvancedTooltips;

	public IngredientFilterConfig(ForgeConfigSpec.Builder builder) {
		this.values = new IngredientFilterConfigValues();
		IngredientFilterConfigValues defaultValues = new IngredientFilterConfigValues();

		builder.push("search");
		builder.comment("Search mode for Mod Names (prefix: @)");
		modNameSearchMode = builder.defineEnum("ModNameSearchMode", defaultValues.modNameSearchMode);
		builder.comment("Search mode for Tooltips (prefix: #)");
		tooltipSearchMode = builder.defineEnum("TooltipSearchMode", defaultValues.tooltipSearchMode);
		builder.comment("Search mode for Tag Names (prefix: $)");
		tagSearchMode = builder.defineEnum("TagSearchMode", defaultValues.tagSearchMode);
		builder.comment("Search mode for Creative Tab Names (prefix: %)");
		creativeTabSearchMode = builder.defineEnum("CreativeTabSearchMode", defaultValues.creativeTabSearchMode);
		builder.comment("Search mode for Colors (prefix: ^)");
		colorSearchMode = builder.defineEnum("ColorSearchMode", defaultValues.colorSearchMode);
		builder.comment("Search mode for resources IDs (prefix: &)");
		resourceIdSearchMode = builder.defineEnum("ResourceIdSearchMode", defaultValues.resourceIdSearchMode);
		builder.comment("Search advanced tooltips (visible with F3+H)");
		searchAdvancedTooltips = builder.define("SearchAdvancedTooltips", defaultValues.searchAdvancedTooltips);
		builder.pop();
	}

	@Override
	public void buildSettingsGUI(ConfigGroup group) {
		IngredientFilterConfigValues defaultValues = new IngredientFilterConfigValues();

		group.addEnum(cfgTranslation("modNameSearchMode"), values.modNameSearchMode, v -> {
			modNameSearchMode.set(v);
			values.modNameSearchMode = v;
		}, NameMap.of(defaultValues.modNameSearchMode, SearchMode.values()).create());
		group.addEnum(cfgTranslation("tooltipSearchMode"), values.tooltipSearchMode, v -> {
			tooltipSearchMode.set(v);
			values.tooltipSearchMode = v;
		}, NameMap.of(defaultValues.tooltipSearchMode, SearchMode.values()).create());
		group.addEnum(cfgTranslation("tagSearchMode"), values.tagSearchMode, v -> {
			tagSearchMode.set(v);
			values.tagSearchMode = v;
		}, NameMap.of(defaultValues.tagSearchMode, SearchMode.values()).create());
		group.addEnum(cfgTranslation("creativeTabSearchMode"), values.creativeTabSearchMode, v -> {
			creativeTabSearchMode.set(v);
			values.creativeTabSearchMode = v;
		}, NameMap.of(defaultValues.creativeTabSearchMode, SearchMode.values()).create());
		group.addEnum(cfgTranslation("colorSearchMode"), values.colorSearchMode, v -> {
			colorSearchMode.set(v);
			values.colorSearchMode = v;
		}, NameMap.of(defaultValues.colorSearchMode, SearchMode.values()).create());
		group.addEnum(cfgTranslation("resourceIdSearchMode"), values.resourceIdSearchMode, v -> {
			resourceIdSearchMode.set(v);
			values.resourceIdSearchMode = v;
		}, NameMap.of(defaultValues.resourceIdSearchMode, SearchMode.values()).create());
		group.addBool(cfgTranslation("searchAdvancedTooltips"), values.searchAdvancedTooltips, v -> {
			searchAdvancedTooltips.set(v);
			values.searchAdvancedTooltips = v;
		}, defaultValues.searchAdvancedTooltips);
	}

	private String cfgTranslation(String name) {
		return "search."+name;
	}

	@Override
	public void reload() {
		values.modNameSearchMode = modNameSearchMode.get();
		values.tooltipSearchMode = tooltipSearchMode.get();
		values.tagSearchMode = tagSearchMode.get();
		values.creativeTabSearchMode = creativeTabSearchMode.get();
		values.colorSearchMode = colorSearchMode.get();
		values.resourceIdSearchMode = resourceIdSearchMode.get();
		values.searchAdvancedTooltips = searchAdvancedTooltips.get();
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

}
