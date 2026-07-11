package mezz.jei.neoforge.tests.plugins.vanilla;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.library.plugins.vanilla.VanillaRecipeFactory;
import mezz.jei.neoforge.platform.BrewingRecipeMaker;
import mezz.jei.neoforge.tests.lib.JeiGameTestHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.brewing.BrewingRecipe;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@ForEachTest(groups = "synthetic_recipes")
public final class BrewingRecipeMakerGameTests {
	private static final TestItemStackHelper ITEM_STACK_HELPER = new TestItemStackHelper();

	private BrewingRecipeMakerGameTests() {
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Valid NeoForge brewing recipes are added to JEI's recipe list.")
	public static void addModdedBrewingRecipesAddsValidRecipe(JeiGameTestHelper helper) {
		// Setup: a NeoForge brewing recipe has concrete input, reagent, and output stacks.
		BrewingRecipe brewingRecipe = new BrewingRecipe(
			Ingredient.of(Items.POTION),
			Ingredient.of(Items.NETHER_WART),
			new ItemStack(Items.DIAMOND)
		);
		List<IJeiBrewingRecipe> recipes = new ArrayList<>();

		// Operation: JEI converts the NeoForge recipe into its internal brewing recipe.
		addModdedBrewingRecipes(List.of(brewingRecipe), recipes);

		// Assertions: the converted recipe exposes the reagent, input, output, and generated uid.
		helper.assertEquals(1, recipes.size(), "Expected one converted brewing recipe");
		IJeiBrewingRecipe recipe = recipes.getFirst();
		assertStacksEqual(helper, List.of(new ItemStack(Items.NETHER_WART)), recipe.getIngredients());
		assertStacksEqual(helper, List.of(new ItemStack(Items.POTION)), recipe.getPotionInputs());
		helper.assertTrue(
			ItemStack.isSameItemSameComponents(new ItemStack(Items.DIAMOND), recipe.getPotionOutput()),
			"Expected converted brewing recipe output"
		);
		helper.assertEquals("minecraft", recipe.getUid().getNamespace(), "Expected output namespace to drive generated uid");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "NeoForge brewing recipes with no reagent are skipped.")
	public static void addModdedBrewingRecipesSkipsEmptyIngredient(JeiGameTestHelper helper) {
		// Setup: the brewing recipe has no reagent stacks to display.
		BrewingRecipe brewingRecipe = new BrewingRecipe(
			Ingredient.of(Items.POTION),
			emptyIngredient(),
			new ItemStack(Items.DIAMOND)
		);
		List<IJeiBrewingRecipe> recipes = new ArrayList<>();

		// Operation: JEI converts the NeoForge recipe collection.
		addModdedBrewingRecipes(List.of(brewingRecipe), recipes);

		// Assertions: invalid recipes with no reagent are skipped.
		helper.assertEquals(List.of(), recipes, "Expected no recipes for empty brewing reagent");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "NeoForge brewing recipes with no potion input are skipped.")
	public static void addModdedBrewingRecipesSkipsEmptyInput(JeiGameTestHelper helper) {
		// Setup: the brewing recipe has no input potion stacks to display.
		BrewingRecipe brewingRecipe = new BrewingRecipe(
			emptyIngredient(),
			Ingredient.of(Items.NETHER_WART),
			new ItemStack(Items.DIAMOND)
		);
		List<IJeiBrewingRecipe> recipes = new ArrayList<>();

		// Operation: JEI converts the NeoForge recipe collection.
		addModdedBrewingRecipes(List.of(brewingRecipe), recipes);

		// Assertions: invalid recipes with no input are skipped.
		helper.assertEquals(List.of(), recipes, "Expected no recipes for empty brewing input");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "NeoForge brewing recipes with no output are skipped.")
	public static void addModdedBrewingRecipesSkipsEmptyOutput(JeiGameTestHelper helper) {
		// Setup: the brewing recipe has an empty output stack.
		BrewingRecipe brewingRecipe = new BrewingRecipe(
			Ingredient.of(Items.POTION),
			Ingredient.of(Items.NETHER_WART),
			ItemStack.EMPTY
		);
		List<IJeiBrewingRecipe> recipes = new ArrayList<>();

		// Operation: JEI converts the NeoForge recipe collection.
		addModdedBrewingRecipes(List.of(brewingRecipe), recipes);

		// Assertions: invalid recipes with no output are skipped.
		helper.assertEquals(List.of(), recipes, "Expected no recipes for empty brewing output");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Unsupported NeoForge brewing recipe implementations are skipped.")
	public static void addModdedBrewingRecipesSkipsUnsupportedRecipeClass(JeiGameTestHelper helper) {
		// Setup: a mod registers a custom brewing recipe implementation that JEI does not know how to inspect.
		IBrewingRecipe brewingRecipe = new UnsupportedBrewingRecipe();
		List<IJeiBrewingRecipe> recipes = new ArrayList<>();

		// Operation: JEI converts the NeoForge recipe collection.
		addModdedBrewingRecipes(List.of(brewingRecipe, brewingRecipe), recipes);

		// Assertions: unsupported recipe implementations are ignored without failing the whole collection.
		helper.assertEquals(List.of(), recipes, "Expected unsupported brewing recipe implementations to be skipped");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Valid NeoForge brewing recipes are kept when invalid recipes are skipped.")
	public static void addModdedBrewingRecipesKeepsExistingRecipesWhenSkippingInvalidOnes(JeiGameTestHelper helper) {
		// Setup: the target collection already contains a recipe, and the source collection has one invalid
		// recipe followed by one valid recipe.
		IJeiBrewingRecipe existingRecipe = createRecipeFactory().createBrewingRecipe(
			List.of(new ItemStack(Items.REDSTONE)),
			new ItemStack(Items.POTION),
			new ItemStack(Items.EMERALD),
			ResourceLocation.fromNamespaceAndPath("test", "existing")
		);
		BrewingRecipe invalidRecipe = new BrewingRecipe(
			Ingredient.of(Items.POTION),
			Ingredient.of(Items.NETHER_WART),
			ItemStack.EMPTY
		);
		BrewingRecipe validRecipe = new BrewingRecipe(
			Ingredient.of(Items.POTION),
			Ingredient.of(Items.GLOWSTONE_DUST),
			new ItemStack(Items.GOLD_INGOT)
		);
		Collection<IJeiBrewingRecipe> recipes = new ArrayList<>(List.of(existingRecipe));

		// Operation: JEI converts the mixed NeoForge recipe collection.
		addModdedBrewingRecipes(List.of(invalidRecipe, validRecipe), recipes);

		// Assertions: the existing recipe remains, the invalid one is skipped, and the valid one is added.
		helper.assertEquals(2, recipes.size(), "Expected existing and valid brewing recipes");
		Set<ResourceLocation> uids = recipes.stream()
			.map(IJeiBrewingRecipe::getUid)
			.collect(java.util.stream.Collectors.toSet());
		helper.assertTrue(uids.contains(existingRecipe.getUid()), "Expected existing brewing recipe to remain");
		helper.assertTrue(
			recipes.stream().anyMatch(recipe -> ItemStack.isSameItemSameComponents(new ItemStack(Items.GOLD_INGOT), recipe.getPotionOutput())),
			"Expected valid modded brewing recipe to be added"
		);
		helper.succeed();
	}

	private static void addModdedBrewingRecipes(Collection<IBrewingRecipe> brewingRecipes, Collection<IJeiBrewingRecipe> recipes) {
		BrewingRecipeMaker.addModdedBrewingRecipes(
			createRecipeFactory(),
			ITEM_STACK_HELPER,
			brewingRecipes,
			recipes
		);
	}

	private static VanillaRecipeFactory createRecipeFactory() {
		return new VanillaRecipeFactory(ITEM_STACK_HELPER);
	}

	private static Ingredient emptyIngredient() {
		return new EmptyCustomIngredient().toVanilla();
	}

	private static void assertStacksEqual(JeiGameTestHelper helper, List<ItemStack> expected, List<ItemStack> actual) {
		helper.assertEquals(expected.size(), actual.size(), "Expected stack list size");
		for (int i = 0; i < expected.size(); i++) {
			int index = i;
			ItemStack expectedStack = expected.get(i);
			ItemStack actualStack = actual.get(i);
			helper.assertTrue(
				ItemStack.isSameItemSameComponents(expectedStack, actualStack),
				"Expected stack " + expectedStack + " at index " + index + " but got " + actualStack
			);
		}
	}

	private static class UnsupportedBrewingRecipe implements IBrewingRecipe {
		@Override
		public boolean isInput(ItemStack input) {
			return true;
		}

		@Override
		public boolean isIngredient(ItemStack ingredient) {
			return true;
		}

		@Override
		public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
			return new ItemStack(Items.DIAMOND);
		}
	}

	private static class EmptyCustomIngredient implements ICustomIngredient {
		@Override
		public boolean test(ItemStack stack) {
			return false;
		}

		@Override
		public Stream<ItemStack> getItems() {
			return Stream.empty();
		}

		@Override
		public boolean isSimple() {
			return true;
		}

		@Override
		public IngredientType<?> getType() {
			throw new UnsupportedOperationException("Test ingredient is never serialized");
		}

		@Override
		public boolean equals(Object object) {
			return object instanceof EmptyCustomIngredient;
		}

		@Override
		public int hashCode() {
			return EmptyCustomIngredient.class.hashCode();
		}
	}

	private static class TestItemStackHelper implements IIngredientHelper<ItemStack> {
		@Override
		public IIngredientType<ItemStack> getIngredientType() {
			return VanillaTypes.ITEM_STACK;
		}

		@Override
		public String getDisplayName(ItemStack ingredient) {
			return ingredient.getHoverName().getString();
		}

		@Override
		public String getUniqueId(ItemStack ingredient, UidContext context) {
			return getResourceLocation(ingredient).toString();
		}

		@Override
		public Object getUid(ItemStack ingredient, UidContext context) {
			return BuiltInRegistries.ITEM.getKey(ingredient.getItem());
		}

		@Override
		public ResourceLocation getResourceLocation(ItemStack ingredient) {
			return BuiltInRegistries.ITEM.getKey(ingredient.getItem());
		}

		@Override
		public ItemStack copyIngredient(ItemStack ingredient) {
			return ingredient.copy();
		}

		@Override
		public String getErrorInfo(@Nullable ItemStack ingredient) {
			return String.valueOf(ingredient);
		}
	}
}
