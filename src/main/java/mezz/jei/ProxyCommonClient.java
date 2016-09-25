package mezz.jei;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.config.Config;
import mezz.jei.config.Constants;
import mezz.jei.config.KeyBindings;
import mezz.jei.config.SessionData;
import mezz.jei.gui.ItemListOverlay;
import mezz.jei.gui.RecipesGui;
import mezz.jei.network.packets.PacketJEI;
import mezz.jei.plugins.jei.JEIInternalPlugin;
import mezz.jei.plugins.vanilla.VanillaPlugin;
import mezz.jei.util.AnnotatedInstanceUtil;
import mezz.jei.util.Log;
import mezz.jei.util.ModIdUtil;
import mezz.jei.util.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.crash.CrashReport;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@SuppressWarnings("unused")
public class ProxyCommonClient extends ProxyCommon {
	@Nullable
	private ItemFilter itemFilter;
	private List<IModPlugin> plugins;

	private static void initVersionChecker() {
		final NBTTagCompound compound = new NBTTagCompound();
		compound.setString("curseProjectName", "just-enough-items-jei");
		compound.setString("curseFilenameParser", "jei_" + ForgeVersion.mcVersion + "-[].jar");
		FMLInterModComms.sendRuntimeMessage(Constants.MOD_ID, "VersionChecker", "addCurseCheck", compound);
	}
	
	@Override
	public void preInit(FMLPreInitializationEvent event) {
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
		MinecraftForge.EVENT_BUS.register(this);

		GuiEventHandler guiEventHandler = new GuiEventHandler();
		MinecraftForge.EVENT_BUS.register(guiEventHandler);

		fixVanillaItemHasSubtypes();
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {
		// Reload when resources change
		Minecraft minecraft = Minecraft.getMinecraft();
		IReloadableResourceManager reloadableResourceManager = (IReloadableResourceManager) minecraft.getResourceManager();
		reloadableResourceManager.registerReloadListener(new IResourceManagerReloadListener() {
			@Override
			public void onResourceManagerReload(IResourceManager resourceManager) {
				restartJEI();
			}
		});
	}

	/** fix vanilla items that don't mark themselves as having subtypes */
	private static void fixVanillaItemHasSubtypes() {
		List<Item> items = Arrays.asList(
				Items.POTIONITEM,
				Items.LINGERING_POTION,
				Items.SPLASH_POTION,
				Items.TIPPED_ARROW,
				Items.ENCHANTED_BOOK
		);
		for (Item item : items) {
			item.setHasSubtypes(true);
		}
	}

	@SubscribeEvent
	public void onEntityJoinedWorld(EntityJoinWorldEvent event) {
		if (!SessionData.isJeiStarted() && Minecraft.getMinecraft().thePlayer != null) {
			try {
				startJEI();
			} catch (Throwable e) {
				Minecraft.getMinecraft().displayCrashReport(new CrashReport("JEI failed to start:", e));
			}
		}
	}

	private void startJEI() {
		SessionData.setJeiStarted();

		Config.startJei();

		Internal.setHelpers(new JeiHelpers());
		Internal.getStackHelper().enableUidCache();

		ModIngredientRegistration modIngredientRegistry = new ModIngredientRegistration();

		Iterator<IModPlugin> iterator = plugins.iterator();
		while (iterator.hasNext()) {
			IModPlugin plugin = iterator.next();
			try {
				plugin.registerIngredients(modIngredientRegistry);
			} catch (RuntimeException e) {
				Log.error("Failed to register Ingredients for mod plugin: {}", plugin.getClass(), e);
				iterator.remove();
			} catch (LinkageError ignored) {
				// legacy mod plugins do not have registerIngredients
			}
		}

		IngredientRegistry ingredientRegistry = modIngredientRegistry.createIngredientRegistry();
		Internal.setIngredientRegistry(ingredientRegistry);

		ModIdUtil modIdUtil = Internal.getHelpers().getModIdUtil();
		ItemRegistry itemRegistry = ItemRegistryFactory.createItemRegistry(ingredientRegistry, modIdUtil);

		ModRegistry modRegistry = new ModRegistry(Internal.getHelpers(), itemRegistry, ingredientRegistry);

		iterator = plugins.iterator();
		while (iterator.hasNext()) {
			IModPlugin plugin = iterator.next();
			try {
				long start_time = System.nanoTime();
				Log.info("Registering plugin: {}", plugin.getClass().getName());
				plugin.register(modRegistry);
				long timeElapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start_time);
				Log.info("Registered  plugin: {} in {} milliseconds", plugin.getClass().getName(), timeElapsedMs);
			} catch (RuntimeException e) {
				Log.error("Failed to register mod plugin: {}", plugin.getClass(), e);
				iterator.remove();
			} catch (LinkageError e) {
				Log.error("Failed to register mod plugin: {}", plugin.getClass(), e);
				iterator.remove();
			}
		}

		RecipeRegistry recipeRegistry = modRegistry.createRecipeRegistry(ingredientRegistry);

		List<IAdvancedGuiHandler<?>> advancedGuiHandlers = modRegistry.getAdvancedGuiHandlers();

		itemFilter = new ItemFilter(ingredientRegistry);
		ItemListOverlay itemListOverlay = new ItemListOverlay(itemFilter, advancedGuiHandlers, ingredientRegistry);
		RecipesGui recipesGui = new RecipesGui(recipeRegistry);

		JeiRuntime jeiRuntime = new JeiRuntime(recipeRegistry, itemListOverlay, recipesGui, ingredientRegistry);
		Internal.setRuntime(jeiRuntime);

		iterator = plugins.iterator();
		while (iterator.hasNext()) {
			IModPlugin plugin = iterator.next();
			try {
				plugin.onRuntimeAvailable(jeiRuntime);
			} catch (RuntimeException e) {
				Log.error("Mod plugin failed: {}", plugin.getClass(), e);
				iterator.remove();
			} catch (LinkageError e) {
				Log.error("Mod plugin failed: {}", plugin.getClass(), e);
				iterator.remove();
			}
		}

		Internal.getStackHelper().disableUidCache();
	}

	@Override
	public void restartJEI() {
		// check that JEI has been started before. if not, do nothing
		if (SessionData.isJeiStarted()) {
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
		NetHandlerPlayClient netHandler = FMLClientHandler.instance().getClient().getConnection();
		if (netHandler != null) {
			netHandler.sendPacket(packet.getPacket());
		}
	}

	// subscribe to event with low priority so that addon mods that use the config can do their stuff first
	@SubscribeEvent(priority = EventPriority.LOW)
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if (!Constants.MOD_ID.equals(eventArgs.getModID())) {
			return;
		}

		if (SessionData.isJeiStarted() && Config.syncAllConfig()) {
			restartJEI(); // reload everything, configs can change available recipes
		}
	}
}
