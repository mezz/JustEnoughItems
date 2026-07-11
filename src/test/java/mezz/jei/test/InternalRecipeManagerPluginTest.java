package mezz.jei.test;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.mojang.blaze3d.matrix.MatrixStack;
import mezz.jei.Internal;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.gui.Focus;
import mezz.jei.ingredients.IngredientBlacklistInternal;
import mezz.jei.ingredients.IngredientManager;
import mezz.jei.ingredients.Ingredients;
import mezz.jei.ingredients.RegisteredIngredient;
import mezz.jei.recipes.InternalRecipeManagerPlugin;
import mezz.jei.recipes.RecipeCatalystBuilder;
import mezz.jei.recipes.RecipeCategoryDataMap;
import mezz.jei.recipes.RecipeMap;
import mezz.jei.test.lib.TestIngredient;
import mezz.jei.test.lib.TestIngredientHelper;
import mezz.jei.test.lib.TestModIdHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InternalRecipeManagerPluginTest {
	private static final TestIngredient CATALYST = new TestIngredient(1);
	private static final TestIngredient INPUT = new TestIngredient(2);
	private static final String FIRST_RECIPE = "first recipe";
	private static final String SECOND_RECIPE = "second recipe";
	private static final String THIRD_RECIPE = "third recipe";

	@Test
	public void inputFocusDoesNotDuplicateRecipeWhenIngredientIsAlsoCatalyst() {
		// Setup: one recipe has the focused ingredient as an input and as the category catalyst.
		PluginFixture fixture = createFixture(
			Collections.singletonList(FIRST_RECIPE),
			Collections.singletonList(CATALYST)
		);
		fixture.addRecipeInputs(FIRST_RECIPE, CATALYST);

		// Operation: request recipes for the catalyst through the internal focused lookup path.
		List<String> recipes = fixture.getRecipes(IFocus.Mode.INPUT, CATALYST);

		// Assertions: the recipe is returned once, not once from inputs and once from catalysts.
		assertEquals(Collections.singletonList(FIRST_RECIPE), recipes);
	}

	@Test
	public void inputFocusReturnsAllCategoryRecipesForCatalyst() {
		// Setup: the focused ingredient is registered as the category catalyst but is not a recipe ingredient.
		PluginFixture fixture = createFixture(
			Arrays.asList(FIRST_RECIPE, SECOND_RECIPE),
			Collections.singletonList(CATALYST)
		);

		// Operation: request recipes for the catalyst through the internal focused lookup path.
		List<String> recipes = fixture.getRecipes(IFocus.Mode.INPUT, CATALYST);

		// Assertions: catalyst lookup expands to all recipes in the category.
		assertEquals(Arrays.asList(FIRST_RECIPE, SECOND_RECIPE), recipes);
	}

	@Test
	public void inputFocusReturnsDirectRecipeForNonCatalyst() {
		// Setup: the focused ingredient appears in a recipe but is not registered as a category catalyst.
		PluginFixture fixture = createFixture(
			Arrays.asList(FIRST_RECIPE, SECOND_RECIPE),
			Collections.singletonList(CATALYST)
		);
		fixture.addRecipeInputs(FIRST_RECIPE, INPUT);

		// Operation: request recipes for the non-catalyst ingredient.
		List<String> recipes = fixture.getRecipes(IFocus.Mode.INPUT, INPUT);

		// Assertions: direct recipe matches still work without expanding to the whole category.
		assertEquals(Collections.singletonList(FIRST_RECIPE), recipes);
	}

	@Test
	public void outputFocusDoesNotUseCatalystExpansion() {
		// Setup: the focused ingredient is a category catalyst, and a recipe also uses it as an output.
		PluginFixture fixture = createFixture(
			Arrays.asList(FIRST_RECIPE, SECOND_RECIPE),
			Collections.singletonList(CATALYST)
		);
		fixture.addRecipeOutputs(FIRST_RECIPE, CATALYST);

		// Operation: request output-focused recipes for the catalyst.
		List<String> recipes = fixture.getRecipes(IFocus.Mode.OUTPUT, CATALYST);

		// Assertions: output lookup returns only output matches, not every recipe for the catalyst category.
		assertEquals(Collections.singletonList(FIRST_RECIPE), recipes);
	}

	@Test
	public void inputFocusMergesDirectAndCatalystRecipesWithoutDuplicates() {
		// Setup: two recipes match directly and all three recipes match through the category catalyst.
		PluginFixture fixture = createFixture(
			Arrays.asList(FIRST_RECIPE, SECOND_RECIPE, THIRD_RECIPE),
			Collections.singletonList(CATALYST)
		);
		fixture.addRecipeInputs(FIRST_RECIPE, CATALYST);
		fixture.addRecipeInputs(SECOND_RECIPE, CATALYST);

		// Operation: request recipes for the catalyst through the internal focused lookup path.
		List<String> recipes = fixture.getRecipes(IFocus.Mode.INPUT, CATALYST);

		// Assertions: directly matched recipes keep their order, and category expansion only adds missing recipes.
		assertEquals(Arrays.asList(FIRST_RECIPE, SECOND_RECIPE, THIRD_RECIPE), recipes);
	}

	@Test
	public void duplicateIngredientEntriesInOneRecipeReturnOneRecipe() {
		// Setup: one recipe contains the same focused ingredient more than once for the same lookup mode.
		PluginFixture fixture = createFixture(
			Collections.singletonList(FIRST_RECIPE),
			Collections.emptyList()
		);
		fixture.addRecipeInputs(FIRST_RECIPE, INPUT, INPUT);

		// Operation: request recipes for the duplicated ingredient.
		List<String> recipes = fixture.getRecipes(IFocus.Mode.INPUT, INPUT);

		// Assertions: duplicate ingredient entries inside one recipe do not duplicate the recipe result.
		assertEquals(Collections.singletonList(FIRST_RECIPE), recipes);
	}

	private static PluginFixture createFixture(List<String> recipes, List<TestIngredient> catalysts) {
		IngredientManager ingredientManager = createIngredientManager();
		Internal.setIngredientManager(ingredientManager);

		Comparator<ResourceLocation> recipeCategoryUidComparator = Comparator.comparing(ResourceLocation::toString);
		RecipeMap recipeInputMap = new RecipeMap(recipeCategoryUidComparator, ingredientManager);
		RecipeMap recipeOutputMap = new RecipeMap(recipeCategoryUidComparator, ingredientManager);
		TestRecipeCategory recipeCategory = new TestRecipeCategory();

		RecipeCatalystBuilder recipeCatalystBuilder = new RecipeCatalystBuilder(ingredientManager);
		List<Object> catalystIngredients = new ArrayList<>(catalysts);
		recipeCatalystBuilder.addCatalysts(recipeCategory, catalystIngredients, recipeInputMap);
		ImmutableListMultimap<IRecipeCategory<?>, Object> recipeCatalystsMap = recipeCatalystBuilder.buildRecipeCatalysts();
		List<IRecipeCategory<?>> recipeCategories = Collections.singletonList(recipeCategory);
		RecipeCategoryDataMap recipeCategoryDataMap = new RecipeCategoryDataMap(
			recipeCategories,
			recipeCatalystsMap
		);
		recipeCategoryDataMap.get(recipeCategory).getRecipes().addAll(recipes);

		ImmutableMultimap<String, ResourceLocation> categoriesForRecipeCatalystKeys = recipeCatalystBuilder.buildCategoriesForRecipeCatalystKeys();
		InternalRecipeManagerPlugin plugin = new InternalRecipeManagerPlugin(
			categoriesForRecipeCatalystKeys,
			ingredientManager,
			recipeCategoryDataMap,
			recipeInputMap,
			recipeOutputMap,
			() -> Stream.of(recipeCategory)
		);
		return new PluginFixture(recipeInputMap, recipeOutputMap, recipeCategory, plugin);
	}

	private static IngredientManager createIngredientManager() {
		RegisteredIngredient<TestIngredient> registeredIngredient = new RegisteredIngredient<>(
			TestIngredient.TYPE,
			Arrays.asList(CATALYST, INPUT),
			new TestIngredientHelper(),
			new TestRenderer()
		);
		List<RegisteredIngredient<?>> registeredIngredients = Collections.singletonList(registeredIngredient);
		return new IngredientManager(
			new TestModIdHelper(),
			new IngredientBlacklistInternal(),
			registeredIngredients,
			true
		);
	}

	private static final class PluginFixture {
		private final RecipeMap recipeInputMap;
		private final RecipeMap recipeOutputMap;
		private final TestRecipeCategory recipeCategory;
		private final InternalRecipeManagerPlugin plugin;

		private PluginFixture(
			RecipeMap recipeInputMap,
			RecipeMap recipeOutputMap,
			TestRecipeCategory recipeCategory,
			InternalRecipeManagerPlugin plugin
		) {
			this.recipeInputMap = recipeInputMap;
			this.recipeOutputMap = recipeOutputMap;
			this.recipeCategory = recipeCategory;
			this.plugin = plugin;
		}

		private void addRecipeInputs(String recipe, TestIngredient... ingredients) {
			Ingredients recipeIngredients = new Ingredients();
			recipeIngredients.setInputs(TestIngredient.TYPE, Arrays.asList(ingredients));
			recipeInputMap.addRecipe(recipe, recipeCategory, recipeIngredients.getInputIngredients());
		}

		private void addRecipeOutputs(String recipe, TestIngredient... ingredients) {
			Ingredients recipeIngredients = new Ingredients();
			recipeIngredients.setOutputs(TestIngredient.TYPE, Arrays.asList(ingredients));
			recipeOutputMap.addRecipe(recipe, recipeCategory, recipeIngredients.getOutputIngredients());
		}

		private List<String> getRecipes(IFocus.Mode mode, TestIngredient ingredient) {
			Focus<TestIngredient> focus = new Focus<>(mode, ingredient);
			return plugin.getRecipes(recipeCategory, focus);
		}
	}

	private static class TestRenderer implements IIngredientRenderer<TestIngredient> {
		@Override
		public void render(MatrixStack matrixStack, int xPosition, int yPosition, @Nullable TestIngredient ingredient) {

		}

		@Override
		public List<ITextComponent> getTooltip(TestIngredient ingredient, ITooltipFlag tooltipFlag) {
			return Collections.singletonList(new StringTextComponent(ingredient.toString()));
		}
	}

	private static class TestRecipeCategory implements IRecipeCategory<String> {
		private static final ResourceLocation UID = new ResourceLocation("jei", "internal_plugin_test");

		@Override
		public ResourceLocation getUid() {
			return UID;
		}

		@Override
		public Class<? extends String> getRecipeClass() {
			return String.class;
		}

		@Override
		public String getTitle() {
			return "Internal Plugin Test";
		}

		@Override
		public IDrawable getBackground() {
			return DummyDrawable.INSTANCE;
		}

		@Override
		public IDrawable getIcon() {
			return DummyDrawable.INSTANCE;
		}

		@Override
		public void setIngredients(String recipe, IIngredients ingredients) {

		}

		@Override
		public void setRecipe(IRecipeLayout recipeLayout, String recipe, IIngredients ingredients) {

		}
	}

	private enum DummyDrawable implements IDrawable {
		INSTANCE;

		@Override
		public int getWidth() {
			return 1;
		}

		@Override
		public int getHeight() {
			return 1;
		}

		@Override
		public void draw(MatrixStack matrixStack, int xOffset, int yOffset) {

		}
	}
}
