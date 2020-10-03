package mezz.jei.config;

public class IngredientFilterConfigValues {
	public SearchMode modNameSearchMode = SearchMode.REQUIRE_PREFIX;
	public SearchMode tooltipSearchMode = SearchMode.ENABLED;
	public SearchMode tagSearchMode = SearchMode.REQUIRE_PREFIX;
	public SearchMode creativeTabSearchMode = SearchMode.DISABLED;
	public SearchMode colorSearchMode = SearchMode.DISABLED;
	public SearchMode resourceIdSearchMode = SearchMode.DISABLED;
	public boolean searchAdvancedTooltips = false;
}
