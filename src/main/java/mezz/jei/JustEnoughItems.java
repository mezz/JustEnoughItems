package mezz.jei;

import java.util.Map;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;

import mezz.jei.config.Constants;
import mezz.jei.config.ServerInfo;
import mezz.jei.startup.ProxyCommon;

@Mod(modid = Constants.MOD_ID,
	name = Constants.NAME,
	version = Constants.VERSION,
	guiFactory = "mezz.jei.config.JEIModGuiFactory",
	acceptedMinecraftVersions = "[1.12.2,1.13)",
	dependencies = "required-after:forge@[14.23.5.2816,);")
public class JustEnoughItems {

	@SuppressWarnings("NullableProblems")
	@SidedProxy(clientSide = "mezz.jei.startup.ProxyCommonClient", serverSide = "mezz.jei.startup.ProxyCommon")
	private static ProxyCommon proxy;

	public static ProxyCommon getProxy() {
		return proxy;
	}

	@NetworkCheckHandler
	public boolean checkModLists(Map<String, String> modList, Side side) {
		if (side == Side.SERVER) {
			boolean jeiOnServer = modList.containsKey(Constants.MOD_ID);
			ServerInfo.onConnectedToServer(jeiOnServer);
		}

		return true;
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		proxy.preInit(event);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init(event);
	}

	@Mod.EventHandler
	public void loadComplete(FMLLoadCompleteEvent event) {
		proxy.loadComplete(event);
	}
}
