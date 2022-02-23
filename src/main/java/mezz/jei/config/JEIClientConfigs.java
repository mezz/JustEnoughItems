package mezz.jei.config;

import mezz.jei.events.PermanentEventSubscriptions;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

public class JEIClientConfigs {
	private final ClientConfig clientConfig;
	private final IngredientFilterConfig filterConfig;
	private final ModIdFormattingConfig modNameFormat;
	private final ForgeConfigSpec config;

	public JEIClientConfigs() {
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

		clientConfig = new ClientConfig(builder);
		filterConfig = new IngredientFilterConfig(builder);
		modNameFormat = new ModIdFormattingConfig(builder);
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
		filterConfig.reload();
		modNameFormat.reload();
	}

	public ClientConfig getClientConfig() {
		return clientConfig;
	}

	public IngredientFilterConfig getFilterConfig() {
		return filterConfig;
	}

	public ModIdFormattingConfig getModNameFormat() {
		return modNameFormat;
	}
}
