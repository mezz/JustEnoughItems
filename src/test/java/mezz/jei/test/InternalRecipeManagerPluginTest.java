package mezz.jei.test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import mezz.jei.Internal;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.collect.ListMultiMap;
import mezz.jei.gui.Focus;
import mezz.jei.ingredients.IngredientBlacklistInternal;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.ingredients.Ingredients;
import mezz.jei.recipes.InternalRecipeRegistryPlugin;
import mezz.jei.recipes.RecipeCategoryComparator;
import mezz.jei.recipes.RecipeMap;
import mezz.jei.startup.ModIngredientRegistration;
import mezz.jei.test.lib.TestIngredient;
import mezz.jei.test.lib.TestIngredientHelper;
import mezz.jei.test.lib.TestModIdHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import org.junit.Assert;
import org.junit.Test;

public class InternalRecipeManagerPluginTest {
	private static final TestIngredient CATALYST = new TestIngredient(1);
	private static final TestIngredient INPUT = new TestIngredient(2);
	private static final TestRecipeWrapper FIRST_RECIPE = new TestRecipeWrapper("first recipe");
	private static final TestRecipeWrapper SECOND_RECIPE = new TestRecipeWrapper("second recipe");
	private static final TestRecipeWrapper THIRD_RECIPE = new TestRecipeWrapper("third recipe");

	@Test
	public void inputFocusDoesNotDuplicateRecipeWhenIngredientIsAlsoCatalyst() {
		// Setup: one recipe has the focused ingredient as an input and as the category catalyst.
		PluginFixture fixture = createFixture(
			Collections.singletonList(FIRST_RECIPE),
			Collections.singletonList(CATALYST)
		);
		fixture.addRecipeInputs(FIRST_RECIPE, CATALYST);

		// Operation: request recipes for the catalyst through the internal focused lookup path.
		List<TestRecipeWrapper> recipes = fixture.getRecipes(IFocus.Mode.INPUT, CATALYST);

		// Assertions: the recipe is returned once, not once from inputs and once from catalysts.
		Assert.assertEquals(Collections.singletonList(FIRST_RECIPE), recipes);
	}

	@Test
	public void inputFocusReturnsAllCategoryRecipesForCatalyst() {
		// Setup: the focused ingredient is registered as the category catalyst but is not a recipe ingredient.
		PluginFixture fixture = createFixture(
			Arrays.asList(FIRST_RECIPE, SECOND_RECIPE),
			Collections.singletonList(CATALYST)
		);

		// Operation: request recipes for the catalyst through the internal focused lookup path.
		List<TestRecipeWrapper> recipes = fixture.getRecipes(IFocus.Mode.INPUT, CATALYST);

		// Assertions: catalyst lookup expands to all recipes in the category.
		Assert.assertEquals(Arrays.asList(FIRST_RECIPE, SECOND_RECIPE), recipes);
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
		List<TestRecipeWrapper> recipes = fixture.getRecipes(IFocus.Mode.INPUT, INPUT);

		// Assertions: direct recipe matches still work without expanding to the whole category.
		Assert.assertEquals(Collections.singletonList(FIRST_RECIPE), recipes);
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
		List<TestRecipeWrapper> recipes = fixture.getRecipes(IFocus.Mode.OUTPUT, CATALYST);

		// Assertions: output lookup returns only output matches, not every recipe for the catalyst category.
		Assert.assertEquals(Collections.singletonList(FIRST_RECIPE), recipes);
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
		List<TestRecipeWrapper> recipes = fixture.getRecipes(IFocus.Mode.INPUT, CATALYST);

		// Assertions: directly matched recipes keep their order, and category expansion only adds missing recipes.
		Assert.assertEquals(Arrays.asList(FIRST_RECIPE, SECOND_RECIPE, THIRD_RECIPE), recipes);
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
		List<TestRecipeWrapper> recipes = fixture.getRecipes(IFocus.Mode.INPUT, INPUT);

		// Assertions: duplicate ingredient entries inside one recipe do not duplicate the recipe result.
		Assert.assertEquals(Collections.singletonList(FIRST_RECIPE), recipes);
	}

	private static PluginFixture createFixture(List<TestRecipeWrapper> recipes, List<TestIngredient> catalysts) {
		IngredientRegistry ingredientRegistry = createIngredientRegistry();
		Internal.setIngredientRegistry(ingredientRegistry);

		TestRecipeCategory recipeCategory = new TestRecipeCategory();
		RecipeCategoryComparator recipeCategoryComparator = new RecipeCategoryComparator(Collections.singletonList(recipeCategory));
		RecipeMap recipeInputMap = new RecipeMap(recipeCategoryComparator, ingredientRegistry);
		RecipeMap recipeOutputMap = new RecipeMap(recipeCategoryComparator, ingredientRegistry);
		ListMultiMap<IRecipeCategory, IRecipeWrapper> recipeWrappersForCategories = new ListMultiMap<>();
		for (TestRecipeWrapper recipe : recipes) {
			recipeWrappersForCategories.put(recipeCategory, recipe);
		}

		IIngredientHelper<TestIngredient> ingredientHelper = ingredientRegistry.getIngredientHelper(TestIngredient.TYPE);
		ImmutableMultimap.Builder<String, String> categoriesForRecipeCatalystKeysBuilder = ImmutableMultimap.builder();
		for (TestIngredient catalyst : catalysts) {
			recipeInputMap.addRecipeCategory(recipeCategory, catalyst, ingredientHelper);
			categoriesForRecipeCatalystKeysBuilder.put(ingredientHelper.getUniqueId(catalyst), recipeCategory.getUid());
		}

		InternalRecipeRegistryPlugin plugin = new InternalRecipeRegistryPlugin(
			null,
			categoriesForRecipeCatalystKeysBuilder.build(),
			ingredientRegistry,
			ImmutableMap.of(recipeCategory.getUid(), recipeCategory),
			recipeInputMap,
			recipeOutputMap,
			recipeWrappersForCategories
		);
		return new PluginFixture(recipeInputMap, recipeOutputMap, recipeCategory, plugin);
	}

	private static IngredientRegistry createIngredientRegistry() {
		ModIngredientRegistration modIngredientRegistration = new ModIngredientRegistration();
		modIngredientRegistration.register(
			TestIngredient.TYPE,
			Arrays.asList(CATALYST, INPUT),
			new TestIngredientHelper(),
			new TestRenderer()
		);
		return modIngredientRegistration.createIngredientRegistry(new TestModIdHelper(), new IngredientBlacklistInternal());
	}

	private static final class PluginFixture {
		private final RecipeMap recipeInputMap;
		private final RecipeMap recipeOutputMap;
		private final TestRecipeCategory recipeCategory;
		private final InternalRecipeRegistryPlugin plugin;

		private PluginFixture(
			RecipeMap recipeInputMap,
			RecipeMap recipeOutputMap,
			TestRecipeCategory recipeCategory,
			InternalRecipeRegistryPlugin plugin
		) {
			this.recipeInputMap = recipeInputMap;
			this.recipeOutputMap = recipeOutputMap;
			this.recipeCategory = recipeCategory;
			this.plugin = plugin;
		}

		private void addRecipeInputs(TestRecipeWrapper recipe, TestIngredient... ingredients) {
			Ingredients recipeIngredients = new Ingredients();
			recipeIngredients.setInputs(TestIngredient.TYPE, Arrays.asList(ingredients));
			recipeInputMap.addRecipe(recipe, recipeCategory, recipeIngredients.getInputIngredients());
		}

		private void addRecipeOutputs(TestRecipeWrapper recipe, TestIngredient... ingredients) {
			Ingredients recipeIngredients = new Ingredients();
			recipeIngredients.setOutputs(TestIngredient.TYPE, Arrays.asList(ingredients));
			recipeOutputMap.addRecipe(recipe, recipeCategory, recipeIngredients.getOutputIngredients());
		}

		private List<TestRecipeWrapper> getRecipes(IFocus.Mode mode, TestIngredient ingredient) {
			Focus<TestIngredient> focus = new Focus<>(mode, ingredient);
			return plugin.getRecipeWrappers(recipeCategory, focus);
		}
	}

	private static final class TestRecipeWrapper implements IRecipeWrapper {
		private final String name;

		private TestRecipeWrapper(String name) {
			this.name = name;
		}

		@Override
		public void getIngredients(IIngredients ingredients) {

		}

		@Override
		public String toString() {
			return name;
		}
	}

	private static class TestRenderer implements IIngredientRenderer<TestIngredient> {
		@Override
		public void render(Minecraft minecraft, int xPosition, int yPosition, @Nullable TestIngredient ingredient) {

		}

		@Override
		public List<String> getTooltip(Minecraft minecraft, TestIngredient ingredient, ITooltipFlag tooltipFlag) {
			return Collections.singletonList(ingredient.toString());
		}
	}

	private static class TestRecipeCategory implements IRecipeCategory<TestRecipeWrapper> {
		private static final String UID = "jei.internal_plugin_test";

		@Override
		public String getUid() {
			return UID;
		}

		@Override
		public String getTitle() {
			return "Internal Plugin Test";
		}

		@Override
		public String getModName() {
			return "JEI Test";
		}

		@Override
		public IDrawable getBackground() {
			return DummyDrawable.INSTANCE;
		}

		@Nullable
		@Override
		public IDrawable getIcon() {
			return DummyDrawable.INSTANCE;
		}

		@Override
		public void setRecipe(IRecipeLayout recipeLayout, TestRecipeWrapper recipeWrapper, IIngredients ingredients) {

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
		public void draw(Minecraft minecraft, int xOffset, int yOffset) {

		}
	}
}
