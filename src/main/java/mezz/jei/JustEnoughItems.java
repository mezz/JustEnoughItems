package mezz.jei;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

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
import mezz.jei.plugins.vanilla.VanillaPlugin;

@Mod(modid = Constants.MOD_ID,
		name = Constants.NAME,
		version = Constants.VERSION,
		guiFactory = "mezz.jei.config.JEIModGuiFactory",
		dependencies = "required-after:Forge@[11.14.0.1269,);")
public class JustEnoughItems implements IPluginRegistry {

	@Mod.Instance(Constants.MOD_ID)
	public static JustEnoughItems instance;

	@Nonnull
	private final List<IModPlugin> plugins = new ArrayList<>();
	private boolean pluginsCanRegister = true;

	public JustEnoughItems() {
		JEIManager.guiHelper = new GuiHelper();
		JEIManager.pluginRegistry = this;
	}

	@Mod.EventHandler
	public void preInit(@Nonnull FMLPreInitializationEvent event) {
		Config.preInit(event);

		JEIManager.pluginRegistry.registerPlugin(new VanillaPlugin());
	}

	@Override
	public void registerPlugin(IModPlugin plugin) {
		if (!pluginsCanRegister) {
			throw new IllegalStateException("Plugins must be registered during FMLPreInitializationEvent.");
		}

		if (plugin.isModLoaded()) {
			plugins.add(plugin);
		}
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		pluginsCanRegister = false;
		KeyBindings.init();

		FMLCommonHandler.instance().bus().register(instance);
	}

	@Mod.EventHandler
	public void loadComplete(FMLLoadCompleteEvent event) {
		JEIManager.itemRegistry = new ItemRegistry();

		ImmutableList.Builder<IRecipeCategory> recipeCategories = ImmutableList.builder();
		ImmutableList.Builder<IRecipeHandler> recipeHandlers = ImmutableList.builder();
		ImmutableList.Builder<Object> recipes = ImmutableList.builder();

		for (IModPlugin plugin : plugins) {
			recipeCategories.addAll(plugin.getRecipeCategories());
			recipeHandlers.addAll(plugin.getRecipeHandlers());
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
