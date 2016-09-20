package mezz.jei.plugins.vanilla;

import javax.annotation.Nullable;

import mezz.jei.api.BlankModPlugin;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IItemRegistry;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;
import mezz.jei.plugins.vanilla.brewing.BrewingRecipeCategory;
import mezz.jei.plugins.vanilla.brewing.BrewingRecipeHandler;
import mezz.jei.plugins.vanilla.brewing.BrewingRecipeMaker;
import mezz.jei.plugins.vanilla.brewing.PotionSubtypeInterpreter;
import mezz.jei.plugins.vanilla.crafting.CraftingRecipeCategory;
import mezz.jei.plugins.vanilla.crafting.ShapedOreRecipeHandler;
import mezz.jei.plugins.vanilla.crafting.ShapedRecipesHandler;
import mezz.jei.plugins.vanilla.crafting.ShapelessOreRecipeHandler;
import mezz.jei.plugins.vanilla.crafting.ShapelessRecipesHandler;
import mezz.jei.plugins.vanilla.crafting.TippedArrowRecipeHandler;
import mezz.jei.plugins.vanilla.crafting.TippedArrowRecipeMaker;
import mezz.jei.plugins.vanilla.furnace.FuelRecipeHandler;
import mezz.jei.plugins.vanilla.furnace.FuelRecipeMaker;
import mezz.jei.plugins.vanilla.furnace.FurnaceFuelCategory;
import mezz.jei.plugins.vanilla.furnace.FurnaceSmeltingCategory;
import mezz.jei.plugins.vanilla.furnace.SmeltingRecipeHandler;
import mezz.jei.plugins.vanilla.furnace.SmeltingRecipeMaker;
import net.minecraft.client.gui.inventory.GuiBrewingStand;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiFurnace;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemBanner;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fluids.FluidRegistry;

@JEIPlugin
public class VanillaPlugin extends BlankModPlugin {
	@Override
	public void register(IModRegistry registry) {
		IItemRegistry itemRegistry = registry.getItemRegistry();
		IJeiHelpers jeiHelpers = registry.getJeiHelpers();

		ISubtypeRegistry subtypeRegistry = jeiHelpers.getSubtypeRegistry();
		subtypeRegistry.useNbtForSubtypes(
				Items.ENCHANTED_BOOK
		);

		subtypeRegistry.registerNbtInterpreter(Items.TIPPED_ARROW, PotionSubtypeInterpreter.INSTANCE);
		subtypeRegistry.registerNbtInterpreter(Items.POTIONITEM, PotionSubtypeInterpreter.INSTANCE);
		subtypeRegistry.registerNbtInterpreter(Items.SPLASH_POTION, PotionSubtypeInterpreter.INSTANCE);
		subtypeRegistry.registerNbtInterpreter(Items.LINGERING_POTION, PotionSubtypeInterpreter.INSTANCE);
		subtypeRegistry.registerNbtInterpreter(Items.BANNER, new ISubtypeRegistry.ISubtypeInterpreter() {
			@Nullable
			@Override
			public String getSubtypeInfo(ItemStack itemStack) {
				EnumDyeColor baseColor = ItemBanner.getBaseColor(itemStack);
				return baseColor.toString();
			}
		});
		subtypeRegistry.registerNbtInterpreter(Items.SPAWN_EGG, new ISubtypeRegistry.ISubtypeInterpreter() {
			@Nullable
			@Override
			public String getSubtypeInfo(ItemStack itemStack) {
				return ItemMonsterPlacer.getEntityIdFromItem(itemStack);
			}
		});

		if (FluidRegistry.isUniversalBucketEnabled()) {
			subtypeRegistry.useNbtForSubtypes(ForgeModContainer.getInstance().universalBucket);
		}

		IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
		registry.addRecipeCategories(
				new CraftingRecipeCategory(guiHelper),
				new FurnaceFuelCategory(guiHelper),
				new FurnaceSmeltingCategory(guiHelper),
				new BrewingRecipeCategory(guiHelper)
		);

		registry.addRecipeHandlers(
				new ShapedOreRecipeHandler(),
				new ShapedRecipesHandler(),
				new ShapelessOreRecipeHandler(guiHelper),
				new ShapelessRecipesHandler(guiHelper),
				new TippedArrowRecipeHandler(),
				new FuelRecipeHandler(),
				new SmeltingRecipeHandler(),
				new BrewingRecipeHandler()
		);

		registry.addRecipeClickArea(GuiCrafting.class, 88, 32, 28, 23, VanillaRecipeCategoryUid.CRAFTING);
		registry.addRecipeClickArea(GuiBrewingStand.class, 97, 16, 14, 30, VanillaRecipeCategoryUid.BREWING);
		registry.addRecipeClickArea(GuiFurnace.class, 78, 32, 28, 23, VanillaRecipeCategoryUid.SMELTING, VanillaRecipeCategoryUid.FUEL);

		IRecipeTransferRegistry recipeTransferRegistry = registry.getRecipeTransferRegistry();

		recipeTransferRegistry.addRecipeTransferHandler(ContainerWorkbench.class, VanillaRecipeCategoryUid.CRAFTING, 1, 9, 10, 36);
		recipeTransferRegistry.addRecipeTransferHandler(ContainerFurnace.class, VanillaRecipeCategoryUid.SMELTING, 0, 1, 3, 36);
		recipeTransferRegistry.addRecipeTransferHandler(ContainerFurnace.class, VanillaRecipeCategoryUid.FUEL, 1, 1, 3, 36);
		recipeTransferRegistry.addRecipeTransferHandler(ContainerBrewingStand.class, VanillaRecipeCategoryUid.BREWING, 0, 4, 5, 36);

		registry.addRecipeCategoryCraftingItem(new ItemStack(Blocks.CRAFTING_TABLE), VanillaRecipeCategoryUid.CRAFTING);
		registry.addRecipeCategoryCraftingItem(new ItemStack(Blocks.FURNACE), VanillaRecipeCategoryUid.SMELTING, VanillaRecipeCategoryUid.FUEL);
		registry.addRecipeCategoryCraftingItem(new ItemStack(Items.BREWING_STAND), VanillaRecipeCategoryUid.BREWING);

		registry.addRecipes(CraftingManager.getInstance().getRecipeList());
		registry.addRecipes(SmeltingRecipeMaker.getFurnaceRecipes(jeiHelpers));
		registry.addRecipes(FuelRecipeMaker.getFuelRecipes(itemRegistry, jeiHelpers));
		registry.addRecipes(BrewingRecipeMaker.getBrewingRecipes(itemRegistry));
		registry.addRecipes(TippedArrowRecipeMaker.getTippedArrowRecipes());
	}
}
