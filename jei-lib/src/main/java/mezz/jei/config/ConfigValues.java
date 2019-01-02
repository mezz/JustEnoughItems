package mezz.jei.config;

import mezz.jei.util.GiveMode;

public class ConfigValues {
	// advanced
	public boolean debugModeEnabled = false;
	public boolean centerSearchBarEnabled = false;
	public GiveMode giveMode = GiveMode.MOUSE_PICKUP;
	public String modNameFormat = ClientConfig.parseFriendlyModNameFormat(ClientConfig.defaultModNameFormatFriendly);
	public int maxColumns = 100;
	public int maxRecipeGuiHeight = 350;

	// search
	public SearchMode modNameSearchMode = SearchMode.REQUIRE_PREFIX;
	public SearchMode tooltipSearchMode = SearchMode.ENABLED;
	public SearchMode tagSearchMode = SearchMode.DISABLED;
	public SearchMode creativeTabSearchMode = SearchMode.DISABLED;
	public SearchMode colorSearchMode = SearchMode.DISABLED;
	public SearchMode resourceIdSearchMode = SearchMode.DISABLED;
	public boolean searchAdvancedTooltips = false;

	// per-world
	public boolean overlayEnabled = true;
	public boolean cheatItemsEnabled = false;
	public boolean hideModeEnabled = false;
	public boolean bookmarkOverlayEnabled = true;
	public String filterText = "";
}
