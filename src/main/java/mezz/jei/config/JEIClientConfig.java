package mezz.jei.config;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigScreen;
import mezz.jei.api.constants.ModIds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class JEIClientConfig {
	private static final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

	public static final ClientConfig clientConfig = new ClientConfig(builder);
	public static final IngredientFilterConfig filterConfig = new IngredientFilterConfig(builder);
	public static final ModIdFormattingConfig modNameFormat = new ModIdFormattingConfig(builder);

	private static final ForgeConfigSpec config = builder.build();
	private static boolean ftbLibraryLoaded = false;
	private static final String TRANSLATION_KEY = "config." + ModIds.JEI_ID;

	public static void register() {
		FMLJavaModLoadingContext.get().getModEventBus().register(JEIClientConfig.class);
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, config);
	}

	@SubscribeEvent
	public static void commonSetup(FMLCommonSetupEvent event) {
		ftbLibraryLoaded = ModList.get().isLoaded("ftblibrary");
	}

	@SubscribeEvent
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

		if (ftbLibraryLoaded) {
			ConfigGroup group = new ConfigGroup(TRANSLATION_KEY);

			clientConfig.buildSettingsGUI(group);
			filterConfig.buildSettingsGUI(group);
			modNameFormat.buildSettingsGUI(group);

			EditConfigScreen gui = new EditConfigScreen(group);
			group.savedCallback = b -> {
				if (b) {
					config.save();
				}
				mc.setScreen(new InventoryScreen(mc.player));
			};
			gui.openGui();
		} else {
			mc.player.displayClientMessage(new TranslatableComponent(ModIds.JEI_ID + ".message.ftblibrary")
				.setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/ftb-library-forge"))), false);
		}
	}
}
