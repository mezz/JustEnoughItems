package mezz.jei.forge.config;

import mezz.jei.common.gui.overlay.options.HorizontalAlignment;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class JEIClientConfigs {
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

	public void register() {
		ModLoadingContext modLoadingContext = ModLoadingContext.get();
		modLoadingContext.registerConfig(ModConfig.Type.CLIENT, config);
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
