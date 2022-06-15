package mezz.jei.common.config;

import mezz.jei.common.config.file.ConfigSchema;
import mezz.jei.common.config.file.ConfigSchemaBuilder;
import mezz.jei.common.config.file.ConfigSerializer;
import mezz.jei.common.config.file.FileWatcher;
import mezz.jei.common.gui.overlay.options.HorizontalAlignment;
import mezz.jei.core.util.PathUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class JEIClientConfigs {
	private static final Logger LOGGER = LogManager.getLogger();

	private final ClientConfig clientConfig;
	private final IngredientFilterConfig filterConfig;
	private final ModIdFormatConfig modIdFormat;
	private final IngredientGridConfig ingredientListConfig;
	private final IngredientGridConfig bookmarkListConfig;

	private final ConfigSchema schema;

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

		Path configPath = schema.getPath();
		if (!Files.exists(configPath)) {
			try {
				Files.createDirectories(configPath.getParent());
				ConfigSerializer.save(schema);
			} catch (IOException e) {
				LOGGER.error("Failed to create config file: '{}'", configFile, e);
			}
		}

		try {
			FileWatcher fileWatcher = new FileWatcher(Map.of(configPath, schema::onFileChanged));
			Thread thread = new Thread(fileWatcher::run, "JEI Config file watcher");
			thread.start();
		} catch (IOException e) {
			LOGGER.error("Failed to create FileWatcher Thread for config file: '{}'", configFile, e);
		}
	}

	public ClientConfig getClientConfig() {
		return clientConfig;
	}

	public IngredientFilterConfig getFilterConfig() {
		return filterConfig;
	}

	public IngredientGridConfig getIngredientListConfig() {
		return ingredientListConfig;
	}

	public IngredientGridConfig getBookmarkListConfig() {
		return bookmarkListConfig;
	}

	public ModIdFormatConfig getModIdFormat() {
		return modIdFormat;
	}
}
