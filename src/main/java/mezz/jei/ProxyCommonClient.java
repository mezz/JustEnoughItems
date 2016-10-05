package mezz.jei;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

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
import mezz.jei.util.StackHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.crash.CrashReport;
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
	private GuiEventHandler guiEventHandler;
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

	@SubscribeEvent
	public void onEntityJoinedWorld(EntityJoinWorldEvent event) {
		if (event.getWorld().isRemote && !SessionData.isJeiStarted() && Minecraft.getMinecraft().thePlayer != null) {
			try {
				startJEI();
			} catch (Throwable e) {
				Minecraft.getMinecraft().displayCrashReport(new CrashReport("JEI failed to start:", e));
			}
		}
	}

	private void startJEI() {
		long jeiStartTime = System.currentTimeMillis();
		Log.info("Beginning startup...");
		SessionData.setJeiStarted();

		Config.startJei();

		SubtypeRegistry subtypeRegistry = new SubtypeRegistry();

		Iterator<IModPlugin> iterator = plugins.iterator();
		while (iterator.hasNext()) {
			IModPlugin plugin = iterator.next();
			try {
				plugin.registerItemSubtypes(subtypeRegistry);
			} catch (RuntimeException e) {
				Log.error("Failed to register item subtypes for mod plugin: {}", plugin.getClass(), e);
				iterator.remove();
			} catch (LinkageError ignored) {
				// legacy mod plugins do not have registerItemSubtypes
			}
		}

		StackHelper stackHelper = new StackHelper(subtypeRegistry);
		stackHelper.enableUidCache();
		Internal.setStackHelper(stackHelper);

		ModIngredientRegistration modIngredientRegistry = new ModIngredientRegistration();

		iterator = plugins.iterator();
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

		JeiHelpers jeiHelpers = new JeiHelpers(ingredientRegistry, stackHelper, subtypeRegistry);
		Internal.setHelpers(jeiHelpers);

		ModIdUtil modIdUtil = Internal.getModIdUtil();
		ItemRegistry itemRegistry = ItemRegistryFactory.createItemRegistry(ingredientRegistry, modIdUtil);

		ModRegistry modRegistry = new ModRegistry(jeiHelpers, itemRegistry, ingredientRegistry);

		iterator = plugins.iterator();
		while (iterator.hasNext()) {
			IModPlugin plugin = iterator.next();
			try {
				long start_time = System.currentTimeMillis();
				Log.info("Registering plugin: {} ...", plugin.getClass().getName());
				plugin.register(modRegistry);
				long timeElapsedMs = System.currentTimeMillis() - start_time;
				Log.info("Registered  plugin: {} in {} ms", plugin.getClass().getName(), timeElapsedMs);
			} catch (RuntimeException e) {
				Log.error("Failed to register mod plugin: {}", plugin.getClass(), e);
				iterator.remove();
			} catch (LinkageError e) {
				Log.error("Failed to register mod plugin: {}", plugin.getClass(), e);
				iterator.remove();
			}
		}

		long start_time = System.currentTimeMillis();
		Log.info("Building recipe registry...");
		RecipeRegistry recipeRegistry = modRegistry.createRecipeRegistry(stackHelper, ingredientRegistry);
		Log.info("Built    recipe registry in {} ms", System.currentTimeMillis() - start_time);

		start_time = System.currentTimeMillis();
		Log.info("Building item filter...");
		ItemFilter itemFilter = new ItemFilter(ingredientRegistry, jeiHelpers);
		Log.info("Built    item filter in {} ms", System.currentTimeMillis() - start_time);

		start_time = System.currentTimeMillis();
		Log.info("Building runtime...");
		List<IAdvancedGuiHandler<?>> advancedGuiHandlers = modRegistry.getAdvancedGuiHandlers();
		ItemListOverlay itemListOverlay = new ItemListOverlay(itemFilter, advancedGuiHandlers, ingredientRegistry);
		RecipesGui recipesGui = new RecipesGui(recipeRegistry);

		JeiRuntime jeiRuntime = new JeiRuntime(recipeRegistry, itemListOverlay, recipesGui, ingredientRegistry);
		Internal.setRuntime(jeiRuntime);

		stackHelper.disableUidCache();
		Log.info("Built    runtime in {} ms", System.currentTimeMillis() - start_time);

		iterator = plugins.iterator();
		while (iterator.hasNext()) {
			IModPlugin plugin = iterator.next();
			try {
				start_time = System.currentTimeMillis();
				Log.info("Sending runtime to plugin: {} ...", plugin.getClass().getName());
				plugin.onRuntimeAvailable(jeiRuntime);
				long timeElapsedMs = System.currentTimeMillis() - start_time;
				Log.info("Sent    runtime to plugin: {} in {} ms", plugin.getClass().getName(), timeElapsedMs);
			} catch (RuntimeException e) {
				Log.error("Sending runtime to plugin failed: {}", plugin.getClass(), e);
				iterator.remove();
			} catch (LinkageError e) {
				Log.error("Sending runtime to plugin failed: {}", plugin.getClass(), e);
				iterator.remove();
			}
		}

		if (guiEventHandler != null) {
			MinecraftForge.EVENT_BUS.unregister(guiEventHandler);
			guiEventHandler = null;
		}
		guiEventHandler = new GuiEventHandler(jeiRuntime);
		MinecraftForge.EVENT_BUS.register(guiEventHandler);
		Log.info("Finished startup in {} ms", System.currentTimeMillis() - jeiStartTime);
	}

	@Override
	public void restartJEI() {
		// check that JEI has been started before. if not, do nothing
		if (SessionData.isJeiStarted()) {
			startJEI();
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
