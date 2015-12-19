package mezz.jei;

import javax.annotation.Nonnull;
import java.util.Map;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLModIdMappingEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;

import mezz.jei.config.Config;
import mezz.jei.config.Constants;
import mezz.jei.network.PacketHandler;

@Mod(modid = Constants.MOD_ID,
		name = Constants.NAME,
		version = Constants.VERSION,
		guiFactory = "mezz.jei.config.JEIModGuiFactory",
		dependencies = "required-after:Forge@[11.14.0.1269,);")
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
		if (side == Side.SERVER && !modList.containsKey(Constants.MOD_ID)) {
			Config.disableRecipeTransfer();
		}

		return true;
	}

	@Mod.EventHandler
	public void preInit(@Nonnull FMLPreInitializationEvent event) {
		packetHandler = new PacketHandler();
		Internal.setHelpers(new JeiHelpers());
		proxy.preInit(event);
	}

	@Mod.EventHandler
	public void init(@Nonnull FMLInitializationEvent event) {
		proxy.init(event);
	}

	@Mod.EventHandler
	public void startJEI(@Nonnull FMLModIdMappingEvent event) {
		proxy.startJEI();
	}
}
