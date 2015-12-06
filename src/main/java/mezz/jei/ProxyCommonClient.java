package mezz.jei;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.client.network.NetHandlerPlayClient;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JEIManager;
import mezz.jei.config.Config;
import mezz.jei.config.Constants;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.ItemListOverlay;
import mezz.jei.network.packets.PacketJEI;
import mezz.jei.util.Log;
import mezz.jei.util.ModRegistry;

public class ProxyCommonClient extends ProxyCommon {
	private ItemFilter itemFilter;
	private GuiEventHandler guiEventHandler;

	@Override
	public void preInit(@Nonnull FMLPreInitializationEvent event) {
		Config.preInit(event);
	}

	@Override
	public void init(@Nonnull FMLInitializationEvent event) {
		KeyBindings.init();
		FMLCommonHandler.instance().bus().register(this);

		guiEventHandler = new GuiEventHandler();
		MinecraftForge.EVENT_BUS.register(guiEventHandler);
		FMLCommonHandler.instance().bus().register(guiEventHandler);

		TooltipEventHandler tooltipEventHandler = new TooltipEventHandler();
		MinecraftForge.EVENT_BUS.register(tooltipEventHandler);
	}

	@Override
	public void startJEI(@Nonnull Set<ASMDataTable.ASMData> modPlugins) {
		JEIManager.itemRegistry = new ItemRegistry();
		JEIManager.recipeRegistry = createRecipeRegistry(modPlugins);

		itemFilter = new ItemFilter();
		ItemListOverlay itemListOverlay = new ItemListOverlay(itemFilter);
		guiEventHandler.setItemListOverlay(itemListOverlay);
	}

	@Override
	public void sendPacketToServer(PacketJEI packet) {
		NetHandlerPlayClient netHandler = FMLClientHandler.instance().getClient().getNetHandler();
		if (netHandler != null) {
			netHandler.addToSendQueue(packet.getPacket());
		}
	}

	@SubscribeEvent
	public void onConfigChanged(@Nonnull ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if (eventArgs.modID.equals(Constants.MOD_ID)) {
			Config.syncConfig();
			itemFilter.reset();
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
					Log.info("Loaded plugin: {}", asmData.getClassName());
				}
			} catch (Throwable e) {
				FMLLog.bigWarning("Failed to load mod plugin: {}", asmData.getClassName());
				Log.error("Exception: {}", e);
			}
		}

		ModRegistry modRegistry = new ModRegistry();

		for (IModPlugin plugin : plugins) {
			try {
				plugin.register(modRegistry);
			} catch (Throwable e) {
				FMLLog.bigWarning("Failed to register mod plugin: {}", plugin.getClass());
				Log.error("Exception: {}", e);
			}
		}

		return modRegistry.createRecipeRegistry();
	}
}
