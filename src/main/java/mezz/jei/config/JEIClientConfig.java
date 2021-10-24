package mezz.jei.config;

import mezz.jei.events.EventBusHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class JEIClientConfig {
	private static final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

	public static final ClientConfig clientConfig = new ClientConfig(builder);
	public static final IngredientFilterConfig filterConfig = new IngredientFilterConfig(builder);
	public static final ModIdFormattingConfig modNameFormat = new ModIdFormattingConfig(builder);

	private static final ForgeConfigSpec config = builder.build();

	public static void register(IEventBus modEventBus) {
		EventBusHelper.addListener(modEventBus, ModConfig.ModConfigEvent.class, JEIClientConfig::reload);

		ModLoadingContext modLoadingContext = ModLoadingContext.get();
		modLoadingContext.registerConfig(ModConfig.Type.CLIENT, config);
	}

	public static void reload(ModConfig.ModConfigEvent event) {
		if (event.getConfig().getSpec() != config) {
			return;
		}

		clientConfig.reload();
		filterConfig.reload();
		modNameFormat.reload();
	}

	public static void openSettings() {
		Minecraft mc = Minecraft.getInstance();
		if (mc == null || mc.player == null) {
			return;
		}

		ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/configured");
		Style style = Style.EMPTY
				.setUnderlined(true)
				.withClickEvent(clickEvent);
		TranslationTextComponent textComponent = new TranslationTextComponent("jei.message.configured");
		ITextComponent message = textComponent.setStyle(style);
		mc.player.displayClientMessage(message, false);
	}
}
