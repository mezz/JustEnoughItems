package mezz.jei.plugins.vanilla;

import mezz.jei.Internal;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
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
import mezz.jei.plugins.vanilla.cooking.BlastingCategory;
import mezz.jei.plugins.vanilla.cooking.CampfireCategory;
import mezz.jei.plugins.vanilla.cooking.FurnaceSmeltingCategory;
import mezz.jei.plugins.vanilla.cooking.SmokingCategory;
import mezz.jei.plugins.vanilla.cooking.fuel.FuelRecipeMaker;
import mezz.jei.plugins.vanilla.cooking.fuel.FurnaceFuelCategory;
import mezz.jei.plugins.vanilla.crafting.CraftingCategoryExtension;
import mezz.jei.plugins.vanilla.crafting.CraftingRecipeCategory;
import mezz.jei.plugins.vanilla.crafting.TippedArrowRecipeMaker;
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
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.gui.screen.inventory.AbstractFurnaceScreen;
import net.minecraft.client.gui.screen.inventory.AnvilScreen;
import net.minecraft.client.gui.screen.inventory.BlastFurnaceScreen;
import net.minecraft.client.gui.screen.inventory.BrewingStandScreen;
import net.minecraft.client.gui.screen.inventory.CraftingScreen;
import net.minecraft.client.gui.screen.inventory.FurnaceScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.screen.inventory.SmithingTableScreen;
import net.minecraft.client.gui.screen.inventory.SmokerScreen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.container.BlastFurnaceContainer;
import net.minecraft.inventory.container.BrewingStandContainer;
import net.minecraft.inventory.container.FurnaceContainer;
import net.minecraft.inventory.container.RepairContainer;
import net.minecraft.inventory.container.SmithingTableContainer;
import net.minecraft.inventory.container.SmokerContainer;
import net.minecraft.inventory.container.WorkbenchContainer;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.BlastingRecipe;
import net.minecraft.item.crafting.CampfireCookingRecipe;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.SmithingRecipe;
import net.minecraft.item.crafting.SmokingRecipe;
import net.minecraft.item.crafting.StonecuttingRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class VanillaPlugin implements IModPlugin {
	@Nullable
	private CraftingRecipeCategory craftingCategory;
	@Nullable
	private IRecipeCategory<StonecuttingRecipe> stonecuttingCategory;
	@Nullable
	private IRecipeCategory<FurnaceRecipe> furnaceCategory;
	@Nullable
	private IRecipeCategory<SmokingRecipe> smokingCategory;
	@Nullable
	private IRecipeCategory<BlastingRecipe> blastingCategory;
	@Nullable
	private IRecipeCategory<CampfireCookingRecipe> campfireCategory;
	@Nullable
	private IRecipeCategory<SmithingRecipe> smithingCategory;

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
			ListNBT enchantments = EnchantedBookItem.getEnchantments(itemStack);
			for (int i = 0; i < enchantments.size(); ++i) {
				CompoundNBT compoundnbt = enchantments.getCompound(i);
				String id = compoundnbt.getString("id");
				Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(ResourceLocation.tryCreate(id));
				if (enchantment != null) {
					String enchantmentUid = enchantment.getName() + ".lvl" + compoundnbt.getShort("lvl");
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
		ItemStackListFactory itemStackListFactory = new ItemStackListFactory();
		IColorHelper colorHelper = registration.getColorHelper();

		List<ItemStack> itemStacks = itemStackListFactory.create(stackHelper);
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
			campfireCategory = new CampfireCategory(guiHelper),
			smithingCategory = new SmithingRecipeCategory(guiHelper),
			new FurnaceFuelCategory(guiHelper, textures),
			new BrewingRecipeCategory(guiHelper),
			new AnvilRecipeCategory(guiHelper)
		);
	}

	@Override
	public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
		IExtendableRecipeCategory<ICraftingRecipe, ICraftingCategoryExtension> craftingCategory = registration.getCraftingCategory();
		craftingCategory.addCategoryExtension(ICraftingRecipe.class, r -> !r.isDynamic(), CraftingCategoryExtension::new);
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

		IJeiHelpers jeiHelpers = registration.getJeiHelpers();
		IIngredientManager ingredientManager = registration.getIngredientManager();
		IVanillaRecipeFactory vanillaRecipeFactory = registration.getVanillaRecipeFactory();
		VanillaRecipes vanillaRecipes = new VanillaRecipes();
		registration.addRecipes(vanillaRecipes.getCraftingRecipes(craftingCategory), VanillaRecipeCategoryUid.CRAFTING);
		registration.addRecipes(vanillaRecipes.getStonecuttingRecipes(stonecuttingCategory), VanillaRecipeCategoryUid.STONECUTTING);
		registration.addRecipes(vanillaRecipes.getFurnaceRecipes(furnaceCategory), VanillaRecipeCategoryUid.FURNACE);
		registration.addRecipes(vanillaRecipes.getSmokingRecipes(smokingCategory), VanillaRecipeCategoryUid.SMOKING);
		registration.addRecipes(vanillaRecipes.getBlastingRecipes(blastingCategory), VanillaRecipeCategoryUid.BLASTING);
		registration.addRecipes(vanillaRecipes.getCampfireCookingRecipes(campfireCategory), VanillaRecipeCategoryUid.CAMPFIRE);
		registration.addRecipes(FuelRecipeMaker.getFuelRecipes(ingredientManager, jeiHelpers), VanillaRecipeCategoryUid.FUEL);
		registration.addRecipes(BrewingRecipeMaker.getBrewingRecipes(ingredientManager, vanillaRecipeFactory), VanillaRecipeCategoryUid.BREWING);
		registration.addRecipes(TippedArrowRecipeMaker.createTippedArrowRecipes(), VanillaRecipeCategoryUid.CRAFTING);
		registration.addRecipes(AnvilRecipeMaker.getAnvilRecipes(vanillaRecipeFactory, ingredientManager), VanillaRecipeCategoryUid.ANVIL);
		registration.addRecipes(vanillaRecipes.getSmithingRecipes(smithingCategory), VanillaRecipeCategoryUid.SMITHING);
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		registration.addRecipeClickArea(CraftingScreen.class, 88, 32, 28, 23, VanillaRecipeCategoryUid.CRAFTING);
		registration.addRecipeClickArea(InventoryScreen.class, 137, 29, 10, 13, VanillaRecipeCategoryUid.CRAFTING);
		registration.addRecipeClickArea(BrewingStandScreen.class, 97, 16, 14, 30, VanillaRecipeCategoryUid.BREWING);
		registration.addRecipeClickArea(FurnaceScreen.class, 78, 32, 28, 23, VanillaRecipeCategoryUid.FURNACE, VanillaRecipeCategoryUid.FUEL);
		registration.addRecipeClickArea(SmokerScreen.class, 78, 32, 28, 23, VanillaRecipeCategoryUid.SMOKING, VanillaRecipeCategoryUid.FUEL);
		registration.addRecipeClickArea(BlastFurnaceScreen.class, 78, 32, 28, 23, VanillaRecipeCategoryUid.BLASTING, VanillaRecipeCategoryUid.FUEL);
		registration.addRecipeClickArea(AnvilScreen.class, 102, 48, 22, 15, VanillaRecipeCategoryUid.ANVIL);
		registration.addRecipeClickArea(SmithingTableScreen.class, 102, 48, 22, 15, VanillaRecipeCategoryUid.SMITHING);

		registration.addGenericGuiContainerHandler(DisplayEffectsScreen.class, new InventoryEffectRendererGuiHandler<>());
		registration.addGuiContainerHandler(CraftingScreen.class, new RecipeBookGuiHandler<>());
		registration.addGuiContainerHandler(InventoryScreen.class, new RecipeBookGuiHandler<>());
		registration.addGuiContainerHandler(AbstractFurnaceScreen.class, new RecipeBookGuiHandler<>());
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
		IJeiHelpers jeiHelpers = registration.getJeiHelpers();
		IRecipeTransferHandlerHelper transferHelper = registration.getTransferHelper();
		IStackHelper stackHelper = jeiHelpers.getStackHelper();
		registration.addRecipeTransferHandler(WorkbenchContainer.class, VanillaRecipeCategoryUid.CRAFTING, 1, 9, 10, 36);
		registration.addRecipeTransferHandler(new PlayerRecipeTransferHandler(stackHelper, transferHelper), VanillaRecipeCategoryUid.CRAFTING);
		registration.addRecipeTransferHandler(FurnaceContainer.class, VanillaRecipeCategoryUid.FURNACE, 0, 1, 3, 36);
		registration.addRecipeTransferHandler(FurnaceContainer.class, VanillaRecipeCategoryUid.FUEL, 1, 1, 3, 36);
		registration.addRecipeTransferHandler(SmokerContainer.class, VanillaRecipeCategoryUid.SMOKING, 0, 1, 3, 36);
		registration.addRecipeTransferHandler(SmokerContainer.class, VanillaRecipeCategoryUid.FUEL, 1, 1, 3, 36);
		registration.addRecipeTransferHandler(BlastFurnaceContainer.class, VanillaRecipeCategoryUid.BLASTING, 0, 1, 3, 36);
		registration.addRecipeTransferHandler(BlastFurnaceContainer.class, VanillaRecipeCategoryUid.FUEL, 1, 1, 3, 36);
		registration.addRecipeTransferHandler(BrewingStandContainer.class, VanillaRecipeCategoryUid.BREWING, 0, 4, 5, 36);
		registration.addRecipeTransferHandler(RepairContainer.class, VanillaRecipeCategoryUid.ANVIL, 0, 2, 3, 36);
		registration.addRecipeTransferHandler(SmithingTableContainer.class, VanillaRecipeCategoryUid.SMITHING, 0, 2, 3, 36);
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		registration.addRecipeCatalyst(new ItemStack(Blocks.CRAFTING_TABLE), VanillaRecipeCategoryUid.CRAFTING);
		registration.addRecipeCatalyst(new ItemStack(Blocks.STONECUTTER), VanillaRecipeCategoryUid.STONECUTTING);
		registration.addRecipeCatalyst(new ItemStack(Blocks.FURNACE), VanillaRecipeCategoryUid.FURNACE, VanillaRecipeCategoryUid.FUEL);
		registration.addRecipeCatalyst(new ItemStack(Blocks.SMOKER), VanillaRecipeCategoryUid.SMOKING, VanillaRecipeCategoryUid.FUEL);
		registration.addRecipeCatalyst(new ItemStack(Blocks.BLAST_FURNACE), VanillaRecipeCategoryUid.BLASTING, VanillaRecipeCategoryUid.FUEL);
		registration.addRecipeCatalyst(new ItemStack(Blocks.CAMPFIRE), VanillaRecipeCategoryUid.CAMPFIRE);
		registration.addRecipeCatalyst(new ItemStack(Blocks.SOUL_CAMPFIRE), VanillaRecipeCategoryUid.CAMPFIRE);
		registration.addRecipeCatalyst(new ItemStack(Blocks.BREWING_STAND), VanillaRecipeCategoryUid.BREWING);
		registration.addRecipeCatalyst(new ItemStack(Blocks.ANVIL), VanillaRecipeCategoryUid.ANVIL);
		registration.addRecipeCatalyst(new ItemStack(Blocks.SMITHING_TABLE), VanillaRecipeCategoryUid.SMITHING);
	}

	@Nullable
	public CraftingRecipeCategory getCraftingCategory() {
		return craftingCategory;
	}
}
