package mezz.jei.plugins.vanilla;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.screen.inventory.AbstractFurnaceScreen;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.gui.screen.inventory.AnvilScreen;
import net.minecraft.client.gui.screen.inventory.BlastFurnaceScreen;
import net.minecraft.client.gui.screen.inventory.BrewingStandScreen;
import net.minecraft.client.gui.screen.inventory.CraftingScreen;
import net.minecraft.client.gui.screen.inventory.FurnaceScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.screen.inventory.SmokerScreen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.container.BlastFurnaceContainer;
import net.minecraft.inventory.container.BrewingStandContainer;
import net.minecraft.inventory.container.FurnaceContainer;
import net.minecraft.inventory.container.RepairContainer;
import net.minecraft.inventory.container.SmokerContainer;
import net.minecraft.inventory.container.WorkbenchContainer;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;

import mezz.jei.Internal;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.ingredients.subtypes.ISubtypeManager;
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
import mezz.jei.plugins.vanilla.crafting.VanillaRecipeValidator;
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

@JeiPlugin
public class VanillaPlugin implements IModPlugin {
	@Nullable
	private CraftingRecipeCategory craftingCategory;
	@Nullable
	private StoneCuttingRecipeCategory stonecuttingCategory;
	@Nullable
	private FurnaceSmeltingCategory furnaceCategory;
	@Nullable
	private SmokingCategory smokingCategory;
	@Nullable
	private BlastingCategory blastingCategory;
	@Nullable
	private CampfireCategory campfireCategory;

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
		registration.registerSubtypeInterpreter(Items.ENCHANTED_BOOK, itemStack -> {
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

		List<ItemStack> itemStacks = itemStackListFactory.create(stackHelper);
		ItemStackHelper itemStackHelper = new ItemStackHelper(stackHelper);
		ItemStackRenderer itemStackRenderer = new ItemStackRenderer();
		registration.register(VanillaTypes.ITEM, itemStacks, itemStackHelper, itemStackRenderer);

		List<FluidStack> fluidStacks = FluidStackListFactory.create();
		FluidStackHelper fluidStackHelper = new FluidStackHelper();
		FluidStackRenderer fluidStackRenderer = new FluidStackRenderer();
		registration.register(VanillaTypes.FLUID, fluidStacks, fluidStackHelper, fluidStackRenderer);
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		Textures textures = Internal.getTextures();
		IJeiHelpers jeiHelpers = registration.getJeiHelpers();
		IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
		IModIdHelper modIdHelper = jeiHelpers.getModIdHelper();
		craftingCategory = new CraftingRecipeCategory(guiHelper);
		stonecuttingCategory = new StoneCuttingRecipeCategory(guiHelper);
		furnaceCategory = new FurnaceSmeltingCategory(guiHelper);
		smokingCategory = new SmokingCategory(guiHelper);
		blastingCategory = new BlastingCategory(guiHelper);
		campfireCategory = new CampfireCategory(guiHelper);
		registration.addRecipeCategories(
			craftingCategory,
			stonecuttingCategory,
			furnaceCategory,
			smokingCategory,
			blastingCategory,
			campfireCategory,
			new FurnaceFuelCategory(guiHelper, textures),
			new BrewingRecipeCategory(guiHelper),
			new AnvilRecipeCategory(guiHelper)
		);
	}

	@Override
	public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
		IExtendableRecipeCategory<ICraftingRecipe, ICraftingCategoryExtension> craftingCategory = registration.getCraftingCategory();
		craftingCategory.addCategoryExtension(ICraftingRecipe.class, CraftingCategoryExtension::new);
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		ErrorUtil.checkNotNull(craftingCategory, "craftingCategory");
		ErrorUtil.checkNotNull(stonecuttingCategory, "stonecuttingCategory");
		ErrorUtil.checkNotNull(furnaceCategory, "furnaceCategory");
		ErrorUtil.checkNotNull(smokingCategory, "smokingCategory");
		ErrorUtil.checkNotNull(blastingCategory, "blastingCategory");
		ErrorUtil.checkNotNull(campfireCategory, "campfireCategory");

		IJeiHelpers jeiHelpers = registration.getJeiHelpers();
		IIngredientManager ingredientManager = registration.getIngredientManager();
		IVanillaRecipeFactory vanillaRecipeFactory = registration.getVanillaRecipeFactory();
		VanillaRecipeValidator.Results recipes = VanillaRecipeValidator.getValidRecipes(craftingCategory, stonecuttingCategory, furnaceCategory, smokingCategory, blastingCategory, campfireCategory);
		registration.addRecipes(recipes.getCraftingRecipes(), VanillaRecipeCategoryUid.CRAFTING);
		registration.addRecipes(recipes.getStonecuttingRecipes(), VanillaRecipeCategoryUid.STONECUTTING);
		registration.addRecipes(recipes.getFurnaceRecipes(), VanillaRecipeCategoryUid.FURNACE);
		registration.addRecipes(recipes.getSmokingRecipes(), VanillaRecipeCategoryUid.SMOKING);
		registration.addRecipes(recipes.getBlastingRecipes(), VanillaRecipeCategoryUid.BLASTING);
		registration.addRecipes(recipes.getCampfireRecipes(), VanillaRecipeCategoryUid.CAMPFIRE);
		registration.addRecipes(FuelRecipeMaker.getFuelRecipes(ingredientManager, jeiHelpers), VanillaRecipeCategoryUid.FUEL);
		registration.addRecipes(BrewingRecipeMaker.getBrewingRecipes(ingredientManager, vanillaRecipeFactory), VanillaRecipeCategoryUid.BREWING);
		registration.addRecipes(TippedArrowRecipeMaker.createTippedArrowRecipes(), VanillaRecipeCategoryUid.CRAFTING);
		registration.addRecipes(AnvilRecipeMaker.getAnvilRecipes(vanillaRecipeFactory, ingredientManager), VanillaRecipeCategoryUid.ANVIL);
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
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		registration.addRecipeCatalyst(new ItemStack(Blocks.CRAFTING_TABLE), VanillaRecipeCategoryUid.CRAFTING);
		registration.addRecipeCatalyst(new ItemStack(Blocks.STONECUTTER), VanillaRecipeCategoryUid.STONECUTTING);
		registration.addRecipeCatalyst(new ItemStack(Blocks.FURNACE), VanillaRecipeCategoryUid.FURNACE, VanillaRecipeCategoryUid.FUEL);
		registration.addRecipeCatalyst(new ItemStack(Blocks.SMOKER), VanillaRecipeCategoryUid.SMOKING, VanillaRecipeCategoryUid.FUEL);
		registration.addRecipeCatalyst(new ItemStack(Blocks.BLAST_FURNACE), VanillaRecipeCategoryUid.BLASTING, VanillaRecipeCategoryUid.FUEL);
		registration.addRecipeCatalyst(new ItemStack(Blocks.CAMPFIRE), VanillaRecipeCategoryUid.CAMPFIRE);
		registration.addRecipeCatalyst(new ItemStack(Blocks.BREWING_STAND), VanillaRecipeCategoryUid.BREWING);
		registration.addRecipeCatalyst(new ItemStack(Blocks.ANVIL), VanillaRecipeCategoryUid.ANVIL);
	}

	@Nullable
	public CraftingRecipeCategory getCraftingCategory() {
		return craftingCategory;
	}
}
