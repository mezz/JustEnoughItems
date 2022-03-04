package mezz.jei.config;

import mezz.jei.events.PermanentEventSubscriptions;
import mezz.jei.gui.overlay.HorizontalAlignment;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

public class JEIClientConfigs {
	private final ClientConfig clientConfig;
	private final IngredientFilterConfig filterConfig;
	private final ModIdFormattingConfig modNameFormat;
	private final IngredientGridConfig ingredientListConfig;
	private final IngredientGridConfig bookmarkListConfig;

	private final ForgeConfigSpec config;

	public JEIClientConfigs() {
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

		clientConfig = new ClientConfig(builder);
		filterConfig = new IngredientFilterConfig(builder);
		modNameFormat = new ModIdFormattingConfig(builder);
		ingredientListConfig = new IngredientGridConfig("IngredientList", builder, HorizontalAlignment.RIGHT);
		bookmarkListConfig = new IngredientGridConfig("BookmarkList", builder, HorizontalAlignment.LEFT);
		config = builder.build();
	}

	public void register(PermanentEventSubscriptions subscriptions) {
		subscriptions.register(ModConfigEvent.class, this::reload);

		ModLoadingContext modLoadingContext = ModLoadingContext.get();
		modLoadingContext.registerConfig(ModConfig.Type.CLIENT, config);
	}

	public void reload(ModConfigEvent event) {
		if (event.getConfig().getSpec() != config) {
			return;
		}

		clientConfig.reload();
		modNameFormat.reload();
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

	public ModIdFormattingConfig getModNameFormat() {
		return modNameFormat;
	}
}
