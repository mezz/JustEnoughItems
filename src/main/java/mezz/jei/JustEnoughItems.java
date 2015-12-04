package mezz.jei;

import javax.annotation.Nonnull;
import java.util.Set;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLModIdMappingEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import mezz.jei.api.JEIManager;
import mezz.jei.api.JEIPlugin;
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

	private Set<ASMDataTable.ASMData> modPlugins;

	@Mod.EventHandler
	public void preInit(@Nonnull FMLPreInitializationEvent event) {
		modPlugins = event.getAsmData().getAll(JEIPlugin.class.getCanonicalName());
		packetHandler = new PacketHandler();
		JEIManager.guiHelper = new GuiHelper();
		JEIManager.itemBlacklist = new ItemBlacklist();
		common.preInit(event);
	}

	@Mod.EventHandler
	public void init(@Nonnull FMLInitializationEvent event) {
		common.init(event);
	}

	@Mod.EventHandler
	public void startJEI(@Nonnull FMLModIdMappingEvent event) {
		common.startJEI(modPlugins);
	}
}
