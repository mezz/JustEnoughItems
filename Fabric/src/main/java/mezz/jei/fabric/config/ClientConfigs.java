package mezz.jei.fabric.config;

import mezz.jei.api.config.IClientConfig;
import mezz.jei.api.config.IIngredientFilterConfig;
import mezz.jei.api.config.IIngredientGridConfig;
import mezz.jei.api.config.IClientConfigs;
import mezz.jei.core.config.file.FileWatcher;
import mezz.jei.api.config.gui.HorizontalAlignment;
import mezz.jei.core.config.file.ConfigSchemaBuilder;
import mezz.jei.core.config.file.IConfigSchema;
import mezz.jei.core.config.file.IConfigSchemaBuilder;

import java.nio.file.Path;

public class ClientConfigs implements IClientConfigs {
	private final IClientConfig clientConfig;
	private final IIngredientFilterConfig ingredientFilterConfig;
	private final IIngredientGridConfig ingredientListConfig;
	private final IIngredientGridConfig bookmarkListConfig;

	private final IConfigSchema schema;

	public ClientConfigs(Path configFile) {
		IConfigSchemaBuilder builder = new ConfigSchemaBuilder(configFile);

		clientConfig = new ClientConfig(builder);
		ingredientFilterConfig = new IngredientFilterConfig(builder);
		ingredientListConfig = new IngredientGridConfig("IngredientList", builder, HorizontalAlignment.RIGHT);
		bookmarkListConfig = new IngredientGridConfig("BookmarkList", builder, HorizontalAlignment.LEFT);

		schema = builder.build();
	}

	public void register(FileWatcher fileWatcher) {
		schema.register(fileWatcher);
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
