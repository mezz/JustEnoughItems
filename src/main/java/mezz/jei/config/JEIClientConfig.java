package mezz.jei.config;

import mezz.jei.api.constants.ModIds;
import mezz.jei.events.EventBusHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.BiFunction;

public class JEIClientConfig {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

	public static final ClientConfig clientConfig = new ClientConfig(builder);
	public static final IngredientFilterConfig filterConfig = new IngredientFilterConfig(builder);
	public static final ModIdFormattingConfig modNameFormat = new ModIdFormattingConfig(builder);

	private static final ForgeConfigSpec config = builder.build();

	public static void register(IEventBus modEventBus) {
		EventBusHelper.addListener(JEIClientConfig.class, modEventBus, ModConfig.ModConfigEvent.class, JEIClientConfig::reload);

		ModLoadingContext modLoadingContext = ModLoadingContext.get();
		modLoadingContext.registerConfig(ModConfig.Type.CLIENT, config);
	}

	public static void reload(ModConfig.ModConfigEvent event) {
		if (event.getConfig().getSpec() != config) {
			return;
		}

		clientConfig.reload();
		modNameFormat.reload();
	}

	public static void openSettings() {
		Minecraft mc = Minecraft.getInstance();
		if (mc == null || mc.player == null) {
			return;
		}

        ModContainer jeiContainer = ModList.get().getModContainerById(ModIds.JEI_ID).get();
        Optional<BiFunction<Minecraft, Screen, Screen>> configGuiFactory = jeiContainer.getCustomExtension(ExtensionPoint.CONFIGGUIFACTORY);
        if (configGuiFactory.isPresent()) {
            mc.setScreen(configGuiFactory.get().apply(mc, mc.screen));
		} else {
			ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/configured");
			Style style = Style.EMPTY
				.setUnderlined(true)
				.withColor(TextFormatting.DARK_BLUE)
				.withClickEvent(clickEvent);
			TranslationTextComponent message = new TranslationTextComponent("jei.message.configured");
			message.setStyle(style);

			Path configDirectory = FMLPaths.CONFIGDIR.get().resolve(ModIds.JEI_ID);
			try {
				Files.createDirectories(configDirectory);
				Style folderStyle = Style.EMPTY
					.setUnderlined(true)
					.withColor(TextFormatting.WHITE)
					.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, configDirectory.toAbsolutePath().toString()));
				ITextComponent folderMessage = new TranslationTextComponent("jei.message.config.folder").setStyle(folderStyle);
				message.append(new StringTextComponent("\n"));
				message.append(folderMessage);
			} catch (IOException e) {
				LOGGER.error("Unable to create JEI config directory: {}", configDirectory, e);
			}
			mc.player.displayClientMessage(message, false);
		}
	}
}
