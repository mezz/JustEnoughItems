package mezz.jei.config;

import mezz.jei.util.GiveMode;

public class ConfigValues extends IngredientFilterConfigValues {
	// advanced
	public boolean debugModeEnabled = false;
	public boolean centerSearchBarEnabled = false;
	public boolean hideIngredientsWhenNotSearching = false;
	public boolean optimizeMemoryUsage = true;
	public boolean addBookmarksToFront = true;
	public GiveMode giveMode = GiveMode.MOUSE_PICKUP;
	public String modNameFormat = ModIdFormattingConfig.parseFriendlyModNameFormat(ModIdFormattingConfig.defaultModNameFormatFriendly);
	public int maxColumns = 9;
	public int maxRecipeGuiHeight = 350;

	// per-world
	public boolean overlayEnabled = true;
	public boolean cheatItemsEnabled = false;
	public boolean editModeEnabled = false;
	public boolean bookmarkOverlayEnabled = true;
	public String filterText = "";
}
