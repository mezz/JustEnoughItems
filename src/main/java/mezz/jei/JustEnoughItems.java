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
import mezz.jei.api.recipe.type.EnumRecipeTypeKey;
import mezz.jei.config.Config;
import mezz.jei.config.Constants;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.GuiHelper;
import mezz.jei.plugins.forestry.crafting.ForestryShapedRecipeHelper;
import mezz.jei.recipe.crafting.CraftingRecipeType;
import mezz.jei.plugins.vanilla.crafting.ShapedOreRecipeHelper;
import mezz.jei.plugins.vanilla.crafting.ShapedRecipesHelper;
import mezz.jei.plugins.vanilla.crafting.ShapelessOreRecipeHelper;
import mezz.jei.plugins.vanilla.crafting.ShapelessRecipesHelper;
import mezz.jei.recipe.furnace.FurnaceRecipeType;
import mezz.jei.recipe.furnace.fuel.FuelRecipe;
import mezz.jei.recipe.furnace.fuel.FuelRecipeHelper;
import mezz.jei.recipe.furnace.fuel.FuelRecipeMaker;
import mezz.jei.recipe.furnace.smelting.SmeltingRecipe;
import mezz.jei.recipe.furnace.smelting.SmeltingRecipeHelper;
import mezz.jei.recipe.furnace.smelting.SmeltingRecipeMaker;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nonnull;
import java.util.List;

@Mod(modid = Constants.MODID,
		name = Constants.NAME,
		version = Constants.VERSION,
		guiFactory = "mezz.jei.config.JEIModGuiFactory",
		dependencies = "required-after:Forge@[10.13.0.1207,);")
public class JustEnoughItems {
	@Mod.Instance(Constants.MODID)
	public static JustEnoughItems instance;

	@Nonnull
	public static ItemFilter itemFilter;

	public JustEnoughItems() {
		itemFilter = new ItemFilter();
		JEIManager.guiHelper = new GuiHelper();
		JEIManager.recipeRegistry = new RecipeRegistry();
	}

	@EventHandler
	public void preInit(@Nonnull FMLPreInitializationEvent event) {
		Config.preInit(event);

		JEIManager.recipeRegistry.registerRecipeType(EnumRecipeTypeKey.CRAFTING_TABLE, new CraftingRecipeType());
		JEIManager.recipeRegistry.registerRecipeType(EnumRecipeTypeKey.FURNACE, new FurnaceRecipeType());

		JEIManager.recipeRegistry.registerRecipeHelpers(
				new ShapedRecipesHelper(),
				new ShapedOreRecipeHelper(),
				new ShapelessRecipesHelper(),
				new ShapelessOreRecipeHelper(),
				new SmeltingRecipeHelper(),
				new FuelRecipeHelper(),
				new ForestryShapedRecipeHelper()
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
		ItemRegistry itemRegistry = new ItemRegistry();

		itemFilter.init(itemRegistry.getItemList());

		JEIManager.recipeRegistry.addRecipes(CraftingManager.getInstance().getRecipeList());

		List<SmeltingRecipe> smeltingRecipeList = SmeltingRecipeMaker.getFurnaceRecipes(FurnaceRecipes.smelting());
		JEIManager.recipeRegistry.addRecipes(smeltingRecipeList);

		List<FuelRecipe> fuelRecipeList = FuelRecipeMaker.getFuelRecipes(itemRegistry.getFuels());
		JEIManager.recipeRegistry.addRecipes(fuelRecipeList);
	}

	@SubscribeEvent
	public void onConfigChanged(@Nonnull ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		Config.onConfigChanged(eventArgs);
	}
}
