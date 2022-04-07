package mezz.jei.forge.config;

import mezz.jei.common.gui.overlay.options.HorizontalAlignment;
import mezz.jei.core.util.PathUtil;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;

public class JEIClientConfigs {
	private static final Logger LOGGER = LogManager.getLogger();

	private final ClientConfig clientConfig;
	private final IngredientFilterConfig filterConfig;
	private final ModIdFormatConfig modNameFormat;
	private final IngredientGridConfig ingredientListConfig;
	private final IngredientGridConfig bookmarkListConfig;

	private final ForgeConfigSpec config;

	public JEIClientConfigs() {
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

		clientConfig = new ClientConfig(builder);
		filterConfig = new IngredientFilterConfig(builder);
		modNameFormat = new ModIdFormatConfig(builder);
		ingredientListConfig = new IngredientGridConfig("IngredientList", builder, HorizontalAlignment.RIGHT);
		bookmarkListConfig = new IngredientGridConfig("BookmarkList", builder, HorizontalAlignment.LEFT);
		config = builder.build();
	}

	public void register(Path configDir, Path configFile) {
		Path oldConfigFile = configDir.resolve(configFile.getFileName());
		try {
			if (PathUtil.migrateConfigLocation(configFile, oldConfigFile)) {
				LOGGER.info("Successfully migrated config file from '{}' to new location '{}'", oldConfigFile, configFile);
			}
		} catch (IOException e) {
			LOGGER.error("Failed to migrate config file from '{}' to new location '{}'", oldConfigFile, configFile, e);
		}
		ModLoadingContext modLoadingContext = ModLoadingContext.get();
		String relativePath = configDir.relativize(configFile).toString();
		modLoadingContext.registerConfig(ModConfig.Type.CLIENT, config, relativePath);
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

	public ModIdFormatConfig getModNameFormat() {
		return modNameFormat;
	}
}
