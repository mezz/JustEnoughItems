package mezz.jei.config;

public class IngredientFilterConfigValues {
	public SearchMode modNameSearchMode = SearchMode.REQUIRE_PREFIX;
	public SearchMode tooltipSearchMode = SearchMode.ENABLED;
	public SearchMode tagSearchMode = SearchMode.REQUIRE_PREFIX;
	public SearchMode creativeTabSearchMode = SearchMode.REQUIRE_PREFIX;
	public SearchMode colorSearchMode = SearchMode.REQUIRE_PREFIX;
	public SearchMode resourceIdSearchMode = SearchMode.REQUIRE_PREFIX;
	public boolean searchAdvancedTooltips = false;
}
