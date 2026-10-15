package mezz.jei.forge.tests.plugins.vanilla;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientSupplier;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.library.plugins.vanilla.VanillaRecipeFactory;
import mezz.jei.library.plugins.vanilla.cooking.FurnaceRecipeMaker;
import mezz.jei.library.plugins.vanilla.cooking.FurnaceRecipeTransferInfo;
import mezz.jei.library.plugins.vanilla.cooking.FurnaceSmeltingCategory;
import mezz.jei.library.plugins.vanilla.cooking.JeiSmeltingRecipe;
import mezz.jei.library.util.IngredientSupplierHelper;
import mezz.jei.library.util.RecipeDebugUtil;
import mezz.jei.forge.tests.lib.JeiGameTestHelper;
import mezz.jei.forge.tests.lib.TestGuiHelper;
import mezz.jei.forge.tests.lib.TestIngredientManagers;
import mezz.jei.forge.tests.lib.TestRecipeSlotView;
import mezz.jei.forge.tests.lib.TransferRecipe;
import mezz.jei.forge.tests.recipe.transfer.RecipeTransferTestHelper;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.SimpleContainer;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraft.world.item.crafting.SmeltingRecipe;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.AbstractList;
import mezz.jei.library.gui.recipes.supplier.builder.IngredientSupplierBuilder;
import mezz.jei.library.ingredients.RecipeIngredientSupplier;

@GameTestHolder("jei")
@PrefixGameTestTemplate(false)
public final class FurnaceRecipeGameTests {
	private FurnaceRecipeGameTests() {
	}

	@GameTest(template = "empty")
	public static void wetSpongeRecipeExtendsSmelting(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		VanillaRecipeFactory recipeFactory = TestIngredientManagers.createVanillaRecipeFactory();
		RecipeManager recipeManager = gameTestHelper.getLevel().getServer().getRecipeManager();
		ResourceLocation spongeRecipeId = new ResourceLocation("minecraft", "sponge");
		ResourceLocation spongeWithBucketRecipeId = new ResourceLocation("minecraft", "sponge_with_bucket");
		List<SmeltingRecipe> vanillaRecipes = recipeManager.getAllRecipesFor(RecipeType.SMELTING);

		List<SmeltingRecipe> recipes = FurnaceRecipeMaker.getRecipes(recipeFactory, recipeManager, gameTestHelper.getLevel().registryAccess());

		helper.assertEquals(
			1,
			vanillaRecipes.stream().filter(recipe -> recipe.getId().equals(spongeRecipeId)).toList().size(),
			"Vanilla sponge recipe count"
		);
		List<SmeltingRecipe> spongeRecipes = recipes.stream()
			.filter(recipe -> recipe.getId().equals(spongeWithBucketRecipeId))
			.toList();
		helper.assertEquals(1, spongeRecipes.size(), "Enhanced sponge recipe count");
		SmeltingRecipe recipeHolder = spongeRecipes.get(0);
		SmeltingRecipe recipe = recipeHolder;
		helper.assertEquals(RecipeType.SMELTING, recipe.getType(), "Recipe type");
		helper.assertTrue(recipe.getIngredients().get(0).test(new ItemStack(Items.WET_SPONGE)), "Expected a wet-sponge input");
		helper.assertSameStack(
			new ItemStack(Items.SPONGE),
			recipe.assemble(new SimpleContainer(new ItemStack(Items.WET_SPONGE)), gameTestHelper.getLevel().registryAccess()),
			"Smelting output"
		);
		helper.assertEquals(200, recipe.getCookingTime(), "Cooking time");
		helper.assertEquals(0.15f, recipe.getExperience(), "Experience");
		helper.assertEquals(spongeWithBucketRecipeId, recipeHolder.getId(), "Recipe ID");

		if (!(recipe instanceof JeiSmeltingRecipe jeiRecipe)) {
			throw helper.createFailException("Expected a JEI smelting recipe");
		}
		assertStacks(helper, List.of(new ItemStack(Items.BUCKET)), Arrays.asList(jeiRecipe.getFuel().getItems()), "Fuel-slot inputs");
		assertStacks(helper, List.of(new ItemStack(Items.WATER_BUCKET)), List.of(jeiRecipe.getFuelOutput()), "Fuel-slot outputs");

		IIngredientManager ingredientManager = TestIngredientManagers.createVanillaItemStackIngredientManager(gameTestHelper.getLevel());
		FurnaceSmeltingCategory recipeCategory = new FurnaceSmeltingCategory(TestGuiHelper.INSTANCE, List.of());
		helper.assertEquals(List.of(spongeRecipeId), recipeCategory.getSourceRecipeIds(recipeHolder), "Underlying sponge recipe ID");
		IIngredientSupplier ingredients = IngredientSupplierHelper.getIngredientSupplier(
			recipeHolder,
			recipeCategory,
			ingredientManager,
			Set.of(RecipeIngredientRole.INPUT, RecipeIngredientRole.OUTPUT, RecipeIngredientRole.CATALYST)
		);
		assertStacks(
			helper,
			List.of(new ItemStack(Items.WET_SPONGE), new ItemStack(Items.BUCKET)),
			getItemStacks(ingredients, RecipeIngredientRole.INPUT),
			"Indexed inputs"
		);
		assertStacks(
			helper,
			List.of(new ItemStack(Items.SPONGE), new ItemStack(Items.WATER_BUCKET)),
			getItemStacks(ingredients, RecipeIngredientRole.OUTPUT),
			"Indexed outputs"
		);

		FurnaceMenu menu = helper.openMenu(FurnaceMenu::new);
		FurnaceRecipeTransferInfo transferInfo = new FurnaceRecipeTransferInfo();
		helper.assertEquals(2, transferInfo.getRecipeSlots(menu, recipeHolder).size(), "Special smelting transfer slot count");

		TransferRecipe<SmeltingRecipe> transferRecipe = new TransferRecipe<>(
			recipeHolder,
			List.of(TestRecipeSlotView.item(Items.WET_SPONGE), TestRecipeSlotView.item(Items.BUCKET))
		);
		menu = helper.openMenu(FurnaceMenu::new, new ItemStack(Items.WET_SPONGE), new ItemStack(Items.BUCKET));
		var transferResult = helper.transfer(RecipeTypes.SMELTING, transferRecipe, menu);
		helper.assertSuccessfulTransfer(
			transferResult,
			(furnaceMenu, player) -> List.of(furnaceMenu.getSlot(0), furnaceMenu.getSlot(1))
		);

		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void smeltingRecipeDefaultsToAllFuels(GameTestHelper gameTestHelper) {
		JeiGameTestHelper helper = new JeiGameTestHelper(gameTestHelper);
		VanillaRecipeFactory recipeFactory = TestIngredientManagers.createVanillaRecipeFactory();
		SmeltingRecipe recipeHolder = recipeFactory.createSmeltingRecipe(
			Ingredient.of(Items.COBBLESTONE),
			new ItemStack(Items.STONE),
			200,
			0,
			new ResourceLocation("test", "stone")
		);

		if (!(recipeHolder instanceof JeiSmeltingRecipe recipe)) {
			throw helper.createFailException("Expected a JEI smelting recipe");
		}
		helper.assertTrue(recipe.getFuel().isEmpty(), "Expected the default all-fuels marker");
		helper.assertTrue(recipe.getFuelOutput().isEmpty(), "Expected no default fuel output");

		List<ItemStack> furnaceFuels = List.of(new ItemStack(Items.COAL), new ItemStack(Items.CHARCOAL));
		IIngredientManager ingredientManager = TestIngredientManagers.createVanillaItemStackIngredientManager(gameTestHelper.getLevel());
		FurnaceSmeltingCategory recipeCategory = new FurnaceSmeltingCategory(TestGuiHelper.INSTANCE, furnaceFuels);
		IIngredientSupplier ingredients = IngredientSupplierHelper.getIngredientSupplier(
			recipeHolder,
			recipeCategory,
			ingredientManager
		);
		assertStacks(
			helper,
			furnaceFuels,
			getItemStacks(ingredients, RecipeIngredientRole.RENDER_ONLY),
			"Default fuel-slot inputs"
		);

		IIngredientSupplier indexedIngredients = IngredientSupplierHelper.getIngredientSupplier(
			recipeHolder,
			recipeCategory,
			ingredientManager,
			Set.of(RecipeIngredientRole.INPUT, RecipeIngredientRole.OUTPUT, RecipeIngredientRole.CATALYST)
		);
		assertStacks(helper, List.of(new ItemStack(Items.COBBLESTONE)), getItemStacks(indexedIngredients, RecipeIngredientRole.INPUT), "Indexed inputs");
		assertStacks(helper, List.of(new ItemStack(Items.STONE)), getItemStacks(indexedIngredients, RecipeIngredientRole.OUTPUT), "Indexed outputs");
		helper.assertTrue(indexedIngredients.getIngredients(RecipeIngredientRole.RENDER_ONLY).isEmpty(), "Fuel must not be indexed");

		// Diagnostics must describe searchable ingredients without reading the full fuel list.
		List<ItemStack> unreadableFuels = new AbstractList<>() {
			@Override
			public ItemStack get(int index) {
				throw new AssertionError("Diagnostics must not read render-only fuels");
			}

			@Override
			public int size() {
				return 100_000;
			}
		};
		String diagnostic = RecipeDebugUtil.getDebugInfoFromRecipe(
			recipeHolder, new FurnaceSmeltingCategory(TestGuiHelper.INSTANCE, unreadableFuels), ingredientManager
		);
		String inputInfo = ingredientManager.getIngredientHelper(VanillaTypes.ITEM_STACK).getErrorInfo(new ItemStack(Items.COBBLESTONE));
		String outputInfo = ingredientManager.getIngredientHelper(VanillaTypes.ITEM_STACK).getErrorInfo(new ItemStack(Items.STONE));
		helper.assertTrue(diagnostic.contains(inputInfo), "Diagnostics must include inputs");
		helper.assertTrue(diagnostic.contains(outputInfo), "Diagnostics must include outputs");

		FurnaceMenu menu = helper.openMenu(FurnaceMenu::new);
		FurnaceRecipeTransferInfo transferInfo = new FurnaceRecipeTransferInfo();
		helper.assertEquals(1, transferInfo.getRecipeSlots(menu, recipeHolder).size(), "Normal smelting transfer slot count");

		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void ingredientCollectionSkipsUnselectedIngredientExpansion(GameTestHelper gameTestHelper) {
		JeiGameTestHelper helper = new JeiGameTestHelper(gameTestHelper);
		IIngredientManager ingredientManager = TestIngredientManagers.createVanillaItemStackIngredientManager(gameTestHelper.getLevel());
		IngredientSupplierBuilder builder = new IngredientSupplierBuilder(ingredientManager, Set.of(RecipeIngredientRole.INPUT));
		Ingredient fuelIngredient = new Ingredient(java.util.stream.Stream.empty()) {
			@Override
			public ItemStack[] getItems() {
				throw new AssertionError("Unselected ingredients must not be expanded");
			}
		};

		builder.addInputSlot().addIngredients(Ingredient.of(Items.STICK));
		builder.addSlot(RecipeIngredientRole.RENDER_ONLY).addIngredients(fuelIngredient);
		builder.addInvisibleIngredients(RecipeIngredientRole.RENDER_ONLY).addIngredients(fuelIngredient);
		RecipeIngredientSupplier ingredients = builder.buildIngredientSupplier();

		assertStacks(helper, List.of(new ItemStack(Items.STICK)), getItemStacks(ingredients, RecipeIngredientRole.INPUT), "Selected input");
		helper.assertTrue(ingredients.getIngredients(RecipeIngredientRole.RENDER_ONLY).isEmpty(), "Unselected role must be empty");
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void ingredientCollectionCapturesListsWhenAdded(GameTestHelper gameTestHelper) {
		JeiGameTestHelper helper = new JeiGameTestHelper(gameTestHelper);
		IIngredientManager ingredientManager = TestIngredientManagers.createVanillaItemStackIngredientManager(gameTestHelper.getLevel());
		IngredientSupplierBuilder builder = new IngredientSupplierBuilder(ingredientManager, Set.of(RecipeIngredientRole.INPUT));
		List<ItemStack> stacks = new ArrayList<>(List.of(new ItemStack(Items.STICK)));
		builder.addInputSlot().addItemStacks(stacks);
		stacks.add(new ItemStack(Items.STONE));
		RecipeIngredientSupplier ingredients = builder.buildIngredientSupplier();
		stacks.clear();

		assertStacks(helper, List.of(new ItemStack(Items.STICK)), getItemStacks(ingredients, RecipeIngredientRole.INPUT), "Collected input");
		helper.succeed();
	}

	private static List<ItemStack> getItemStacks(IIngredientSupplier ingredients, RecipeIngredientRole role) {
		return ingredients.getIngredients(role)
			.stream()
			.flatMap(ingredient -> ingredient.getItemStack().stream())
			.toList();
	}

	private static void assertStacks(JeiGameTestHelper helper, List<ItemStack> expected, List<ItemStack> actual, String message) {
		helper.assertEquals(expected.size(), actual.size(), message + " count");
		for (int i = 0; i < expected.size(); i++) {
			helper.assertSameStack(expected.get(i), actual.get(i), message + " at index " + i);
		}
	}
}
