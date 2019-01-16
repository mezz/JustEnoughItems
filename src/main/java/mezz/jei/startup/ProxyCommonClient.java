package mezz.jei.startup;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;

import com.google.common.base.Preconditions;
import mezz.jei.Internal;
import mezz.jei.api.IModPlugin;
import mezz.jei.config.Config;
import mezz.jei.config.Constants;
import mezz.jei.config.KeyBindings;
import mezz.jei.config.ServerInfo;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.textures.JeiTextureMap;
import mezz.jei.gui.textures.Textures;
import mezz.jei.network.PacketHandler;
import mezz.jei.network.PacketHandlerClient;
import mezz.jei.network.packets.PacketJei;
import mezz.jei.plugins.jei.JEIInternalPlugin;
import mezz.jei.plugins.vanilla.VanillaPlugin;
import mezz.jei.runtime.JeiRuntime;
import mezz.jei.util.Log;

@SuppressWarnings("unused")
public class ProxyCommonClient extends ProxyCommon {
	private List<IModPlugin> plugins = new ArrayList<>();
	private final JeiStarter starter = new JeiStarter();
	private final JeiTextureMap textureMap = new JeiTextureMap("textures");
	@Nullable
	private Textures textures;

	private static void initVersionChecker() {
		final NBTTagCompound compound = new NBTTagCompound();
		compound.setString("curseProjectName", "jei");
		compound.setString("curseFilenameParser", "jei_" + ForgeVersion.mcVersion + "-[].jar");
		FMLInterModComms.sendRuntimeMessage(Constants.MOD_ID, "VersionChecker", "addCurseCheck", compound);
	}

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		PacketHandlerClient packetHandler = new PacketHandlerClient();
		channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(PacketHandler.CHANNEL_ID);
		channel.register(packetHandler);

		Config.preInit(event);
		initVersionChecker();

		ASMDataTable asmDataTable = event.getAsmData();
		this.plugins = AnnotatedInstanceUtil.getModPlugins(asmDataTable);

		IModPlugin vanillaPlugin = getVanillaPlugin(this.plugins);
		if (vanillaPlugin != null) {
			this.plugins.remove(vanillaPlugin);
			this.plugins.add(0, vanillaPlugin);
		}

		IModPlugin jeiInternalPlugin = getJeiInternalPlugin(this.plugins);
		if (jeiInternalPlugin != null) {
			this.plugins.remove(jeiInternalPlugin);
			this.plugins.add(jeiInternalPlugin);
		}

		MinecraftForge.EVENT_BUS.register(this);
	}

	@Nullable
	private IModPlugin getVanillaPlugin(List<IModPlugin> modPlugins) {
		for (IModPlugin modPlugin : modPlugins) {
			if (modPlugin instanceof VanillaPlugin) {
				return modPlugin;
			}
		}
		return null;
	}

	@Nullable
	private IModPlugin getJeiInternalPlugin(List<IModPlugin> modPlugins) {
		for (IModPlugin modPlugin : modPlugins) {
			if (modPlugin instanceof JEIInternalPlugin) {
				return modPlugin;
			}
		}
		return null;
	}

	@Override
	public void init(FMLInitializationEvent event) {
		KeyBindings.init();

		Minecraft minecraft = Minecraft.getMinecraft();
		minecraft.renderEngine.loadTickableTexture(textureMap.getLocation(), textureMap);
	}

	@Override
	public void loadComplete(FMLLoadCompleteEvent event) {
		// Reload when resources change
		Minecraft minecraft = Minecraft.getMinecraft();
		IReloadableResourceManager reloadableResourceManager = (IReloadableResourceManager) minecraft.getResourceManager();
		reloadableResourceManager.registerReloadListener(resourceManager -> {
			// check that JEI has been started before. if not, do nothing
			if (this.starter.hasStarted()) {
				if (Config.isDebugModeEnabled()) {
					Log.get().info("Restarting JEI.", new RuntimeException("Stack trace for debugging"));
				} else {
					Log.get().info("Restarting JEI.");
				}
				Preconditions.checkNotNull(textures);
				this.starter.start(this.plugins, textures);
			}
		});

		Preconditions.checkNotNull(textures);
		this.starter.start(plugins, textures);
	}

	@SubscribeEvent
	public void onClientConnectedToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
		if (!event.isLocal() && !event.getConnectionType().equals("MODDED")) {
			ServerInfo.onConnectedToServer(false);
		}
		NetworkManager networkManager = event.getManager();
		Config.syncWorldConfig(networkManager);
		MinecraftForge.EVENT_BUS.post(new PlayerJoinedWorldEvent());
	}

	private static void reloadItemList() {
		JeiRuntime runtime = Internal.getRuntime();
		if (runtime != null) {
			IngredientListOverlay ingredientListOverlay = runtime.getIngredientListOverlay();
			ingredientListOverlay.rebuildItemFilter();
		}
	}

	@Override
	public void sendPacketToServer(PacketJei packet) {
		NetHandlerPlayClient netHandler = FMLClientHandler.instance().getClient().getConnection();
		if (netHandler != null && ServerInfo.isJeiOnServer()) {
			netHandler.sendPacket(packet.getPacket());
		}
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if (Constants.MOD_ID.equals(eventArgs.getModID())) {
			if (Config.syncAllConfig()) {
				reloadItemList();
			}
		} else {
			if (starter.hasStarted()) {
				Config.checkForModNameFormatOverride();
			}
		}
	}

	@SubscribeEvent
	public void onWorldSave(WorldEvent.Save event) {
		try {
			Config.saveFilterText();
		} catch (RuntimeException e) {
			Log.get().error("Failed to save filter text.", e);
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void handleTextureRemap(TextureStitchEvent.Pre event) {
		textures = new Textures(textureMap);
	}
}
