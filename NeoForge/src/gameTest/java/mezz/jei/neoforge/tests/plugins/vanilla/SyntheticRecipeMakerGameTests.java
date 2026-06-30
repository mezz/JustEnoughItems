package mezz.jei.neoforge.tests.plugins.vanilla;

import mezz.jei.common.util.ImmutableSize2i;
import mezz.jei.common.util.RegistryUtil;
import mezz.jei.library.gui.helpers.CraftingGridHelper;
import mezz.jei.library.plugins.vanilla.crafting.CraftingCategoryExtension;
import mezz.jei.library.plugins.vanilla.crafting.CraftingRecipeCategory;
import mezz.jei.library.plugins.vanilla.crafting.replacers.ShieldDecorationRecipeMaker;
import mezz.jei.library.plugins.vanilla.crafting.replacers.TippedArrowRecipeMaker;
import mezz.jei.neoforge.tests.lib.JeiGameTestHelper;
import mezz.jei.neoforge.tests.lib.TestGuiHelper;
import mezz.jei.neoforge.tests.lib.TestIngredientManagers;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;

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
		prepareRegistries(helper);
		List<ItemStack> expectedOutputs = createExpectedTippedArrowOutputs();
		List<RecipeHolder<CraftingRecipe>> jeiRecipes = createTippedArrowRecipes();

		assertJeiRecipesCraftExpectedOutputs(helper, expectedOutputs, jeiRecipes);

		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Every banner color has a shield-decoration JEI recipe that crafts in a real crafting table.")
	public static void shieldDecorationRecipesCraftExpectedRegistryOutputs(JeiGameTestHelper helper) {
		prepareRegistries(helper);
		List<ItemStack> expectedOutputs = createExpectedShieldDecorationOutputs();
		List<RecipeHolder<CraftingRecipe>> jeiRecipes = createShieldDecorationRecipes();

		assertJeiRecipesCraftExpectedOutputs(helper, expectedOutputs, jeiRecipes);

		helper.succeed();
	}

	private static void prepareRegistries(JeiGameTestHelper helper) {
		RegistryUtil.setRegistryAccess(helper.getLevel().registryAccess());
	}

	private static List<RecipeHolder<CraftingRecipe>> createTippedArrowRecipes() {
		List<RecipeHolder<CraftingRecipe>> recipes = new ArrayList<>();
		TippedArrowRecipeMaker recipeMaker = new TippedArrowRecipeMaker(TestIngredientManagers.createVanillaRecipeFactory());
		recipeMaker.createRecipes(recipes::add);
		return recipes;
	}

	private static List<ItemStack> createExpectedTippedArrowOutputs() {
		Registry<Potion> potionRegistry = RegistryUtil.getRegistry(Registries.POTION);
		return potionRegistry.listElements()
			.map(SyntheticRecipeMakerGameTests::createTippedArrowOutput)
			.toList();
	}

	private static ItemStack createTippedArrowOutput(Holder<Potion> potion) {
		ItemStack stack = new ItemStack(Items.TIPPED_ARROW, 8);
		stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
		return stack;
	}

	private static List<RecipeHolder<CraftingRecipe>> createShieldDecorationRecipes() {
		List<RecipeHolder<CraftingRecipe>> recipes = new ArrayList<>();
		ShieldDecorationRecipeMaker.createRecipes(recipes::add);
		return recipes;
	}

	private static List<ItemStack> createExpectedShieldDecorationOutputs() {
		return Items.BANNER.asList()
			.stream()
			.map(BannerItem.class::cast)
			.map(SyntheticRecipeMakerGameTests::createDecoratedShieldOutput)
			.toList();
	}

	private static ItemStack createDecoratedShieldOutput(BannerItem banner) {
		ItemStack stack = new ItemStack(Items.SHIELD);
		stack.set(DataComponents.BASE_COLOR, banner.getColor());
		return stack;
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
		helper.assertTrue(craftingCategory.isHandled(recipeHolder), "Generated JEI recipe should be handled by the crafting category: " + recipeHolder.id().identifier());
		ContextMap displayContext = SlotDisplayContext.fromLevel(helper.getLevel());
		List<ItemStack> inputs = getCraftingInputGrid(helper, displayContext, craftingCategory, recipeHolder);
		ItemStack output = getJeiCraftingRecipeOutput(helper, displayContext, recipeHolder);
		return new JeiCraftingRecipeIngredients(inputs, output);
	}

	private static ItemStack getJeiCraftingRecipeOutput(
		JeiGameTestHelper helper,
		ContextMap displayContext,
		RecipeHolder<CraftingRecipe> recipeHolder
	) {
		ItemStack output = resolveFirstStack(displayContext, getRecipeDisplay(helper, recipeHolder).result());
		helper.assertTrue(!output.isEmpty(), "JEI output slot resolved to an empty item stack");
		return output;
	}

	private static List<ItemStack> getCraftingInputGrid(
		JeiGameTestHelper helper,
		ContextMap displayContext,
		CraftingRecipeCategory craftingCategory,
		RecipeHolder<CraftingRecipe> recipeHolder
	) {
		ImmutableSize2i recipeSize = craftingCategory.getRecipeSize(recipeHolder);
		Map<Integer, SlotDisplay> inputSlots = CraftingGridHelper.getGuiSlotToIngredientMap(
			craftingCategory.getIngredients(recipeHolder),
			recipeSize.width(),
			recipeSize.height()
		);
		List<ItemStack> inputs = emptyCraftingGrid();
		for (Map.Entry<Integer, SlotDisplay> inputSlot : inputSlots.entrySet()) {
			int craftingIndex = inputSlot.getKey();
			helper.assertTrue(craftingIndex >= 0 && craftingIndex < inputs.size(), "JEI input slot is outside the crafting grid: " + craftingIndex);
			helper.assertTrue(inputs.get(craftingIndex).isEmpty(), "Multiple JEI input slots mapped to crafting slot " + craftingIndex);
			ItemStack stack = resolveFirstStack(displayContext, inputSlot.getValue());
			helper.assertTrue(!stack.isEmpty(), "JEI input slot resolved to an empty item stack: " + craftingIndex);
			inputs.set(craftingIndex, stack);
		}
		return inputs;
	}

	private static RecipeDisplay getRecipeDisplay(JeiGameTestHelper helper, RecipeHolder<CraftingRecipe> recipeHolder) {
		List<RecipeDisplay> displays = recipeHolder.value().display();
		helper.assertEquals(1, displays.size(), "Recipe display count");
		return displays.getFirst();
	}

	private static ItemStack resolveFirstStack(ContextMap displayContext, SlotDisplay display) {
		return display.resolveForFirstStack(displayContext).copy();
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

	private record JeiCraftingRecipeIngredients(List<ItemStack> inputs, ItemStack output) {
	}
}
