package mezz.jei;

import java.util.Map;

import mezz.jei.config.Config;
import mezz.jei.config.Constants;
import mezz.jei.config.SessionData;
import mezz.jei.debug.DebugItem;
import mezz.jei.network.PacketHandler;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = Constants.MOD_ID,
		name = Constants.NAME,
		version = Constants.VERSION,
		guiFactory = "mezz.jei.config.JEIModGuiFactory",
		acceptedMinecraftVersions = "[1.10]",
		dependencies = "required-after:Forge@[12.18.1.2053,);")
public class JustEnoughItems {

	@SidedProxy(clientSide = "mezz.jei.ProxyCommonClient", serverSide = "mezz.jei.ProxyCommon")
	private static ProxyCommon proxy;
	private static PacketHandler packetHandler;

	public static PacketHandler getPacketHandler() {
		return packetHandler;
	}

	public static ProxyCommon getProxy() {
		return proxy;
	}

	@NetworkCheckHandler
	public boolean checkModLists(Map<String, String> modList, Side side) {
		if (side == Side.SERVER) {
			boolean jeiOnServer = modList.containsKey(Constants.MOD_ID);
			SessionData.onConnectedToServer(jeiOnServer);
		}

		return true;
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		packetHandler = new PacketHandler();
		proxy.preInit(event);

		if (Config.isDebugItemEnabled()) {
			String name = "jeiDebug";
			Item debugItem = new DebugItem(name);
			debugItem.setUnlocalizedName(name);
			debugItem.setRegistryName(name);
			GameRegistry.register(debugItem);
		}
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init(event);
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		proxy.postInit(event);
	}
}
