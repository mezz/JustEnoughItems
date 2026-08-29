package mezz.jei.neoforge.tests.plugins.vanilla;

import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import mezz.jei.api.recipe.vanilla.IJeiGrindstoneRecipe;
import mezz.jei.common.platform.IPlatformIngredientHelper;
import mezz.jei.common.platform.IPlatformRecipeHelper;
import mezz.jei.common.util.ImmutableSize2i;
import mezz.jei.common.util.RegistryUtil;
import mezz.jei.library.gui.helpers.CraftingGridHelper;
import mezz.jei.library.plugins.vanilla.anvil.AnvilRecipeMaker;
import mezz.jei.library.plugins.vanilla.crafting.CraftingCategoryExtension;
import mezz.jei.library.plugins.vanilla.crafting.CraftingRecipeCategory;
import mezz.jei.library.plugins.vanilla.crafting.replacers.ShieldDecorationRecipeMaker;
import mezz.jei.library.plugins.vanilla.crafting.replacers.TippedArrowRecipeMaker;
import mezz.jei.library.plugins.vanilla.grindstone.GrindstoneRecipeMaker;
import mezz.jei.neoforge.tests.lib.JeiGameTestHelper;
import mezz.jei.neoforge.tests.lib.TestGuiHelper;
import mezz.jei.neoforge.tests.lib.TestIngredientManagers;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ForEachTest(groups = "synthetic_recipes")
public final class SyntheticRecipeMakerGameTests {
	private SyntheticRecipeMakerGameTests() {
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Every registry-backed tipped arrow has a JEI recipe that crafts in a real crafting table.")
	public static void tippedArrowRecipesCraftExpectedRegistryOutputs(JeiGameTestHelper helper) {
		List<ItemStack> expectedOutputs = createExpectedTippedArrowOutputs();
		List<RecipeHolder<CraftingRecipe>> jeiRecipes = createTippedArrowRecipes();

		assertJeiRecipesCraftExpectedOutputs(helper, expectedOutputs, jeiRecipes);

		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Every banner color has a shield-decoration JEI recipe that crafts in a real crafting table.")
	public static void shieldDecorationRecipesCraftExpectedRegistryOutputs(JeiGameTestHelper helper) {
		List<ItemStack> expectedOutputs = createExpectedShieldDecorationOutputs();
		List<RecipeHolder<CraftingRecipe>> jeiRecipes = createShieldDecorationRecipes();

		assertJeiRecipesCraftExpectedOutputs(helper, expectedOutputs, jeiRecipes);

		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Generated JEI anvil recipes produce their displayed outputs in a real anvil menu.")
	public static void anvilRecipesProduceDisplayedOutputs(JeiGameTestHelper helper) {
		List<IJeiAnvilRecipe> recipes = createAnvilRecipes(helper);

		helper.assertTrue(!recipes.isEmpty(), "Generated JEI anvil recipes should not be empty");
		for (IJeiAnvilRecipe recipe : recipes) {
			assertAnvilRecipeProducesDisplayedOutput(helper, recipe);
		}

		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Generated JEI grindstone recipes produce their displayed outputs in a real grindstone menu.")
	public static void grindstoneRecipesProduceDisplayedOutputs(JeiGameTestHelper helper) {
		List<IJeiGrindstoneRecipe> recipes = createGrindstoneRecipes(helper);

		helper.assertTrue(!recipes.isEmpty(), "Generated JEI grindstone recipes should not be empty");
		for (IJeiGrindstoneRecipe recipe : recipes) {
			assertGrindstoneRecipeProducesDisplayedOutput(helper, recipe);
		}

		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Generated JEI grindstone recipes skip enchantment-item pairs whose compatibility checks crash.")
	public static void grindstoneRecipesSkipBrokenEnchantabilityChecks(JeiGameTestHelper helper) {
		ThrowingEnchantabilityRecipeHelper recipeHelper = new ThrowingEnchantabilityRecipeHelper(TestRecipeHelper.INSTANCE);
		GrindstoneMenu grindstoneMenu = createGrindstoneMenu(helper);
		List<IJeiGrindstoneRecipe> recipes = GrindstoneRecipeMaker.getGrindstoneRecipes(
			TestIngredientManagers.createVanillaItemStackIngredientManager(helper.getLevel()),
			recipeHelper,
			TestIngredientHelper.INSTANCE,
			grindstoneMenu
		);

		helper.assertTrue(recipeHelper.hasThrown(), "Expected an enchantability check to throw");
		helper.assertTrue(!recipes.isEmpty(), "Generated JEI grindstone recipes should continue after an enchantability check throws");

		helper.succeed();
	}

	private static List<RecipeHolder<CraftingRecipe>> createTippedArrowRecipes() {
		return TippedArrowRecipeMaker.createRecipes(TestIngredientManagers.createJeiHelpers());
	}

	private static List<ItemStack> createExpectedTippedArrowOutputs() {
		Registry<Potion> potionRegistry = RegistryUtil.getRegistry(Registries.POTION);
		return potionRegistry.holders()
			.map(SyntheticRecipeMakerGameTests::createTippedArrowOutput)
			.toList();
	}

	private static ItemStack createTippedArrowOutput(Holder<Potion> potion) {
		ItemStack stack = new ItemStack(Items.TIPPED_ARROW, 8);
		stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
		return stack;
	}

	private static List<RecipeHolder<CraftingRecipe>> createShieldDecorationRecipes() {
		return ShieldDecorationRecipeMaker.createRecipes();
	}

	private static List<ItemStack> createExpectedShieldDecorationOutputs() {
		Registry<Item> itemRegistry = RegistryUtil.getRegistry(Registries.ITEM);
		List<ItemStack> outputs = new ArrayList<>();
		for (Holder<Item> itemHolder : itemRegistry.getTagOrEmpty(ItemTags.BANNERS)) {
			BannerItem banner = (BannerItem) itemHolder.value();
			outputs.add(createDecoratedShieldOutput(banner));
		}
		return outputs;
	}

	private static ItemStack createDecoratedShieldOutput(BannerItem banner) {
		ItemStack stack = new ItemStack(Items.SHIELD);
		stack.set(DataComponents.BASE_COLOR, banner.getColor());
		return stack;
	}

	private static List<IJeiAnvilRecipe> createAnvilRecipes(JeiGameTestHelper helper) {
		AnvilMenu anvilMenu = createAnvilMenu(helper);
		return AnvilRecipeMaker.getAnvilRecipes(
			TestIngredientManagers.createVanillaRecipeFactory(),
			TestIngredientManagers.createVanillaItemStackIngredientManager(helper.getLevel()),
			anvilMenu
		);
	}

	private static List<IJeiGrindstoneRecipe> createGrindstoneRecipes(JeiGameTestHelper helper) {
		GrindstoneMenu grindstoneMenu = createGrindstoneMenu(helper);
		return GrindstoneRecipeMaker.getGrindstoneRecipes(
			TestIngredientManagers.createVanillaItemStackIngredientManager(helper.getLevel()),
			TestRecipeHelper.INSTANCE,
			TestIngredientHelper.INSTANCE,
			grindstoneMenu
		);
	}

	private static void assertAnvilRecipeProducesDisplayedOutput(JeiGameTestHelper helper, IJeiAnvilRecipe recipe) {
		forEachRecipeVariation(
			helper,
			recipe.getLeftInputs(),
			recipe.getRightInputs(),
			recipe.getOutputs(),
			(leftInput, rightInput, output) -> {
				AnvilResult actual = getAnvilResult(helper, leftInput, rightInput);
				String description = describeAnvilRecipe(recipe, leftInput, rightInput);
				helper.assertTrue(!actual.output().isEmpty(), "Anvil recipe produced an empty output: " + description);
				helper.assertTrue(actual.levelCost() > 0, "Anvil recipe should have a positive level cost: " + description);
				helper.assertSameStack(output, actual.output(), "Anvil recipe should craft its displayed output: " + description);
			},
			"Anvil recipe has unsupported input/output counts: " + describeAnvilRecipe(recipe)
		);
	}

	private static void assertGrindstoneRecipeProducesDisplayedOutput(JeiGameTestHelper helper, IJeiGrindstoneRecipe recipe) {
		forEachRecipeVariation(
			helper,
			recipe.getTopInputs(),
			recipe.getBottomInputs(),
			recipe.getOutputs(),
			(topInput, bottomInput, output) -> {
				ItemStack actualOutput = getGrindstoneResult(helper, topInput, bottomInput);
				String description = describeGrindstoneRecipe(recipe, topInput, bottomInput);
				helper.assertTrue(!actualOutput.isEmpty(), "Grindstone recipe produced an empty output: " + description);
				helper.assertSameStack(output, actualOutput, "Grindstone recipe should craft its displayed output: " + description);
			},
			"Grindstone recipe has unsupported input/output counts: " + describeGrindstoneRecipe(recipe)
		);
	}

	private static void forEachRecipeVariation(
		JeiGameTestHelper helper,
		List<ItemStack> leftInputs,
		List<ItemStack> rightInputs,
		List<ItemStack> outputs,
		RecipeVariationConsumer consumer,
		String unsupportedCountsMessage
	) {
		helper.assertTrue(!leftInputs.isEmpty(), "Recipe should have at least one left input");
		helper.assertTrue(!rightInputs.isEmpty(), "Recipe should have at least one right input");
		helper.assertTrue(!outputs.isEmpty(), "Recipe should have at least one output");

		if (leftInputs.size() == rightInputs.size() && leftInputs.size() == outputs.size()) {
			for (int i = 0; i < outputs.size(); i++) {
				consumer.accept(leftInputs.get(i), rightInputs.get(i), outputs.get(i));
			}
			return;
		}
		if (leftInputs.size() == 1 && rightInputs.size() == outputs.size()) {
			ItemStack leftInput = leftInputs.getFirst();
			for (int i = 0; i < outputs.size(); i++) {
				consumer.accept(leftInput, rightInputs.get(i), outputs.get(i));
			}
			return;
		}
		if (rightInputs.size() == 1 && leftInputs.size() == outputs.size()) {
			ItemStack rightInput = rightInputs.getFirst();
			for (int i = 0; i < outputs.size(); i++) {
				consumer.accept(leftInputs.get(i), rightInput, outputs.get(i));
			}
			return;
		}
		if (leftInputs.size() == 1 && outputs.size() == 1) {
			ItemStack leftInput = leftInputs.getFirst();
			ItemStack output = outputs.getFirst();
			for (ItemStack rightInput : rightInputs) {
				consumer.accept(leftInput, rightInput, output);
			}
			return;
		}
		if (rightInputs.size() == 1 && outputs.size() == 1) {
			ItemStack rightInput = rightInputs.getFirst();
			ItemStack output = outputs.getFirst();
			for (ItemStack leftInput : leftInputs) {
				consumer.accept(leftInput, rightInput, output);
			}
			return;
		}

		throw helper.createFailException(unsupportedCountsMessage);
	}

	private static AnvilResult getAnvilResult(JeiGameTestHelper helper, ItemStack leftInput, ItemStack rightInput) {
		AnvilMenu menu = createAnvilMenu(helper);
		menu.getSlot(AnvilMenu.INPUT_SLOT).set(leftInput.copy());
		menu.getSlot(AnvilMenu.ADDITIONAL_SLOT).set(rightInput.copy());
		return new AnvilResult(menu.getSlot(AnvilMenu.RESULT_SLOT).getItem().copy(), menu.getCost());
	}

	private static ItemStack getGrindstoneResult(JeiGameTestHelper helper, ItemStack topInput, ItemStack bottomInput) {
		GrindstoneMenu menu = createGrindstoneMenu(helper);
		menu.getSlot(GrindstoneMenu.INPUT_SLOT).set(topInput.copy());
		menu.getSlot(GrindstoneMenu.ADDITIONAL_SLOT).set(bottomInput.copy());
		return menu.getSlot(GrindstoneMenu.RESULT_SLOT).getItem().copy();
	}

	private static AnvilMenu createAnvilMenu(JeiGameTestHelper helper) {
		return new AnvilMenu(0, helper.getPlayer().getInventory());
	}

	private static GrindstoneMenu createGrindstoneMenu(JeiGameTestHelper helper) {
		return new GrindstoneMenu(0, helper.getPlayer().getInventory());
	}

	private static void assertJeiRecipesCraftExpectedOutputs(
		JeiGameTestHelper helper,
		List<ItemStack> expectedOutputs,
		List<RecipeHolder<CraftingRecipe>> jeiRecipes
	) {
		List<ItemStack> missingOutputs = expectedOutputs.stream()
			.map(ItemStack::copy)
			.collect(Collectors.toCollection(ArrayList::new));
		CraftingRecipeCategory craftingCategory = createCraftingCategory();

		helper.assertEquals(expectedOutputs.size(), jeiRecipes.size(), "Generated JEI recipe count");

		for (RecipeHolder<CraftingRecipe> jeiRecipe : jeiRecipes) {
			JeiCraftingRecipeIngredients recipeIngredients = getJeiCraftingRecipeIngredients(helper, craftingCategory, jeiRecipe);
			ItemStack actualOutput = helper.craftInCraftingTable(recipeIngredients.inputs());

			helper.assertSameStack(recipeIngredients.output(), actualOutput, "JEI recipe should craft its displayed output");
			removeExpectedOutput(helper, missingOutputs, actualOutput);
		}

		helper.assertTrue(missingOutputs.isEmpty(), "Missing JEI recipes for outputs: " + describeStacks(missingOutputs));
	}

	private static CraftingRecipeCategory createCraftingCategory() {
		CraftingRecipeCategory craftingCategory = new CraftingRecipeCategory(TestGuiHelper.INSTANCE);
		craftingCategory.addExtension(CraftingRecipe.class, new CraftingCategoryExtension());
		return craftingCategory;
	}

	private static JeiCraftingRecipeIngredients getJeiCraftingRecipeIngredients(
		JeiGameTestHelper helper,
		CraftingRecipeCategory craftingCategory,
		RecipeHolder<CraftingRecipe> recipeHolder
	) {
		helper.assertTrue(craftingCategory.isHandled(recipeHolder), "Generated JEI recipe should be handled by the crafting category: " + recipeHolder.id());
		List<ItemStack> inputs = getCraftingInputGrid(helper, craftingCategory, recipeHolder);
		ItemStack output = getJeiCraftingRecipeOutput(helper, recipeHolder);
		return new JeiCraftingRecipeIngredients(inputs, output);
	}

	private static ItemStack getJeiCraftingRecipeOutput(
		JeiGameTestHelper helper,
		RecipeHolder<CraftingRecipe> recipeHolder
	) {
		ItemStack output = recipeHolder.value()
			.getResultItem(helper.getLevel().registryAccess())
			.copy();
		helper.assertTrue(!output.isEmpty(), "JEI output slot resolved to an empty item stack");
		return output;
	}

	private static List<ItemStack> getCraftingInputGrid(
		JeiGameTestHelper helper,
		CraftingRecipeCategory craftingCategory,
		RecipeHolder<CraftingRecipe> recipeHolder
	) {
		ImmutableSize2i recipeSize = craftingCategory.getRecipeSize(recipeHolder);
		Map<Integer, Ingredient> inputSlots = CraftingGridHelper.getGuiSlotToIngredientMap(
			recipeHolder,
			recipeSize.width(),
			recipeSize.height()
		);
		List<ItemStack> inputs = emptyCraftingGrid();
		for (Map.Entry<Integer, Ingredient> inputSlot : inputSlots.entrySet()) {
			int craftingIndex = inputSlot.getKey();
			helper.assertTrue(craftingIndex >= 0 && craftingIndex < inputs.size(), "JEI input slot is outside the crafting grid: " + craftingIndex);
			helper.assertTrue(inputs.get(craftingIndex).isEmpty(), "Multiple JEI input slots mapped to crafting slot " + craftingIndex);
			ItemStack stack = resolveFirstStack(inputSlot.getValue());
			helper.assertTrue(!stack.isEmpty(), "JEI input slot resolved to an empty item stack: " + craftingIndex);
			inputs.set(craftingIndex, stack);
		}
		return inputs;
	}

	private static ItemStack resolveFirstStack(Ingredient ingredient) {
		ItemStack[] itemStacks = ingredient.getItems();
		if (itemStacks.length == 0) {
			return ItemStack.EMPTY;
		}
		return itemStacks[0].copy();
	}

	private static List<ItemStack> emptyCraftingGrid() {
		List<ItemStack> grid = new ArrayList<>(9);
		for (int i = 0; i < 9; i++) {
			grid.add(ItemStack.EMPTY);
		}
		return grid;
	}

	private static void removeExpectedOutput(JeiGameTestHelper helper, List<ItemStack> expectedOutputs, ItemStack output) {
		for (int i = 0; i < expectedOutputs.size(); i++) {
			if (JeiGameTestHelper.isSameStack(expectedOutputs.get(i), output)) {
				expectedOutputs.remove(i);
				return;
			}
		}
		throw helper.createFailException("Unexpected JEI recipe output: " + output + ". Expected one of: " + describeStacks(expectedOutputs));
	}

	private static String describeStacks(List<ItemStack> stacks) {
		return stacks.stream()
			.map(ItemStack::toString)
			.collect(Collectors.joining(", ", "[", "]"));
	}

	private static String describeAnvilRecipe(IJeiAnvilRecipe recipe) {
		return "uid=%s left=%s right=%s outputs=%s".formatted(
			recipe.getUid(),
			describeStacks(recipe.getLeftInputs()),
			describeStacks(recipe.getRightInputs()),
			describeStacks(recipe.getOutputs())
		);
	}

	private static String describeAnvilRecipe(IJeiAnvilRecipe recipe, ItemStack leftInput, ItemStack rightInput) {
		return "uid=%s left=%s right=%s outputs=%s".formatted(
			recipe.getUid(),
			leftInput,
			rightInput,
			describeStacks(recipe.getOutputs())
		);
	}

	private static String describeGrindstoneRecipe(IJeiGrindstoneRecipe recipe) {
		return "uid=%s top=%s bottom=%s outputs=%s".formatted(
			recipe.getUid(),
			describeStacks(recipe.getTopInputs()),
			describeStacks(recipe.getBottomInputs()),
			describeStacks(recipe.getOutputs())
		);
	}

	private static String describeGrindstoneRecipe(IJeiGrindstoneRecipe recipe, ItemStack topInput, ItemStack bottomInput) {
		return "uid=%s top=%s bottom=%s outputs=%s".formatted(
			recipe.getUid(),
			topInput,
			bottomInput,
			describeStacks(recipe.getOutputs())
		);
	}

	private static final class ThrowingEnchantabilityRecipeHelper implements IPlatformRecipeHelper {
		private final IPlatformRecipeHelper delegate;
		private boolean hasThrown;

		private ThrowingEnchantabilityRecipeHelper(IPlatformRecipeHelper delegate) {
			this.delegate = delegate;
		}

		@Override
		public Ingredient getBase(SmithingRecipe recipe) {
			return delegate.getBase(recipe);
		}

		@Override
		public Ingredient getAddition(SmithingRecipe recipe) {
			return delegate.getAddition(recipe);
		}

		@Override
		public Ingredient getTemplate(SmithingRecipe recipe) {
			return delegate.getTemplate(recipe);
		}

		@Override
		public ItemStack getGrindstoneResult(GrindstoneMenu grindstoneMenu, ItemStack input1, ItemStack input2) {
			return delegate.getGrindstoneResult(grindstoneMenu, input1, input2);
		}

		@Override
		public boolean isItemEnchantable(ItemStack stack, Holder<Enchantment> enchantment) {
			boolean isItemEnchantable = delegate.isItemEnchantable(stack, enchantment);
			if (!hasThrown && isItemEnchantable) {
				hasThrown = true;
				throw new IllegalStateException("Test enchantability failure");
			}
			return isItemEnchantable;
		}

		public boolean hasThrown() {
			return hasThrown;
		}
	}

	private record JeiCraftingRecipeIngredients(List<ItemStack> inputs, ItemStack output) {
	}

	private record AnvilResult(ItemStack output, int levelCost) {
	}

	private static final class TestRecipeHelper implements IPlatformRecipeHelper {
		private static final TestRecipeHelper INSTANCE = new TestRecipeHelper();

		@Override
		public Ingredient getBase(SmithingRecipe recipe) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Ingredient getAddition(SmithingRecipe recipe) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Ingredient getTemplate(SmithingRecipe recipe) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ItemStack getGrindstoneResult(GrindstoneMenu grindstoneMenu, ItemStack input1, ItemStack input2) {
			return grindstoneMenu.computeResult(input1, input2);
		}

		@Override
		public boolean isItemEnchantable(ItemStack stack, Holder<Enchantment> enchantment) {
			return stack.getItem().isEnchantable(stack);
		}
	}

	private static final class TestIngredientHelper implements IPlatformIngredientHelper {
		private static final TestIngredientHelper INSTANCE = new TestIngredientHelper();

		@Override
		public Ingredient createShulkerDyeIngredient(net.minecraft.world.item.DyeColor color) {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<Ingredient> getPotionContainers(PotionBrewing potionBrewing) {
			throw new UnsupportedOperationException();
		}

		@Override
		public java.util.stream.Stream<Ingredient> getPotionIngredients(PotionBrewing potionBrewing) {
			throw new UnsupportedOperationException();
		}

		@Override
		public float getCompostValue(ItemStack itemStack) {
			throw new UnsupportedOperationException();
		}

		@Override
		public HolderSet<Item> getSupportedItems(Holder<Enchantment> enchantment) {
			return enchantment.value().getSupportedItems();
		}
	}

	@FunctionalInterface
	private interface RecipeVariationConsumer {
		void accept(ItemStack leftInput, ItemStack rightInput, ItemStack output);
	}
}
