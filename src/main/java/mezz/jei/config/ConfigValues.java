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
	public ClientConfig.SearchMode modNameSearchMode = ClientConfig.SearchMode.REQUIRE_PREFIX;
	public ClientConfig.SearchMode tooltipSearchMode = ClientConfig.SearchMode.ENABLED;
	public ClientConfig.SearchMode oreDictSearchMode = ClientConfig.SearchMode.DISABLED;
	public ClientConfig.SearchMode creativeTabSearchMode = ClientConfig.SearchMode.DISABLED;
	public ClientConfig.SearchMode colorSearchMode = ClientConfig.SearchMode.DISABLED;
	public ClientConfig.SearchMode resourceIdSearchMode = ClientConfig.SearchMode.DISABLED;
	public boolean searchAdvancedTooltips = false;

	// per-world
	public boolean overlayEnabled = true;
	public boolean cheatItemsEnabled = false;
	public boolean hideModeEnabled = false;
	public boolean bookmarkOverlayEnabled = true;
	public String filterText = "";
}
