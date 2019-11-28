package mezz.jei.config;

import mezz.jei.util.GiveMode;

public class ConfigValues {
	// advanced
	public boolean debugModeEnabled = false;
	public boolean centerSearchBarEnabled = false;
	public boolean optimizeMemoryUsage = true;
	public GiveMode giveMode = GiveMode.MOUSE_PICKUP;
	public String modNameFormat = Config.parseFriendlyModNameFormat(Config.defaultModNameFormatFriendly);
	public int maxColumns = 100;
	public int maxRecipeGuiHeight = 350;

	// search
	public Config.SearchMode modNameSearchMode = Config.SearchMode.REQUIRE_PREFIX;
	public Config.SearchMode tooltipSearchMode = Config.SearchMode.ENABLED;
	public Config.SearchMode oreDictSearchMode = Config.SearchMode.DISABLED;
	public Config.SearchMode creativeTabSearchMode = Config.SearchMode.DISABLED;
	public Config.SearchMode colorSearchMode = Config.SearchMode.DISABLED;
	public Config.SearchMode resourceIdSearchMode = Config.SearchMode.DISABLED;
	public boolean searchAdvancedTooltips = false;

	// per-world
	public boolean overlayEnabled = true;
	public boolean cheatItemsEnabled = false;
	public boolean editModeEnabled = false;
	public boolean bookmarkOverlayEnabled = true;
	public String filterText = "";
}
