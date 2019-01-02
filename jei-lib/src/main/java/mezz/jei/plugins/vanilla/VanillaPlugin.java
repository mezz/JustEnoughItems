package mezz.jei.plugins.vanilla;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.fluids.FluidStack;
import net.minecraft.client.gui.GuiRepair;
import net.minecraft.client.gui.inventory.GuiBrewingStand;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiFurnace;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;

import com.google.common.base.Preconditions;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ModIds;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.IModIdHelper;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.IStackHelper;
import mezz.jei.api.recipe.IVanillaRecipeFactory;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;
import mezz.jei.plugins.vanilla.anvil.AnvilRecipeCategory;
import mezz.jei.plugins.vanilla.anvil.AnvilRecipeMaker;
import mezz.jei.plugins.vanilla.brewing.BrewingRecipeCategory;
import mezz.jei.plugins.vanilla.brewing.BrewingRecipeMaker;
import mezz.jei.plugins.vanilla.brewing.PotionSubtypeInterpreter;
import mezz.jei.plugins.vanilla.crafting.CraftingRecipeCategory;
import mezz.jei.plugins.vanilla.crafting.RecipeValidator;
import mezz.jei.plugins.vanilla.crafting.ShapedRecipesWrapper;
import mezz.jei.plugins.vanilla.crafting.ShapelessRecipeWrapper;
import mezz.jei.plugins.vanilla.crafting.TippedArrowRecipeMaker;
import mezz.jei.plugins.vanilla.furnace.FuelRecipeMaker;
import mezz.jei.plugins.vanilla.furnace.FurnaceFuelCategory;
import mezz.jei.plugins.vanilla.furnace.FurnaceSmeltingCategory;
import mezz.jei.plugins.vanilla.ingredients.enchant.EnchantDataHelper;
import mezz.jei.plugins.vanilla.ingredients.enchant.EnchantDataListFactory;
import mezz.jei.plugins.vanilla.ingredients.enchant.EnchantDataRenderer;
import mezz.jei.plugins.vanilla.ingredients.enchant.EnchantedBookCache;
import mezz.jei.plugins.vanilla.ingredients.fluid.FluidStackHelper;
import mezz.jei.plugins.vanilla.ingredients.fluid.FluidStackListFactory;
import mezz.jei.plugins.vanilla.ingredients.fluid.FluidStackRenderer;
import mezz.jei.plugins.vanilla.ingredients.item.ItemStackHelper;
import mezz.jei.plugins.vanilla.ingredients.item.ItemStackListFactory;
import mezz.jei.plugins.vanilla.ingredients.item.ItemStackRenderer;
import mezz.jei.transfer.PlayerRecipeTransferHandler;
import mezz.jei.util.StackHelper;

@JEIPlugin
public class VanillaPlugin implements IModPlugin {
	@Nullable
	private ISubtypeRegistry subtypeRegistry;

	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(ModIds.JEI_ID, "minecraft");
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
		this.subtypeRegistry = subtypeRegistry;

		subtypeRegistry.registerSubtypeInterpreter(Items.TIPPED_ARROW, PotionSubtypeInterpreter.INSTANCE);
		subtypeRegistry.registerSubtypeInterpreter(Items.POTION, PotionSubtypeInterpreter.INSTANCE);
		subtypeRegistry.registerSubtypeInterpreter(Items.SPLASH_POTION, PotionSubtypeInterpreter.INSTANCE);
		subtypeRegistry.registerSubtypeInterpreter(Items.LINGERING_POTION, PotionSubtypeInterpreter.INSTANCE);
		subtypeRegistry.registerSubtypeInterpreter(Items.ENCHANTED_BOOK, itemStack -> {
			List<String> enchantmentNames = new ArrayList<>();
			NBTTagList enchantments = ItemEnchantedBook.getEnchantments(itemStack);
			for (INBTBase nbt : enchantments) {
				if (nbt instanceof NBTTagCompound) {
					NBTTagCompound nbttagcompound = (NBTTagCompound) nbt;
					int j = nbttagcompound.getShort("id");
					Enchantment enchantment = Enchantment.getEnchantmentByID(j);
					if (enchantment != null) {
						String enchantmentUid = enchantment.getName() + ".lvl" + nbttagcompound.getShort("lvl");
						enchantmentNames.add(enchantmentUid);
					}
				}
			}
			enchantmentNames.sort(null);
			return enchantmentNames.toString();
		});
	}

	@Override
	public void registerIngredients(IModIngredientRegistration ingredientRegistration) {
		Preconditions.checkState(this.subtypeRegistry != null);
		StackHelper stackHelper = new StackHelper(subtypeRegistry);
		ItemStackListFactory itemStackListFactory = new ItemStackListFactory(this.subtypeRegistry);

		List<ItemStack> itemStacks = itemStackListFactory.create(stackHelper);
		ItemStackHelper itemStackHelper = new ItemStackHelper(stackHelper);
		ItemStackRenderer itemStackRenderer = new ItemStackRenderer();
		ingredientRegistration.register(VanillaTypes.ITEM, itemStacks, itemStackHelper, itemStackRenderer);

		List<FluidStack> fluidStacks = FluidStackListFactory.create();
		FluidStackHelper fluidStackHelper = new FluidStackHelper();
		FluidStackRenderer fluidStackRenderer = new FluidStackRenderer();
		ingredientRegistration.register(VanillaTypes.FLUID, fluidStacks, fluidStackHelper, fluidStackRenderer);

		List<EnchantmentData> enchantments = EnchantDataListFactory.create();
		EnchantedBookCache enchantedBookCache = new EnchantedBookCache();
		EnchantDataHelper enchantmentHelper = new EnchantDataHelper(enchantedBookCache, itemStackHelper);
		EnchantDataRenderer enchantmentRenderer = new EnchantDataRenderer(itemStackRenderer, enchantedBookCache);
		ingredientRegistration.register(() -> EnchantmentData.class, enchantments, enchantmentHelper, enchantmentRenderer);
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		IJeiHelpers jeiHelpers = registry.getJeiHelpers();
		IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
		IModIdHelper modIdHelper = jeiHelpers.getModIdHelper();
		registry.addRecipeCategories(
			new CraftingRecipeCategory(guiHelper, modIdHelper),
			new FurnaceFuelCategory(guiHelper),
			new FurnaceSmeltingCategory(guiHelper),
			new BrewingRecipeCategory(guiHelper),
			new AnvilRecipeCategory(guiHelper)
		);
	}

	@Override
	public void register(IModRegistry registry) {
		IIngredientRegistry ingredientRegistry = registry.getIngredientRegistry();
		IJeiHelpers jeiHelpers = registry.getJeiHelpers();
		IStackHelper stackHelper = jeiHelpers.getStackHelper();
		IVanillaRecipeFactory vanillaRecipeFactory = jeiHelpers.getVanillaRecipeFactory();

		RecipeValidator.Results recipes = RecipeValidator.getValidRecipes(jeiHelpers);
		registry.addRecipes(recipes.getCraftingRecipes(), VanillaRecipeCategoryUid.CRAFTING);
		registry.addRecipes(recipes.getFurnaceRecipes(), VanillaRecipeCategoryUid.FURNACE);
		registry.addRecipes(FuelRecipeMaker.getFuelRecipes(ingredientRegistry, jeiHelpers), VanillaRecipeCategoryUid.FUEL);
		registry.addRecipes(BrewingRecipeMaker.getBrewingRecipes(ingredientRegistry, vanillaRecipeFactory), VanillaRecipeCategoryUid.BREWING);
		registry.addRecipes(TippedArrowRecipeMaker.getTippedArrowRecipes(), VanillaRecipeCategoryUid.CRAFTING);
		registry.addRecipes(AnvilRecipeMaker.getAnvilRecipes(vanillaRecipeFactory, ingredientRegistry), VanillaRecipeCategoryUid.ANVIL);

		registry.handleRecipes(IShapedRecipe.class, recipe -> new ShapedRecipesWrapper(jeiHelpers, recipe), VanillaRecipeCategoryUid.CRAFTING);
		registry.handleRecipes(IRecipe.class, recipe -> new ShapelessRecipeWrapper<>(jeiHelpers, recipe), VanillaRecipeCategoryUid.CRAFTING);

		registry.addRecipeClickArea(GuiCrafting.class, 88, 32, 28, 23, VanillaRecipeCategoryUid.CRAFTING);
		registry.addRecipeClickArea(GuiInventory.class, 137, 29, 10, 13, VanillaRecipeCategoryUid.CRAFTING);
		registry.addRecipeClickArea(GuiBrewingStand.class, 97, 16, 14, 30, VanillaRecipeCategoryUid.BREWING);
		registry.addRecipeClickArea(GuiFurnace.class, 78, 32, 28, 23, VanillaRecipeCategoryUid.FURNACE, VanillaRecipeCategoryUid.FUEL);
		registry.addRecipeClickArea(GuiRepair.class, 102, 48, 22, 15, VanillaRecipeCategoryUid.ANVIL);

		IRecipeTransferRegistry recipeTransferRegistry = registry.getRecipeTransferRegistry();

		recipeTransferRegistry.addRecipeTransferHandler(ContainerWorkbench.class, VanillaRecipeCategoryUid.CRAFTING, 1, 9, 10, 36);
		recipeTransferRegistry.addRecipeTransferHandler(new PlayerRecipeTransferHandler(stackHelper, jeiHelpers.recipeTransferHandlerHelper()), VanillaRecipeCategoryUid.CRAFTING);
		recipeTransferRegistry.addRecipeTransferHandler(ContainerFurnace.class, VanillaRecipeCategoryUid.FURNACE, 0, 1, 3, 36);
		recipeTransferRegistry.addRecipeTransferHandler(ContainerFurnace.class, VanillaRecipeCategoryUid.FUEL, 1, 1, 3, 36);
		recipeTransferRegistry.addRecipeTransferHandler(ContainerBrewingStand.class, VanillaRecipeCategoryUid.BREWING, 0, 4, 5, 36);
		recipeTransferRegistry.addRecipeTransferHandler(ContainerRepair.class, VanillaRecipeCategoryUid.ANVIL, 0, 2, 3, 36);

		registry.addRecipeCatalyst(new ItemStack(Blocks.CRAFTING_TABLE), VanillaRecipeCategoryUid.CRAFTING);
		registry.addRecipeCatalyst(new ItemStack(Blocks.FURNACE), VanillaRecipeCategoryUid.FURNACE, VanillaRecipeCategoryUid.FUEL);
		registry.addRecipeCatalyst(new ItemStack(Blocks.BREWING_STAND), VanillaRecipeCategoryUid.BREWING);
		registry.addRecipeCatalyst(new ItemStack(Blocks.ANVIL), VanillaRecipeCategoryUid.ANVIL);

		IIngredientBlacklist ingredientBlacklist = registry.getJeiHelpers().getIngredientBlacklist();
		// Game freezes when loading player skulls, see https://bugs.mojang.com/browse/MC-65587
		ingredientBlacklist.addIngredientToBlacklist(new ItemStack(Blocks.PLAYER_HEAD));
		// hide enchanted books, we display them as special ingredients because vanilla does not properly store the enchantment data by its registry name
		ingredientBlacklist.addIngredientToBlacklist(new ItemStack(Items.ENCHANTED_BOOK));
	}
}
