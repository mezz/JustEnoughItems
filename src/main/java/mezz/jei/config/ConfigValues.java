package mezz.jei.config;

public class ConfigValues {
	// advanced
	public boolean debugModeEnabled = false;
	public boolean centerSearchBarEnabled = false;
	public final String modNameFormatFriendly = "blue italic";
	public String modNameFormat = Config.parseFriendlyModNameFormat(modNameFormatFriendly);
	public int maxSubtypes = 300;
	public String itemSortlist = "invtweaks,minecraft,mod,id,default,meta,name";
	public String defaultItemSortlist = "invtweaks,minecraft,mod,id,default,meta,name";

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
