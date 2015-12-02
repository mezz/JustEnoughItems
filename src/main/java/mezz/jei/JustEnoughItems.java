package mezz.jei;

import javax.annotation.Nonnull;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import mezz.jei.api.JEIManager;
import mezz.jei.config.Constants;
import mezz.jei.gui.GuiHelper;
import mezz.jei.network.PacketHandler;

@Mod(modid = Constants.MOD_ID,
		name = Constants.NAME,
		version = Constants.VERSION,
		guiFactory = "mezz.jei.config.JEIModGuiFactory",
		dependencies = "required-after:Forge@[11.14.0.1269,);")
public class JustEnoughItems {

	@SidedProxy(clientSide = "mezz.jei.ProxyCommonClient", serverSide = "mezz.jei.ProxyCommon")
	public static ProxyCommon common;

	@Mod.Instance(Constants.MOD_ID)
	public static JustEnoughItems instance;

	public static PacketHandler packetHandler;

	private PluginRegistry pluginRegistry;

	@Mod.EventHandler
	public void preInit(@Nonnull FMLPreInitializationEvent event) {
		packetHandler = new PacketHandler();
		JEIManager.pluginRegistry = pluginRegistry = new PluginRegistry();
		JEIManager.guiHelper = new GuiHelper();
		common.preInit(event);
	}

	@Mod.EventHandler
	public void init(@Nonnull FMLInitializationEvent event) {
		pluginRegistry.init();
		common.init(event);
	}

	@Mod.EventHandler
	public void loadComplete(@Nonnull FMLLoadCompleteEvent event) {
		JEIManager.recipeRegistry = pluginRegistry.createRecipeRegistry();
		common.loadComplete(event);
	}
}
