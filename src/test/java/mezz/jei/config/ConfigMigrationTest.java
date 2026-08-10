package mezz.jei.config;

import net.minecraftforge.common.config.Configuration;
import org.junit.Assert;
import org.junit.Test;

public class ConfigMigrationTest {
	private static final String LEGACY_CATEGORY = "searchAdvancedTooltips";
	private static final String LEGACY_PROPERTY = Config.CATEGORY_SEARCH;
	private static final String PROPERTY = "searchAdvancedTooltips";

	@Test
	public void testMigrateSearchAdvancedTooltipsConfig() {
		Configuration config = new Configuration();
		config.get(LEGACY_CATEGORY, LEGACY_PROPERTY, true);

		Config.migrateSearchAdvancedTooltipsConfig(config);

		Assert.assertFalse(config.hasCategory(LEGACY_CATEGORY));
		Assert.assertTrue(config.hasKey(Config.CATEGORY_SEARCH, PROPERTY));
		Assert.assertTrue(config.get(Config.CATEGORY_SEARCH, PROPERTY, false).getBoolean());
	}

	@Test
	public void testMigrateSearchAdvancedTooltipsConfigKeepsExistingProperty() {
		Configuration config = new Configuration();
		config.get(LEGACY_CATEGORY, LEGACY_PROPERTY, true);
		config.get(Config.CATEGORY_SEARCH, PROPERTY, false);

		Config.migrateSearchAdvancedTooltipsConfig(config);

		Assert.assertFalse(config.hasCategory(LEGACY_CATEGORY));
		Assert.assertFalse(config.get(Config.CATEGORY_SEARCH, PROPERTY, true).getBoolean());
	}
}
