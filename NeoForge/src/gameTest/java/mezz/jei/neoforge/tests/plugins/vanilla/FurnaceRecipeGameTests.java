package mezz.jei.neoforge.tests.plugins.vanilla;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.ingredients.IIngredientSupplier;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.library.plugins.vanilla.VanillaRecipeFactory;
import mezz.jei.library.plugins.vanilla.cooking.FurnaceRecipeMaker;
import mezz.jei.library.plugins.vanilla.cooking.FurnaceRecipeTransferInfo;
import mezz.jei.library.plugins.vanilla.cooking.FurnaceSmeltingCategory;
import mezz.jei.library.plugins.vanilla.cooking.JeiSmeltingRecipe;
import mezz.jei.library.util.IngredientSupplierHelper;
import mezz.jei.neoforge.tests.lib.JeiGameTestHelper;
import mezz.jei.neoforge.tests.lib.TestGuiHelper;
import mezz.jei.neoforge.tests.lib.TestIngredientManagers;
import mezz.jei.neoforge.tests.lib.TestRecipeSlotView;
import mezz.jei.neoforge.tests.lib.TransferRecipe;
import mezz.jei.neoforge.tests.recipe.transfer.RecipeTransferTestHelper;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

import java.util.Arrays;
import java.util.List;

@ForEachTest(groups = "furnace_recipes")
public final class FurnaceRecipeGameTests {
	private FurnaceRecipeGameTests() {
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "The wet-sponge recipe extends smelting with a bucket fuel-slot transformation.")
	public static void wetSpongeRecipeExtendsSmelting(RecipeTransferTestHelper helper) {
		VanillaRecipeFactory recipeFactory = TestIngredientManagers.createVanillaRecipeFactory();
		RecipeManager recipeManager = helper.getLevel().getServer().getRecipeManager();
		ResourceLocation spongeRecipeId = ResourceLocation.withDefaultNamespace("sponge");
		ResourceLocation spongeWithBucketRecipeId = ResourceLocation.withDefaultNamespace("sponge_with_bucket");
		List<RecipeHolder<SmeltingRecipe>> vanillaRecipes = recipeManager.getAllRecipesFor(RecipeType.SMELTING);

		List<RecipeHolder<SmeltingRecipe>> recipes = FurnaceRecipeMaker.getRecipes(recipeFactory, recipeManager, helper.getLevel().registryAccess());

		helper.assertEquals(
			1,
			vanillaRecipes.stream().filter(recipe -> recipe.id().equals(spongeRecipeId)).toList().size(),
			"Vanilla sponge recipe count"
		);
		List<RecipeHolder<SmeltingRecipe>> spongeRecipes = recipes.stream()
			.filter(recipe -> recipe.id().equals(spongeWithBucketRecipeId))
			.toList();
		helper.assertEquals(1, spongeRecipes.size(), "Enhanced sponge recipe count");
		RecipeHolder<SmeltingRecipe> recipeHolder = spongeRecipes.getFirst();
		SmeltingRecipe recipe = recipeHolder.value();
		helper.assertEquals(RecipeType.SMELTING, recipe.getType(), "Recipe type");
		helper.assertTrue(recipe.getIngredients().getFirst().test(new ItemStack(Items.WET_SPONGE)), "Expected a wet-sponge input");
		helper.assertSameStack(
			new ItemStack(Items.SPONGE),
			recipe.assemble(new SingleRecipeInput(new ItemStack(Items.WET_SPONGE)), helper.getLevel().registryAccess()),
			"Smelting output"
		);
		helper.assertEquals(200, recipe.getCookingTime(), "Cooking time");
		helper.assertEquals(0.15f, recipe.getExperience(), "Experience");
		helper.assertEquals(spongeWithBucketRecipeId, recipeHolder.id(), "Recipe ID");

		if (!(recipe instanceof JeiSmeltingRecipe jeiRecipe)) {
			throw helper.createFailException("Expected a JEI smelting recipe");
		}
		assertStacks(helper, List.of(new ItemStack(Items.BUCKET)), Arrays.asList(jeiRecipe.getFuel().getItems()), "Fuel-slot inputs");
		assertStacks(helper, List.of(new ItemStack(Items.WATER_BUCKET)), List.of(jeiRecipe.getFuelOutput()), "Fuel-slot outputs");

		IIngredientManager ingredientManager = TestIngredientManagers.createVanillaItemStackIngredientManager(helper.getLevel());
		FurnaceSmeltingCategory recipeCategory = new FurnaceSmeltingCategory(TestGuiHelper.INSTANCE);
		IIngredientSupplier ingredients = IngredientSupplierHelper.getIngredientSupplier(
			recipeHolder,
			recipeCategory,
			ingredientManager
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
		VanillaRecipeFactory recipeFactory = TestIngredientManagers.createVanillaRecipeFactory();
		RecipeHolder<SmeltingRecipe> recipeHolder = recipeFactory.createSmeltingRecipe(
			Ingredient.of(Items.COBBLESTONE),
			new ItemStack(Items.STONE),
			200,
			0,
			ResourceLocation.fromNamespaceAndPath("test", "stone")
		);

		if (!(recipeHolder.value() instanceof JeiSmeltingRecipe recipe)) {
			throw helper.createFailException("Expected a JEI smelting recipe");
		}
		helper.assertTrue(recipe.getFuel().isEmpty(), "Expected the default all-fuels marker");
		helper.assertTrue(recipe.getFuelOutput().isEmpty(), "Expected no default fuel output");

		List<ItemStack> furnaceFuels = List.of(new ItemStack(Items.COAL), new ItemStack(Items.CHARCOAL));
		IIngredientManager ingredientManager = TestIngredientManagers.createVanillaItemStackIngredientManager(helper.getLevel());
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

		FurnaceMenu menu = helper.openMenu(FurnaceMenu::new);
		FurnaceRecipeTransferInfo transferInfo = new FurnaceRecipeTransferInfo();
		helper.assertEquals(1, transferInfo.getRecipeSlots(menu, recipeHolder).size(), "Normal smelting transfer slot count");

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
