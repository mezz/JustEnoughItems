package mezz.jei.config;

import mezz.jei.util.GiveMode;

public class ConfigValues {
	// advanced
	public boolean debugModeEnabled = false;
	public boolean centerSearchBarEnabled = false;
	public GiveMode giveMode = GiveMode.MOUSE_PICKUP;
	public final String modNameFormatFriendly = "blue italic";
	public String modNameFormat = Config.parseFriendlyModNameFormat(modNameFormatFriendly);
	public int maxSubtypes = 300;
	public int maxColumns = 100;

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
	public String filterText = "";
}
