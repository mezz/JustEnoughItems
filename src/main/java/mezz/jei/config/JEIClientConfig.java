package mezz.jei.config;

import mezz.jei.api.constants.ModIds;
import mezz.jei.events.EventBusHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.chat.ClickEvent;

import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.Optional;

public class JEIClientConfig {
	private static final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

	public static final ClientConfig clientConfig = new ClientConfig(builder);
	public static final IngredientFilterConfig filterConfig = new IngredientFilterConfig(builder);
	public static final ModIdFormattingConfig modNameFormat = new ModIdFormattingConfig(builder);

	private static final ForgeConfigSpec config = builder.build();

	public static void register(IEventBus modEventBus) {
		EventBusHelper.addListener(JEIClientConfig.class, modEventBus, ModConfigEvent.class, JEIClientConfig::reload);

		ModLoadingContext modLoadingContext = ModLoadingContext.get();
		modLoadingContext.registerConfig(ModConfig.Type.CLIENT, config);
	}

	public static void reload(ModConfigEvent event) {
		if (event.getConfig().getSpec() != config) {
			return;
		}

		clientConfig.reload();
		filterConfig.reload();
		modNameFormat.reload();
	}

	public static void openSettings() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			return;
		}

		Optional<Screen> configScreen = ModList.get()
			.getModContainerById(ModIds.JEI_ID)
			.map(ModContainer::getModInfo)
			.flatMap(ConfigGuiHandler::getGuiFactoryFor)
			.map(f -> f.apply(mc, mc.screen));

		if (configScreen.isPresent()) {
            mc.setScreen(configScreen.get());
        } else {
            ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/configured");
            Style style = Style.EMPTY
                    .setUnderlined(true)
                    .withClickEvent(clickEvent);
			MutableComponent message = new TranslatableComponent("jei.message.configured");
            message = message.setStyle(style);
            mc.player.displayClientMessage(message, false);
        }
	}
}
