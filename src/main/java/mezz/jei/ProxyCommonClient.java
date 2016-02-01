package mezz.jei;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.config.Config;
import mezz.jei.config.Constants;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.ItemListOverlay;
import mezz.jei.network.packets.PacketJEI;
import mezz.jei.plugins.vanilla.VanillaPlugin;
import mezz.jei.util.AnnotatedInstanceUtil;
import mezz.jei.util.Log;
import mezz.jei.util.ModRegistry;

public class ProxyCommonClient extends ProxyCommon {
	private static boolean started = false;
	@Nullable
	private ItemFilter itemFilter;
	private GuiEventHandler guiEventHandler;
	private List<IModPlugin> plugins;

	private static void initVersionChecker() {
		final NBTTagCompound compound = new NBTTagCompound();
		compound.setString("curseProjectName", "just-enough-items-jei");
		compound.setString("curseFilenameParser", "jei_" + ForgeVersion.mcVersion + "-[].jar");
		FMLInterModComms.sendRuntimeMessage(Constants.MOD_ID, "VersionChecker", "addCurseCheck", compound);
	}
	
	@Override
	public void preInit(@Nonnull FMLPreInitializationEvent event) {
		Config.preInit(event);
		initVersionChecker();

		ASMDataTable asmDataTable = event.getAsmData();
		this.plugins = AnnotatedInstanceUtil.getModPlugins(asmDataTable);

		IModPlugin vanillaPlugin = getVanillaPlugin(this.plugins);
		if (vanillaPlugin != null) {
			this.plugins.remove(vanillaPlugin);
			this.plugins.add(0, vanillaPlugin);
		}

		Iterator<IModPlugin> iterator = plugins.iterator();
		while (iterator.hasNext()) {
			IModPlugin plugin = iterator.next();
			try {
				plugin.onJeiHelpersAvailable(Internal.getHelpers());
			} catch (AbstractMethodError ignored) {
				// older plugins don't have this method
			} catch (RuntimeException e) {
				Log.error("Mod plugin failed: {}", plugin.getClass(), e);
				iterator.remove();
			}
		}

		// Reload when localization changes
		Minecraft minecraft = Minecraft.getMinecraft();
		IReloadableResourceManager reloadableResourceManager = (IReloadableResourceManager) minecraft.getResourceManager();
		reloadableResourceManager.registerReloadListener(new IResourceManagerReloadListener() {
			@Override
			public void onResourceManagerReload(IResourceManager resourceManager) {
				restartJEI();
			}
		});
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

	@Override
	public void init(@Nonnull FMLInitializationEvent event) {
		KeyBindings.init();
		MinecraftForge.EVENT_BUS.register(this);

		guiEventHandler = new GuiEventHandler();
		MinecraftForge.EVENT_BUS.register(guiEventHandler);
	}

	@SubscribeEvent
	public void onEntityJoinedWorld(EntityJoinWorldEvent event) {
		if (!started && Minecraft.getMinecraft().thePlayer != null) {
			startJEI();
		}
	}

	private void startJEI() {
		started = true;
		ItemRegistry itemRegistry = new ItemRegistry();
		Internal.setItemRegistry(itemRegistry);

		Iterator<IModPlugin> iterator = plugins.iterator();
		while (iterator.hasNext()) {
			IModPlugin plugin = iterator.next();
			try {
				plugin.onItemRegistryAvailable(itemRegistry);
			} catch (AbstractMethodError ignored) {
				// older plugins don't have this method
			} catch (RuntimeException e) {
				Log.error("Mod plugin failed: {}", plugin.getClass(), e);
				iterator.remove();
			}
		}

		ModRegistry modRegistry = new ModRegistry();

		iterator = plugins.iterator();
		while (iterator.hasNext()) {
			IModPlugin plugin = iterator.next();
			try {
				plugin.register(modRegistry);
				Log.info("Registered plugin: {}", plugin.getClass().getName());
			} catch (RuntimeException e) {
				Log.error("Failed to register mod plugin: {}", plugin.getClass(), e);
				iterator.remove();
			}
		}

		RecipeRegistry recipeRegistry = modRegistry.createRecipeRegistry();

		iterator = plugins.iterator();
		while (iterator.hasNext()) {
			IModPlugin plugin = iterator.next();
			try {
				//noinspection deprecation
				plugin.onRecipeRegistryAvailable(recipeRegistry);
			} catch (AbstractMethodError ignored) {
				// older plugins don't have this method
			} catch (RuntimeException e) {
				Log.error("Mod plugin failed: {}", plugin.getClass(), e);
				iterator.remove();
			}
		}

		List<IAdvancedGuiHandler<?>> advancedGuiHandlers = modRegistry.getAdvancedGuiHandlers();

		itemFilter = new ItemFilter(itemRegistry);
		ItemListOverlay itemListOverlay = new ItemListOverlay(itemFilter, advancedGuiHandlers);
		guiEventHandler.setItemListOverlay(itemListOverlay);

		JeiRuntime jeiRuntime = new JeiRuntime(recipeRegistry, itemListOverlay);
		Internal.setRuntime(jeiRuntime);

		iterator = plugins.iterator();
		while (iterator.hasNext()) {
			IModPlugin plugin = iterator.next();
			try {
				plugin.onRuntimeAvailable(jeiRuntime);
			} catch (AbstractMethodError ignored) {
				// older plugins don't have this method
			} catch (RuntimeException e) {
				Log.error("Mod plugin failed: {}", plugin.getClass(), e);
				iterator.remove();
			}
		}
	}

	@Override
	public void restartJEI() {
		// check that JEI has been started before. if not, do nothing
		if (started) {
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

	// subscribe to event with low priority so that addon mods that use the config can do their stuff first
	@SubscribeEvent(priority = EventPriority.LOW)
	public void onConfigChanged(@Nonnull ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if (!Constants.MOD_ID.equals(eventArgs.modID)) {
			return;
		}

		if (Config.syncConfig()) {
			restartJEI(); // reload everything, configs can change available recipes
		}
	}
}
