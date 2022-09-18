package mezz.jei.common.config;

import mezz.jei.common.config.file.ConfigSchemaBuilder;
import mezz.jei.common.config.file.IConfigSchema;
import mezz.jei.common.config.file.IConfigSchemaBuilder;
import mezz.jei.common.gui.overlay.options.HorizontalAlignment;
import mezz.jei.core.config.IClientConfig;

import java.nio.file.Path;

public class JEIClientConfigs {
	private final IClientConfig clientConfig;
	private final IIngredientFilterConfig filterConfig;
	private final IModIdFormatConfig modIdFormat;
	private final IIngredientGridConfig ingredientListConfig;
	private final IIngredientGridConfig bookmarkListConfig;

	private final IConfigSchema schema;

	public JEIClientConfigs(Path configFile) {
		IConfigSchemaBuilder builder = new ConfigSchemaBuilder(configFile);

		clientConfig = new ClientConfig(builder);
		filterConfig = new IngredientFilterConfig(builder);
		modIdFormat = new ModIdFormatConfig(builder);
		ingredientListConfig = new IngredientGridConfig("IngredientList", builder, HorizontalAlignment.RIGHT);
		bookmarkListConfig = new IngredientGridConfig("BookmarkList", builder, HorizontalAlignment.LEFT);

		schema = builder.build();
	}

	public void register(Path configFile) {
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
