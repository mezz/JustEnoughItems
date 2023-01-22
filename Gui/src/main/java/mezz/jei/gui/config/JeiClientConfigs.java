package mezz.jei.gui.config;

import mezz.jei.common.config.file.ConfigSchemaBuilder;
import mezz.jei.common.config.file.IConfigSchema;
import mezz.jei.common.config.file.IConfigSchemaBuilder;
import mezz.jei.gui.util.HorizontalAlignment;

import java.nio.file.Path;

public class JeiClientConfigs implements IJeiClientConfigs {
	private final IClientConfig clientConfig;
	private final IIngredientFilterConfig ingredientFilterConfig;
	private final IIngredientGridConfig ingredientListConfig;
	private final IIngredientGridConfig bookmarkListConfig;

	private final IConfigSchema schema;

	public JeiClientConfigs(Path configFile) {
		IConfigSchemaBuilder builder = new ConfigSchemaBuilder(configFile);

		clientConfig = new ClientConfig(builder);
		ingredientFilterConfig = new IngredientFilterConfig(builder);
		ingredientListConfig = new IngredientGridConfig("IngredientList", builder, HorizontalAlignment.RIGHT);
		bookmarkListConfig = new IngredientGridConfig("BookmarkList", builder, HorizontalAlignment.LEFT);

		schema = builder.build();
	}

	public void register() {
		schema.register();
	}

	@Override
	public IClientConfig getClientConfig() {
		return clientConfig;
	}

	@Override
	public IIngredientFilterConfig getIngredientFilterConfig() {
		return ingredientFilterConfig;
	}

	@Override
	public IIngredientGridConfig getIngredientListConfig() {
		return ingredientListConfig;
	}

	@Override
	public IIngredientGridConfig getBookmarkListConfig() {
		return bookmarkListConfig;
	}
}
