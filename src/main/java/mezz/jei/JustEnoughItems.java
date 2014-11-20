package mezz.jei;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import mezz.jei.api.JEIManager;
import mezz.jei.config.Config;
import mezz.jei.config.Constants;
import mezz.jei.gui.GuiHelper;
import mezz.jei.recipes.RecipeRegistry;
import mezz.jei.recipes.crafting.ShapedOreRecipeHelper;
import mezz.jei.recipes.crafting.ShapedRecipesHelper;
import mezz.jei.recipes.crafting.ShapelessOreRecipeHelper;
import mezz.jei.recipes.crafting.ShapelessRecipesHelper;
import mezz.jei.recipes.furnace.fuel.FuelRecipe;
import mezz.jei.recipes.furnace.fuel.FuelRecipeHelper;
import mezz.jei.recipes.furnace.fuel.FuelRecipeMaker;
import mezz.jei.recipes.furnace.smelting.SmeltingRecipe;
import mezz.jei.recipes.furnace.smelting.SmeltingRecipeHelper;
import mezz.jei.recipes.furnace.smelting.SmeltingRecipeMaker;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.common.MinecraftForge;

import java.util.List;

@Mod(modid = Constants.MODID,
		name = Constants.NAME,
		version = Constants.VERSION,
		guiFactory = "mezz.jei.config.JEIModGuiFactory",
		dependencies = "required-after:Forge@[10.13.0.1207,);")
public class JustEnoughItems {
	@Mod.Instance(Constants.MODID)
	public static JustEnoughItems instance;

	public static ItemRegistry itemRegistry;

	public static ItemFilter itemFilter;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Config.preInit(event);

		JEIManager.guiHelper = new GuiHelper();

		JEIManager.recipeRegistry = new RecipeRegistry();

		JEIManager.recipeRegistry.registerRecipeHelpers(
				new ShapedRecipesHelper(),
				new ShapedOreRecipeHelper(),
				new ShapelessRecipesHelper(),
				new ShapelessOreRecipeHelper(),
				new SmeltingRecipeHelper(),
				new FuelRecipeHelper()
		);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		KeyBindings.init();

		GuiEventHandler guiEventHandler = new GuiEventHandler();
		MinecraftForge.EVENT_BUS.register(guiEventHandler);
		FMLCommonHandler.instance().bus().register(guiEventHandler);

		FMLCommonHandler.instance().bus().register(instance);
	}

	@EventHandler
	public void loadComplete(FMLLoadCompleteEvent event) {

		itemRegistry = new ItemRegistry();
		itemFilter = new ItemFilter(itemRegistry);

		registerRecipes();
	}

	@SuppressWarnings("unchecked")
	private void registerRecipes() {
		JEIManager.recipeRegistry.addRecipes(CraftingManager.getInstance().getRecipeList());

		List<SmeltingRecipe> smeltingRecipeList = SmeltingRecipeMaker.getFurnaceRecipes(FurnaceRecipes.smelting());
		JEIManager.recipeRegistry.addRecipes(smeltingRecipeList);

		List<FuelRecipe> fuelRecipeList = FuelRecipeMaker.getFuelRecipes(itemRegistry.fuels);
		JEIManager.recipeRegistry.addRecipes(fuelRecipeList);
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		Config.onConfigChanged(eventArgs);
	}
}
