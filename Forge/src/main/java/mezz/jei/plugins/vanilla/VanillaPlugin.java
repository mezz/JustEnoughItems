package mezz.jei.plugins.vanilla;

import mezz.jei.Internal;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.ingredients.subtypes.ISubtypeManager;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IExtendableRecipeCategory;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.textures.Textures;
import mezz.jei.plugins.vanilla.anvil.AnvilRecipeCategory;
import mezz.jei.plugins.vanilla.anvil.AnvilRecipeMaker;
import mezz.jei.plugins.vanilla.anvil.SmithingRecipeCategory;
import mezz.jei.plugins.vanilla.brewing.BrewingRecipeCategory;
import mezz.jei.plugins.vanilla.brewing.BrewingRecipeMaker;
import mezz.jei.plugins.vanilla.brewing.PotionSubtypeInterpreter;
import mezz.jei.plugins.vanilla.compostable.CompostableRecipeCategory;
import mezz.jei.plugins.vanilla.compostable.CompostingRecipeMaker;
import mezz.jei.plugins.vanilla.cooking.BlastingCategory;
import mezz.jei.plugins.vanilla.cooking.CampfireCookingCategory;
import mezz.jei.plugins.vanilla.cooking.FurnaceSmeltingCategory;
import mezz.jei.plugins.vanilla.cooking.SmokingCategory;
import mezz.jei.plugins.vanilla.cooking.fuel.FuelRecipeMaker;
import mezz.jei.plugins.vanilla.cooking.fuel.FurnaceFuelCategory;
import mezz.jei.plugins.vanilla.crafting.CraftingCategoryExtension;
import mezz.jei.plugins.vanilla.crafting.CraftingRecipeCategory;
import mezz.jei.plugins.vanilla.crafting.replacers.ShulkerBoxColoringRecipeMaker;
import mezz.jei.plugins.vanilla.crafting.replacers.SuspiciousStewRecipeMaker;
import mezz.jei.plugins.vanilla.crafting.replacers.TippedArrowRecipeMaker;
import mezz.jei.plugins.vanilla.crafting.VanillaRecipes;
import mezz.jei.plugins.vanilla.ingredients.fluid.FluidStackHelper;
import mezz.jei.plugins.vanilla.ingredients.fluid.FluidStackListFactory;
import mezz.jei.plugins.vanilla.ingredients.fluid.FluidStackRenderer;
import mezz.jei.plugins.vanilla.ingredients.item.ItemStackHelper;
import mezz.jei.plugins.vanilla.ingredients.item.ItemStackListFactory;
import mezz.jei.plugins.vanilla.ingredients.item.ItemStackRenderer;
import mezz.jei.plugins.vanilla.stonecutting.StoneCuttingRecipeCategory;
import mezz.jei.transfer.PlayerRecipeTransferHandler;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.StackHelper;
import net.minecraft.world.item.crafting.ShulkerBoxColoring;
import net.minecraft.world.item.crafting.SuspiciousStewRecipe;
import net.minecraft.world.item.crafting.TippedArrowRecipe;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.BlastFurnaceScreen;
import net.minecraft.client.gui.screens.inventory.BrewingStandScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.FurnaceScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.SmithingScreen;
import net.minecraft.client.gui.screens.inventory.SmokerScreen;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.inventory.BlastFurnaceMenu;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.inventory.SmokerMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

@JeiPlugin
public class VanillaPlugin implements IModPlugin {
	private static final Logger LOGGER = LogManager.getLogger();

	@Nullable
	private CraftingRecipeCategory craftingCategory;
	@Nullable
	private IRecipeCategory<StonecutterRecipe> stonecuttingCategory;
	@Nullable
	private IRecipeCategory<SmeltingRecipe> furnaceCategory;
	@Nullable
	private IRecipeCategory<SmokingRecipe> smokingCategory;
	@Nullable
	private IRecipeCategory<BlastingRecipe> blastingCategory;
	@Nullable
	private IRecipeCategory<CampfireCookingRecipe> campfireCategory;
	@Nullable
	private IRecipeCategory<UpgradeRecipe> smithingCategory;

	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(ModIds.JEI_ID, "minecraft");
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration registration) {
		registration.registerSubtypeInterpreter(Items.TIPPED_ARROW, PotionSubtypeInterpreter.INSTANCE);
		registration.registerSubtypeInterpreter(Items.POTION, PotionSubtypeInterpreter.INSTANCE);
		registration.registerSubtypeInterpreter(Items.SPLASH_POTION, PotionSubtypeInterpreter.INSTANCE);
		registration.registerSubtypeInterpreter(Items.LINGERING_POTION, PotionSubtypeInterpreter.INSTANCE);
		registration.registerSubtypeInterpreter(Items.ENCHANTED_BOOK, (itemStack, context) -> {
			List<String> enchantmentNames = new ArrayList<>();
			ListTag enchantments = EnchantedBookItem.getEnchantments(itemStack);
			for (int i = 0; i < enchantments.size(); ++i) {
				CompoundTag compoundnbt = enchantments.getCompound(i);
				String id = compoundnbt.getString("id");
				Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(ResourceLocation.tryParse(id));
				if (enchantment != null) {
					String enchantmentUid = enchantment.getDescriptionId() + ".lvl" + compoundnbt.getShort("lvl");
					enchantmentNames.add(enchantmentUid);
				}
			}
			enchantmentNames.sort(null);
			return enchantmentNames.toString();
		});
	}

	@Override
	public void registerIngredients(IModIngredientRegistration registration) {
		ISubtypeManager subtypeManager = registration.getSubtypeManager();
		StackHelper stackHelper = new StackHelper(subtypeManager);
		IColorHelper colorHelper = registration.getColorHelper();

		List<ItemStack> itemStacks = ItemStackListFactory.create(stackHelper);
		ItemStackHelper itemStackHelper = new ItemStackHelper(stackHelper);
		ItemStackRenderer itemStackRenderer = new ItemStackRenderer();
		registration.register(VanillaTypes.ITEM, itemStacks, itemStackHelper, itemStackRenderer);

		List<FluidStack> fluidStacks = FluidStackListFactory.create();
		FluidStackHelper fluidStackHelper = new FluidStackHelper(subtypeManager, colorHelper);
		FluidStackRenderer fluidStackRenderer = new FluidStackRenderer();
		registration.register(VanillaTypes.FLUID, fluidStacks, fluidStackHelper, fluidStackRenderer);
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		Textures textures = Internal.getTextures();
		IJeiHelpers jeiHelpers = registration.getJeiHelpers();
		IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
		registration.addRecipeCategories(
			craftingCategory = new CraftingRecipeCategory(guiHelper),
			stonecuttingCategory = new StoneCuttingRecipeCategory(guiHelper),
			furnaceCategory = new FurnaceSmeltingCategory(guiHelper),
			smokingCategory = new SmokingCategory(guiHelper),
			blastingCategory = new BlastingCategory(guiHelper),
			campfireCategory = new CampfireCookingCategory(guiHelper),
			smithingCategory = new SmithingRecipeCategory(guiHelper),
			new CompostableRecipeCategory(guiHelper),
			new FurnaceFuelCategory(guiHelper, textures),
			new BrewingRecipeCategory(guiHelper),
			new AnvilRecipeCategory(guiHelper)
		);
	}

	@Override
	public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
		IExtendableRecipeCategory<CraftingRecipe, ICraftingCategoryExtension> craftingCategory = registration.getCraftingCategory();
		craftingCategory.addCategoryExtension(CraftingRecipe.class, r -> !r.isSpecial(), CraftingCategoryExtension::new);
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		ErrorUtil.checkNotNull(craftingCategory, "craftingCategory");
		ErrorUtil.checkNotNull(stonecuttingCategory, "stonecuttingCategory");
		ErrorUtil.checkNotNull(furnaceCategory, "furnaceCategory");
		ErrorUtil.checkNotNull(smokingCategory, "smokingCategory");
		ErrorUtil.checkNotNull(blastingCategory, "blastingCategory");
		ErrorUtil.checkNotNull(campfireCategory, "campfireCategory");
		ErrorUtil.checkNotNull(smithingCategory, "smithingCategory");

		IIngredientManager ingredientManager = registration.getIngredientManager();
		IVanillaRecipeFactory vanillaRecipeFactory = registration.getVanillaRecipeFactory();
		VanillaRecipes vanillaRecipes = new VanillaRecipes();

		Map<Boolean, List<CraftingRecipe>> craftingRecipes = vanillaRecipes.getCraftingRecipes(craftingCategory);
		List<CraftingRecipe> handledCraftingRecipes = craftingRecipes.get(true);
		List<CraftingRecipe> unhandledCraftingRecipes = craftingRecipes.get(false);
		List<CraftingRecipe> specialCraftingRecipes = replaceSpecialCraftingRecipes(unhandledCraftingRecipes);

		registration.addRecipes(RecipeTypes.CRAFTING, handledCraftingRecipes);
		registration.addRecipes(RecipeTypes.CRAFTING, specialCraftingRecipes);

		registration.addRecipes(RecipeTypes.STONECUTTING, vanillaRecipes.getStonecuttingRecipes(stonecuttingCategory));
		registration.addRecipes(RecipeTypes.SMELTING, vanillaRecipes.getFurnaceRecipes(furnaceCategory));
		registration.addRecipes(RecipeTypes.SMOKING, vanillaRecipes.getSmokingRecipes(smokingCategory));
		registration.addRecipes(RecipeTypes.BLASTING, vanillaRecipes.getBlastingRecipes(blastingCategory));
		registration.addRecipes(RecipeTypes.CAMPFIRE_COOKING, vanillaRecipes.getCampfireCookingRecipes(campfireCategory));
		registration.addRecipes(RecipeTypes.FUELING, FuelRecipeMaker.getFuelRecipes(ingredientManager));
		registration.addRecipes(RecipeTypes.BREWING, BrewingRecipeMaker.getBrewingRecipes(ingredientManager, vanillaRecipeFactory));
		registration.addRecipes(RecipeTypes.ANVIL, AnvilRecipeMaker.getAnvilRecipes(vanillaRecipeFactory, ingredientManager));
		registration.addRecipes(RecipeTypes.SMITHING, vanillaRecipes.getSmithingRecipes(smithingCategory));
		registration.addRecipes(RecipeTypes.COMPOSTING, CompostingRecipeMaker.getRecipes(ingredientManager));
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		registration.addRecipeClickArea(CraftingScreen.class, 88, 32, 28, 23, RecipeTypes.CRAFTING);
		registration.addRecipeClickArea(InventoryScreen.class, 137, 29, 10, 13, RecipeTypes.CRAFTING);
		registration.addRecipeClickArea(BrewingStandScreen.class, 97, 16, 14, 30, RecipeTypes.BREWING);
		registration.addRecipeClickArea(FurnaceScreen.class, 78, 32, 28, 23, RecipeTypes.SMELTING, RecipeTypes.FUELING);
		registration.addRecipeClickArea(SmokerScreen.class, 78, 32, 28, 23, RecipeTypes.SMOKING, RecipeTypes.FUELING);
		registration.addRecipeClickArea(BlastFurnaceScreen.class, 78, 32, 28, 23, RecipeTypes.BLASTING, RecipeTypes.FUELING);
		registration.addRecipeClickArea(AnvilScreen.class, 102, 48, 22, 15, RecipeTypes.ANVIL);
		registration.addRecipeClickArea(SmithingScreen.class, 102, 48, 22, 15, RecipeTypes.SMITHING);

		registration.addGenericGuiContainerHandler(EffectRenderingInventoryScreen.class, new InventoryEffectRendererGuiHandler<>());
		registration.addGuiContainerHandler(CraftingScreen.class, new RecipeBookGuiHandler<>());
		registration.addGuiContainerHandler(InventoryScreen.class, new RecipeBookGuiHandler<>());
		registration.addGuiContainerHandler(AbstractFurnaceScreen.class, new RecipeBookGuiHandler<>());
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
		IJeiHelpers jeiHelpers = registration.getJeiHelpers();
		IRecipeTransferHandlerHelper transferHelper = registration.getTransferHelper();
		IStackHelper stackHelper = jeiHelpers.getStackHelper();
		registration.addRecipeTransferHandler(CraftingMenu.class, RecipeTypes.CRAFTING, 1, 9, 10, 36);
		registration.addRecipeTransferHandler(new PlayerRecipeTransferHandler(stackHelper, transferHelper), RecipeTypes.CRAFTING);
		registration.addRecipeTransferHandler(FurnaceMenu.class, RecipeTypes.SMELTING, 0, 1, 3, 36);
		registration.addRecipeTransferHandler(FurnaceMenu.class, RecipeTypes.FUELING, 1, 1, 3, 36);
		registration.addRecipeTransferHandler(SmokerMenu.class, RecipeTypes.SMOKING, 0, 1, 3, 36);
		registration.addRecipeTransferHandler(SmokerMenu.class, RecipeTypes.FUELING, 1, 1, 3, 36);
		registration.addRecipeTransferHandler(BlastFurnaceMenu.class, RecipeTypes.BLASTING, 0, 1, 3, 36);
		registration.addRecipeTransferHandler(BlastFurnaceMenu.class, RecipeTypes.FUELING, 1, 1, 3, 36);
		registration.addRecipeTransferHandler(BrewingStandMenu.class, RecipeTypes.BREWING, 0, 4, 5, 36);
		registration.addRecipeTransferHandler(AnvilMenu.class, RecipeTypes.ANVIL, 0, 2, 3, 36);
		registration.addRecipeTransferHandler(SmithingMenu.class, RecipeTypes.SMITHING, 0, 2, 3, 36);
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		registration.addRecipeCatalyst(new ItemStack(Blocks.CRAFTING_TABLE), RecipeTypes.CRAFTING);
		registration.addRecipeCatalyst(new ItemStack(Blocks.STONECUTTER), RecipeTypes.STONECUTTING);
		registration.addRecipeCatalyst(new ItemStack(Blocks.FURNACE), RecipeTypes.SMELTING, RecipeTypes.FUELING);

		registration.addRecipeCatalyst(new ItemStack(Blocks.SMOKER), RecipeTypes.SMOKING, RecipeTypes.FUELING);
		registration.addRecipeCatalyst(new ItemStack(Blocks.BLAST_FURNACE), RecipeTypes.BLASTING, RecipeTypes.FUELING);
		registration.addRecipeCatalyst(new ItemStack(Blocks.CAMPFIRE), RecipeTypes.CAMPFIRE_COOKING);
		registration.addRecipeCatalyst(new ItemStack(Blocks.SOUL_CAMPFIRE), RecipeTypes.CAMPFIRE_COOKING);
		registration.addRecipeCatalyst(new ItemStack(Blocks.BREWING_STAND), RecipeTypes.BREWING);
		registration.addRecipeCatalyst(new ItemStack(Blocks.ANVIL), RecipeTypes.ANVIL);
		registration.addRecipeCatalyst(new ItemStack(Blocks.SMITHING_TABLE), RecipeTypes.SMITHING);
		registration.addRecipeCatalyst(new ItemStack(Blocks.COMPOSTER), RecipeTypes.COMPOSTING);
	}

	@Nullable
	public CraftingRecipeCategory getCraftingCategory() {
		return craftingCategory;
	}

	/**
	 * By default, JEI can't handle special recipes.
	 * This method expands some special unhandled recipes into a list of normal recipes that JEI can understand.
	 *
	 * If a special recipe we know how to replace is not present (because it has been removed),
	 * we do not replace it.
	 */
	private static List<CraftingRecipe> replaceSpecialCraftingRecipes(List<CraftingRecipe> unhandledCraftingRecipes) {
		Map<Class<? extends CraftingRecipe>, Supplier<List<CraftingRecipe>>> replacers = new IdentityHashMap<>();
		replacers.put(TippedArrowRecipe.class, TippedArrowRecipeMaker::createRecipes);
		replacers.put(ShulkerBoxColoring.class, ShulkerBoxColoringRecipeMaker::createRecipes);
		replacers.put(SuspiciousStewRecipe.class, SuspiciousStewRecipeMaker::createRecipes);

		return unhandledCraftingRecipes.stream()
			.map(CraftingRecipe::getClass)
			.distinct()
			.filter(replacers::containsKey)
			// distinct + this limit will ensure we stop iterating early if we find all the recipes we're looking for.
			.limit(replacers.size())
			.flatMap(recipeClass -> {
				Supplier<List<CraftingRecipe>> supplier = replacers.get(recipeClass);
				try {
					List<CraftingRecipe> results = supplier.get();
					return results.stream();
				} catch (RuntimeException e) {
					LOGGER.error("Failed to create JEI recipes for {}", recipeClass, e);
					return Stream.of();
				}
			})
			.toList();
	}
}
