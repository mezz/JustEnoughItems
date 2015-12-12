package mezz.jei;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLModIdMappingEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;

import mezz.jei.api.JEIManager;
import mezz.jei.api.JEIPlugin;
import mezz.jei.config.Config;
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

	@NetworkCheckHandler
	public boolean checkModLists(Map<String, String> modList, Side side) {
		Config.recipeTransferEnabled = modList.containsKey(Constants.MOD_ID);

		return true;
	}

	@Mod.EventHandler
	public void preInit(@Nonnull FMLPreInitializationEvent event) {
		packetHandler = new PacketHandler();
		JEIManager.guiHelper = new GuiHelper();
		JEIManager.itemBlacklist = new ItemBlacklist();
		JEIManager.nbtIgnoreList = new NbtIgnoreList();
		common.preInit(event);
	}

	@Mod.EventHandler
	public void init(@Nonnull FMLInitializationEvent event) {
		common.init(event);
	}

	@Mod.EventHandler
	public void startJEI(@Nonnull FMLModIdMappingEvent event) {
		common.startJEI();
	}
}
