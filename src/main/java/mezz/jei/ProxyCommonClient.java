package mezz.jei;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JEIManager;
import mezz.jei.api.JEIPlugin;
import mezz.jei.config.Config;
import mezz.jei.config.Constants;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.ItemListOverlay;
import mezz.jei.network.packets.PacketJEI;
import mezz.jei.util.Log;
import mezz.jei.util.ModRegistry;

public class ProxyCommonClient extends ProxyCommon {
	@Nullable
	private ItemFilter itemFilter;
	private GuiEventHandler guiEventHandler;
	private Set<ASMDataTable.ASMData> modPlugins;

	private void initVersionChecker() {
		final NBTTagCompound compound = new NBTTagCompound();
		compound.setString("curseProjectName", "just-enough-items-jei");
		compound.setString("curseFilenameParser", "jei_" + ForgeVersion.mcVersion + "-[].jar");
		FMLInterModComms.sendRuntimeMessage(Constants.MOD_ID, "VersionChecker", "addCurseCheck", compound);
	}
	
	@Override
	public void preInit(@Nonnull FMLPreInitializationEvent event) {
		Config.preInit(event);
		initVersionChecker();
		modPlugins = event.getAsmData().getAll(JEIPlugin.class.getCanonicalName());
	}

	@Override
	public void init(@Nonnull FMLInitializationEvent event) {
		KeyBindings.init();
		FMLCommonHandler.instance().bus().register(this);

		guiEventHandler = new GuiEventHandler();
		MinecraftForge.EVENT_BUS.register(guiEventHandler);
		FMLCommonHandler.instance().bus().register(guiEventHandler);
	}

	@Override
	public void startJEI() {
		JEIManager.itemRegistry = new ItemRegistry();
		JEIManager.recipeRegistry = createRecipeRegistry(modPlugins);

		itemFilter = new ItemFilter();
		ItemListOverlay itemListOverlay = new ItemListOverlay(itemFilter);
		guiEventHandler.setItemListOverlay(itemListOverlay);
	}

	private void restartJEI() {
		// check that JEI has been started before, if not do nothing
		if (JEIManager.itemRegistry != null) {
			startJEI();
		}
	}

	@Override
	public void resetItemFilter() {
		if (itemFilter != null) {
			itemFilter.reset();
		}
	}

	@Override
	public void sendPacketToServer(PacketJEI packet) {
		NetHandlerPlayClient netHandler = FMLClientHandler.instance().getClient().getNetHandler();
		if (netHandler != null) {
			netHandler.addToSendQueue(packet.getPacket());
		}
	}

	// subscribe to Post event so that addon mods that use the config can do their stuff first
	@SubscribeEvent
	public void onConfigChanged(@Nonnull ConfigChangedEvent.PostConfigChangedEvent eventArgs) {
		if (Constants.MOD_ID.equals(eventArgs.modID)) {
			if (Config.syncConfig()) {
				restartJEI(); // reload everything, configs can change available recipes
			}
		}
	}

	private static RecipeRegistry createRecipeRegistry(@Nonnull Set<ASMDataTable.ASMData> modPluginsData) {
		List<IModPlugin> plugins = new ArrayList<>();
		for (ASMDataTable.ASMData asmData : modPluginsData) {
			try {
				Class<?> asmClass = Class.forName(asmData.getClassName());
				Class<? extends IModPlugin> modPluginClass = asmClass.asSubclass(IModPlugin.class);
				IModPlugin plugin = modPluginClass.newInstance();
				if (plugin.isModLoaded()) {
					plugins.add(plugin);
				}
			} catch (Throwable e) {
				Log.error("Failed to load mod plugin: {}", asmData.getClassName(), e);
			}
		}

		ModRegistry modRegistry = new ModRegistry();

		for (IModPlugin plugin : plugins) {
			try {
				plugin.register(modRegistry);
				Log.info("Registered plugin: {}", plugin.getClass().getName());
			} catch (Throwable e) {
				Log.error("Failed to register mod plugin: {}", plugin.getClass(), e);
			}
		}

		return modRegistry.createRecipeRegistry();
	}
}
