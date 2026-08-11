package mezz.jei.neoforge.tests.plugins.vanilla;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.ingredients.IIngredientSupplier;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.library.ingredients.IIngredientManagerInternal;
import mezz.jei.library.plugins.vanilla.VanillaRecipeFactory;
import mezz.jei.library.plugins.vanilla.cooking.FurnaceRecipeMaker;
import mezz.jei.library.plugins.vanilla.cooking.FurnaceRecipeTransferInfo;
import mezz.jei.library.plugins.vanilla.cooking.FurnaceSmeltingCategory;
import mezz.jei.library.util.IngredientSupplierHelper;
import mezz.jei.neoforge.tests.lib.JeiGameTestHelper;
import mezz.jei.neoforge.tests.lib.TestGuiHelper;
import mezz.jei.neoforge.tests.lib.TestIngredientManagers;
import mezz.jei.neoforge.tests.lib.TestRecipeSlotView;
import mezz.jei.neoforge.tests.lib.TransferRecipe;
import mezz.jei.neoforge.tests.recipe.transfer.RecipeTransferTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;

import java.util.List;

@ForEachTest(groups = "furnace_recipes")
public final class FurnaceRecipeGameTests {
	private FurnaceRecipeGameTests() {
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "The wet-sponge recipe extends smelting with a bucket fuel-slot transformation.")
	public static void wetSpongeRecipeExtendsSmelting(RecipeTransferTestHelper helper) {
		VanillaRecipeFactory recipeFactory = TestIngredientManagers.createVanillaRecipeFactory(ContextMap.EMPTY);
		RecipeMap recipeMap = helper.getLevel().getServer().getRecipeManager().recipeMap();
		Identifier spongeRecipeId = Identifier.withDefaultNamespace("sponge");
		Identifier spongeWithBucketRecipeId = Identifier.withDefaultNamespace("sponge_with_bucket");
		List<RecipeHolder<SmeltingRecipe>> vanillaRecipes = recipeMap.byType(RecipeType.SMELTING)
			.stream()
			.toList();

		List<RecipeHolder<SmeltingRecipe>> recipes = FurnaceRecipeMaker.getRecipes(recipeFactory, recipeMap);

		helper.assertEquals(
			1,
			vanillaRecipes.stream().filter(recipe -> recipe.id().identifier().equals(spongeRecipeId)).toList().size(),
			"Vanilla sponge recipe count"
		);
		List<RecipeHolder<SmeltingRecipe>> spongeRecipes = recipes.stream()
			.filter(recipe -> recipe.id().identifier().equals(spongeWithBucketRecipeId))
			.toList();
		helper.assertEquals(1, spongeRecipes.size(), "Enhanced sponge recipe count");
		RecipeHolder<SmeltingRecipe> recipeHolder = spongeRecipes.getFirst();
		SmeltingRecipe recipe = recipeHolder.value();
		helper.assertEquals(RecipeType.SMELTING, recipe.getType(), "Recipe type");
		helper.assertTrue(recipe.input().test(new ItemStack(Items.WET_SPONGE)), "Expected a wet-sponge input");
		helper.assertSameStack(new ItemStack(Items.SPONGE), recipe.assemble(new SingleRecipeInput(new ItemStack(Items.WET_SPONGE))), "Smelting output");
		helper.assertEquals(200, recipe.cookingTime(), "Cooking time");
		helper.assertEquals(0.15f, recipe.experience(), "Experience");
		helper.assertEquals(spongeWithBucketRecipeId, recipeHolder.id().identifier(), "Recipe ID");

		FurnaceRecipeDisplay display = getFurnaceDisplay(helper, recipe);
		if (!(display.fuel() instanceof SlotDisplay.WithRemainder fuel)) {
			throw helper.createFailException("Expected a fuel display with a remainder");
		}
		assertStacks(helper, List.of(new ItemStack(Items.BUCKET)), fuel.input().resolveForStacks(ContextMap.EMPTY), "Fuel-slot inputs");
		assertStacks(helper, List.of(new ItemStack(Items.WATER_BUCKET)), fuel.remainder().resolveForStacks(ContextMap.EMPTY), "Fuel-slot outputs");

		IIngredientManager ingredientManager = TestIngredientManagers.createVanillaItemStackIngredientManager(helper.getLevel());
		FurnaceSmeltingCategory recipeCategory = new FurnaceSmeltingCategory(TestGuiHelper.INSTANCE);
		IIngredientSupplier ingredients = IngredientSupplierHelper.getIngredientSupplier(
			recipeHolder,
			recipeCategory,
			ingredientManager,
			ContextMap.EMPTY
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

		TransferRecipe<RecipeHolder<SmeltingRecipe>> transferRecipe = new TransferRecipe<>(
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

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "API-created smelting recipes use all furnace fuels by default.")
	public static void smeltingRecipeDefaultsToAllFuels(JeiGameTestHelper helper) {
		VanillaRecipeFactory recipeFactory = TestIngredientManagers.createVanillaRecipeFactory(ContextMap.EMPTY);
		RecipeHolder<SmeltingRecipe> recipeHolder = recipeFactory.createSmeltingRecipe(
			Ingredient.of(Items.COBBLESTONE),
			new ItemStack(Items.STONE),
			200,
			0,
			Identifier.fromNamespaceAndPath("test", "stone")
		);

		FurnaceRecipeDisplay display = getFurnaceDisplay(helper, recipeHolder.value());
		helper.assertTrue(display.fuel() instanceof SlotDisplay.AnyFuel, "Expected the default all-fuels display");

		FurnaceMenu menu = helper.openMenu(FurnaceMenu::new);
		FurnaceRecipeTransferInfo transferInfo = new FurnaceRecipeTransferInfo();
		helper.assertEquals(1, transferInfo.getRecipeSlots(menu, recipeHolder).size(), "Normal smelting transfer slot count");

		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "The wet-sponge furnace recipe is omitted when its vanilla smelting recipe is unavailable.")
	public static void wetSpongeRecipeRequiresVanillaSmeltingRecipe(JeiGameTestHelper helper) {
		VanillaRecipeFactory recipeFactory = TestIngredientManagers.createVanillaRecipeFactory(ContextMap.EMPTY);

		List<RecipeHolder<SmeltingRecipe>> recipes = FurnaceRecipeMaker.getRecipes(recipeFactory, RecipeMap.EMPTY);

		helper.assertTrue(recipes.isEmpty(), "Expected no special furnace recipes without the vanilla sponge recipe");
		helper.succeed();
	}

	private static FurnaceRecipeDisplay getFurnaceDisplay(JeiGameTestHelper helper, SmeltingRecipe recipe) {
		RecipeDisplay display = recipe.display().getFirst();
		if (display instanceof FurnaceRecipeDisplay furnaceRecipeDisplay) {
			return furnaceRecipeDisplay;
		}
		throw helper.createFailException("Expected a furnace recipe display");
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
