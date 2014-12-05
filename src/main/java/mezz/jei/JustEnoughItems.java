package mezz.jei;

import com.google.common.collect.ImmutableList;
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
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
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
	private boolean pluginsCanRegister;

	public JustEnoughItems() {
		plugins = new ArrayList<IModPlugin>();
		JEIManager.guiHelper = new GuiHelper();
		JEIManager.pluginRegistry = this;
		this.pluginsCanRegister = true;
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
	}

	@EventHandler
	public void loadComplete(FMLLoadCompleteEvent event) {
		JEIManager.itemRegistry = new ItemRegistry();

		ImmutableList.Builder<IRecipeCategory> recipeCategories = ImmutableList.builder();
		ImmutableList.Builder<IRecipeHandler> recipeHandlers = ImmutableList.builder();
		ImmutableList.Builder<Object> recipes = ImmutableList.builder();

		for	(IModPlugin plugin : plugins) {
			recipeCategories.addAll(plugin.getRecipeCategories());
			recipeHandlers.addAll( plugin.getRecipeHandlers());
			recipes.addAll(plugin.getRecipes());
		}

		JEIManager.recipeRegistry = new RecipeRegistry(recipeCategories.build(), recipeHandlers.build(), recipes.build());

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
