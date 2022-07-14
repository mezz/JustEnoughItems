package mezz.jei.common.config;

import mezz.jei.common.config.file.ConfigSchemaBuilder;
import mezz.jei.common.config.file.IConfigSchema;
import mezz.jei.common.gui.overlay.options.HorizontalAlignment;
import mezz.jei.core.config.IClientConfig;
import mezz.jei.core.util.PathUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;

public class JEIClientConfigs {
	private static final Logger LOGGER = LogManager.getLogger();

	private final IClientConfig clientConfig;
	private final IIngredientFilterConfig filterConfig;
	private final IModIdFormatConfig modIdFormat;
	private final IIngredientGridConfig ingredientListConfig;
	private final IIngredientGridConfig bookmarkListConfig;

	private final IConfigSchema schema;

	public JEIClientConfigs(Path configFile) {
		ConfigSchemaBuilder builder = new ConfigSchemaBuilder();

		clientConfig = new ClientConfig(builder);
		filterConfig = new IngredientFilterConfig(builder);
		modIdFormat = new ModIdFormatConfig(builder);
		ingredientListConfig = new IngredientGridConfig("IngredientList", builder, HorizontalAlignment.RIGHT);
		bookmarkListConfig = new IngredientGridConfig("BookmarkList", builder, HorizontalAlignment.LEFT);

		schema = builder.build(configFile);
	}

	public void register(Path configDir, Path configFile) {
		Path oldConfigFile = configDir.resolve("jei-client.toml");
		try {
			if (PathUtil.migrateConfigLocation(configFile, oldConfigFile)) {
				LOGGER.info("Successfully migrated config file from '{}' to new location '{}'", oldConfigFile, configFile);
			}
		} catch (IOException e) {
			LOGGER.error("Failed to migrate config file from '{}' to new location '{}'", oldConfigFile, configFile, e);
		}
		schema.register(configFile);
	}

	public IClientConfig getClientConfig() {
		return clientConfig;
	}

	public IIngredientFilterConfig getFilterConfig() {
		return filterConfig;
	}

	public IIngredientGridConfig getIngredientListConfig() {
		return ingredientListConfig;
	}

	public IIngredientGridConfig getBookmarkListConfig() {
		return bookmarkListConfig;
	}

	public IModIdFormatConfig getModIdFormat() {
		return modIdFormat;
	}
}
