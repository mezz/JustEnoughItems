package mezz.jei;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import mezz.jei.config.Config;
import mezz.jei.config.Defaults;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = Defaults.MODID,
		name = Defaults.NAME,
		version = Defaults.VERSION,
		guiFactory = "mezz.jei.config.JEIModGuiFactory",
		dependencies = "required-after:Forge@[10.13.0.1207,);")
public class JustEnoughItems {
	@Mod.Instance(Defaults.MODID)
	public static JustEnoughItems instance;

	public static ItemRegistry itemRegistry;
	public static RecipeRegistry recipeRegistry;

	public static ItemFilter itemFilter;

	@EventHandler
	public static void preInit(FMLPreInitializationEvent event) {
		Config.preInit(event);
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		Config.onConfigChanged(eventArgs);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {

		KeyBindings.init();

		GuiEventHandler guiEventHandler = new GuiEventHandler();
		MinecraftForge.EVENT_BUS.register(guiEventHandler);
		FMLCommonHandler.instance().bus().register(guiEventHandler);

		FMLCommonHandler.instance().bus().register(instance);

		itemRegistry = new ItemRegistry();
		recipeRegistry = new RecipeRegistry();

		itemFilter = new ItemFilter(itemRegistry);
	}
}
