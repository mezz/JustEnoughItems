package mezz.jei.config;

import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiEditConfig;
import mezz.jei.api.constants.ModIds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class JEIClientConfig
{
	private static final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

	public static final ClientConfig clientConfig = new ClientConfig(builder);
	public static final IngredientFilterConfig filterConfig = new IngredientFilterConfig(builder);
	public static final ModIdFormattingConfig modNameFormat = new ModIdFormattingConfig(builder);

	private static final ForgeConfigSpec config = builder.build();
	private static boolean ftbGUILoaded = false;
	private static final String TRANSLATION_KEY = "config."+ModIds.JEI_ID;

	public static void register() {
		FMLJavaModLoadingContext.get().getModEventBus().register(JEIClientConfig.class);
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, config);
	}

	@SubscribeEvent
	public static void commonSetup(FMLCommonSetupEvent event) {
		ftbGUILoaded = ModList.get().isLoaded("ftbguilibrary");
	}

	@SubscribeEvent
	public static void reload(ModConfig.ModConfigEvent event)
	{
		if(event.getConfig().getSpec() != config) return;

		clientConfig.reload();
		modNameFormat.reload();
	}

	public static void openSettings() {
		Minecraft mc = Minecraft.getInstance();
		if(mc.player == null) return;

		if(ftbGUILoaded) {
			ConfigGroup group = new ConfigGroup(TRANSLATION_KEY);

			clientConfig.buildSettingsGUI(group);
			filterConfig.buildSettingsGUI(group);
			modNameFormat.buildSettingsGUI(group);

			GuiEditConfig gui = new GuiEditConfig(group);
			group.savedCallback = b -> {
				if(b) config.save();
				mc.displayGuiScreen(new InventoryScreen(mc.player));
			};
			gui.openGui();
		}
		else {
			mc.player.sendStatusMessage(new TranslationTextComponent(ModIds.JEI_ID+".message.ftbguilib")
					.setStyle(Style.EMPTY.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/ftb-gui-library"))), false);
		}
	}
}
