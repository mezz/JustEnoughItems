package mezz.jei;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IPluginRegistry;
import mezz.jei.api.JEIManager;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeType;
import mezz.jei.config.Config;
import mezz.jei.config.Constants;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.GuiHelper;
import mezz.jei.gui.ItemListOverlay;
import mezz.jei.plugins.forestry.ForestryPlugin;
import mezz.jei.plugins.vanilla.VanillaPlugin;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@Mod(modid = Constants.MOD_ID,
		name = Constants.NAME,
		version = Constants.VERSION,
		guiFactory = "mezz.jei.config.JEIModGuiFactory",
		dependencies = "required-after:Forge@[10.13.0.1207,);")
public class JustEnoughItems implements IPluginRegistry {

	@Mod.Instance(Constants.MOD_ID)
	public static JustEnoughItems instance;

	@Nonnull
	private final List<IModPlugin> plugins;
	private boolean pluginsCanRegister = true;
	@Nonnull
	private final RecipeRegistry recipeRegistry;

	public JustEnoughItems() {
		plugins = new ArrayList<IModPlugin>();
		JEIManager.recipeRegistry = recipeRegistry = new RecipeRegistry();
		JEIManager.guiHelper = new GuiHelper();
		JEIManager.pluginRegistry = this;
	}

	@EventHandler
	public void preInit(@Nonnull FMLPreInitializationEvent event) {
		Config.preInit(event);

		JEIManager.pluginRegistry.registerPlugin(new VanillaPlugin());
		JEIManager.pluginRegistry.registerPlugin(new ForestryPlugin());
	}

	@Override
	public void registerPlugin(IModPlugin plugin) {
		if (!pluginsCanRegister) {
			throw new IllegalStateException("Plugins must be registered during FMLPreInitializationEvent.");
		}
		plugins.add(plugin);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		pluginsCanRegister = false;
		KeyBindings.init();

		FMLCommonHandler.instance().bus().register(instance);

		for	(IModPlugin plugin : plugins) {
			for (IRecipeType recipeType : plugin.getRecipeTypes()) {
				recipeRegistry.registerRecipeType(recipeType);
			}
		}
		for	(IModPlugin plugin : plugins) {
			for (IRecipeHandler recipeHandler : plugin.getRecipeHandlers()) {
				recipeRegistry.registerRecipeHandlers(recipeHandler);
			}
		}
	}

	@EventHandler
	public void loadComplete(FMLLoadCompleteEvent event) {
		JEIManager.itemRegistry = new ItemRegistry();

		for	(IModPlugin plugin : plugins) {
			recipeRegistry.addRecipes(plugin.getRecipes());
		}

		ItemFilter itemFilter = new ItemFilter(JEIManager.itemRegistry.getItemList());
		ItemListOverlay itemListOverlay = new ItemListOverlay(itemFilter);
		GuiEventHandler guiEventHandler = new GuiEventHandler(itemListOverlay);
		MinecraftForge.EVENT_BUS.register(guiEventHandler);
		FMLCommonHandler.instance().bus().register(guiEventHandler);
	}

	@SubscribeEvent
	public void onConfigChanged(@Nonnull ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		Config.onConfigChanged(eventArgs);
	}

}
