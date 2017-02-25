package mezz.jei.config;

public class ConfigValues {
	// advanced
	public boolean debugModeEnabled = false;
	public boolean centerSearchBarEnabled = false;
	public final String modNameFormatFriendly = "blue italic";
	public String modNameFormat = Config.parseFriendlyModNameFormat(modNameFormatFriendly);

	// search
	public Config.SearchMode modNameSearchMode = Config.SearchMode.REQUIRE_PREFIX;
	public Config.SearchMode tooltipSearchMode = Config.SearchMode.ENABLED;
	public Config.SearchMode oreDictSearchMode = Config.SearchMode.REQUIRE_PREFIX;
	public Config.SearchMode creativeTabSearchMode = Config.SearchMode.REQUIRE_PREFIX;
	public Config.SearchMode colorSearchMode = Config.SearchMode.DISABLED;

	// per-world
	public boolean overlayEnabled = true;
	public boolean cheatItemsEnabled = false;
	public boolean editModeEnabled = false;
	public String filterText = "";
}
