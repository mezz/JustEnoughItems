package mezz.jei.config;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class IngredientFilterConfigTest extends ForgeConfigTestBase {
	@Test
	public void loadingSearchSettingsKeepsEachFilterIndependent() throws IOException {
		LocalizedConfiguration first = new LocalizedConfiguration("config.jei", temporaryFolder.newFile("first.cfg"), "0.4.0");
		LocalizedConfiguration second = new LocalizedConfiguration("config.jei", temporaryFolder.newFile("second.cfg"), "0.4.0");
		first.get(Config.CATEGORY_SEARCH, "tooltipSearchMode", "enabled").set("require_prefix");
		first.get(Config.CATEGORY_SEARCH, "searchAdvancedTooltips", false).set(true);
		second.get(Config.CATEGORY_SEARCH, "tooltipSearchMode", "enabled").set("disabled");
		IngredientFilterConfig firstFilter = new IngredientFilterConfig(new IngredientFilterConfigValues());
		IngredientFilterConfig secondFilter = new IngredientFilterConfig(new IngredientFilterConfigValues());

		firstFilter.syncConfig(first);
		secondFilter.syncConfig(second);

		Assert.assertEquals(SearchMode.REQUIRE_PREFIX, firstFilter.getTooltipSearchMode());
		Assert.assertTrue(firstFilter.getSearchAdvancedTooltips());
		Assert.assertEquals(SearchMode.DISABLED, secondFilter.getTooltipSearchMode());
		Assert.assertFalse(secondFilter.getSearchAdvancedTooltips());
	}

	@Test
	public void reloadPreservesTheLegacyTooltipMigrationAndPre03Defaults() throws IOException {
		File file = temporaryFolder.newFile("jei.cfg");
		LocalizedConfiguration old = new LocalizedConfiguration("config.jei", file, "0.2.0");
		old.get("searchAdvancedTooltips", Config.CATEGORY_SEARCH, false).set(true);
		old.get(Config.CATEGORY_SEARCH, "oreDictSearchMode", "disabled").set("enabled");
		old.get(Config.CATEGORY_SEARCH, "creativeTabSearchMode", "disabled").set("enabled");
		old.save();
		LocalizedConfiguration current = new LocalizedConfiguration("config.jei", file, "0.4.0");
		IngredientFilterConfig filter = new IngredientFilterConfig(new IngredientFilterConfigValues());

		filter.syncConfig(current);
		current.save();
		LocalizedConfiguration reloaded = new LocalizedConfiguration("config.jei", file, "0.4.0");

		Assert.assertTrue(filter.getSearchAdvancedTooltips());
		Assert.assertEquals(SearchMode.DISABLED, filter.getOreDictSearchMode());
		Assert.assertEquals(SearchMode.DISABLED, filter.getCreativeTabSearchMode());
		Assert.assertFalse(reloaded.hasCategory("searchAdvancedTooltips"));
		Assert.assertTrue(reloaded.get(Config.CATEGORY_SEARCH, "searchAdvancedTooltips", false).getBoolean());
	}
}
