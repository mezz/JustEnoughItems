package mezz.jei.test;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.focus.Focus;
import mezz.jei.common.ingredients.IIngredientSupplier;
import mezz.jei.common.ingredients.RegisteredIngredients;
import mezz.jei.common.ingredients.TypedIngredient;
import mezz.jei.common.ingredients.subtypes.SubtypeManager;
import mezz.jei.common.load.registration.RegisteredIngredientsBuilder;
import mezz.jei.common.load.registration.SubtypeRegistration;
import mezz.jei.common.recipes.InternalRecipeManagerPlugin;
import mezz.jei.common.recipes.RecipeCatalystBuilder;
import mezz.jei.common.recipes.collect.RecipeMap;
import mezz.jei.common.recipes.collect.RecipeTypeDataMap;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InternalRecipeManagerPluginTest {
	private static final IIngredientType<TestIngredient> INGREDIENT_TYPE = () -> TestIngredient.class;
	private static final RecipeType<String> RECIPE_TYPE = RecipeType.create("jei", "internal_plugin_test", String.class);
	private static final TestIngredient CATALYST = new TestIngredient(1);
	private static final TestIngredient INPUT = new TestIngredient(2);
	private static final String FIRST_RECIPE = "first recipe";
	private static final String SECOND_RECIPE = "second recipe";
	private static final String THIRD_RECIPE = "third recipe";

	@Test
	public void catalystFocusDoesNotDuplicateRecipeWhenIngredientIsAlsoCatalyst() {
		// Setup: one recipe has the focused ingredient as a catalyst role and as the category catalyst.
		PluginFixture fixture = createFixture(List.of(FIRST_RECIPE), List.of(CATALYST));
		ITypedIngredient<TestIngredient> catalyst = fixture.typedIngredient(CATALYST);
		fixture.addRecipeIngredients(FIRST_RECIPE, RecipeIngredientRole.CATALYST, catalyst);

		// Operation: request recipes for the catalyst through the internal focused lookup path.
		List<String> recipes = fixture.getRecipes(RecipeIngredientRole.CATALYST, catalyst);

		// Assertions: the recipe is returned once, not once from ingredients and once from catalysts.
		assertEquals(List.of(FIRST_RECIPE), recipes);
	}

	@Test
	public void catalystFocusReturnsAllCategoryRecipesForCatalyst() {
		// Setup: the focused ingredient is registered as the category catalyst but is not a recipe ingredient.
		PluginFixture fixture = createFixture(List.of(FIRST_RECIPE, SECOND_RECIPE), List.of(CATALYST));
		ITypedIngredient<TestIngredient> catalyst = fixture.typedIngredient(CATALYST);

		// Operation: request recipes for the catalyst through the internal focused lookup path.
		List<String> recipes = fixture.getRecipes(RecipeIngredientRole.CATALYST, catalyst);

		// Assertions: catalyst lookup expands to all recipes in the category.
		assertEquals(List.of(FIRST_RECIPE, SECOND_RECIPE), recipes);
	}

	@Test
	public void catalystFocusReturnsDirectRecipeForNonCatalyst() {
		// Setup: the focused ingredient appears in a recipe but is not registered as a category catalyst.
		PluginFixture fixture = createFixture(List.of(FIRST_RECIPE, SECOND_RECIPE), List.of(CATALYST));
		ITypedIngredient<TestIngredient> input = fixture.typedIngredient(INPUT);
		fixture.addRecipeIngredients(FIRST_RECIPE, RecipeIngredientRole.CATALYST, input);

		// Operation: request recipes for the non-catalyst ingredient.
		List<String> recipes = fixture.getRecipes(RecipeIngredientRole.CATALYST, input);

		// Assertions: direct recipe matches still work without expanding to the whole category.
		assertEquals(List.of(FIRST_RECIPE), recipes);
	}

	@Test
	public void inputFocusDoesNotUseCatalystExpansion() {
		// Setup: the focused ingredient is a category catalyst, and a recipe also uses it as an input.
		PluginFixture fixture = createFixture(List.of(FIRST_RECIPE, SECOND_RECIPE), List.of(CATALYST));
		ITypedIngredient<TestIngredient> catalyst = fixture.typedIngredient(CATALYST);
		fixture.addRecipeIngredients(FIRST_RECIPE, RecipeIngredientRole.INPUT, catalyst);

		// Operation: request input-focused recipes for the catalyst.
		List<String> recipes = fixture.getRecipes(RecipeIngredientRole.INPUT, catalyst);

		// Assertions: input lookup returns only input matches, not every recipe for the catalyst category.
		assertEquals(List.of(FIRST_RECIPE), recipes);
	}

	@Test
	public void catalystFocusMergesDirectAndCatalystRecipesWithoutDuplicates() {
		// Setup: two recipes match directly and all three recipes match through the category catalyst.
		PluginFixture fixture = createFixture(List.of(FIRST_RECIPE, SECOND_RECIPE, THIRD_RECIPE), List.of(CATALYST));
		ITypedIngredient<TestIngredient> catalyst = fixture.typedIngredient(CATALYST);
		fixture.addRecipeIngredients(FIRST_RECIPE, RecipeIngredientRole.CATALYST, catalyst);
		fixture.addRecipeIngredients(SECOND_RECIPE, RecipeIngredientRole.CATALYST, catalyst);

		// Operation: request recipes for the catalyst through the internal focused lookup path.
		List<String> recipes = fixture.getRecipes(RecipeIngredientRole.CATALYST, catalyst);

		// Assertions: directly matched recipes keep their order, and category expansion only adds missing recipes.
		assertEquals(List.of(FIRST_RECIPE, SECOND_RECIPE, THIRD_RECIPE), recipes);
	}

	@Test
	public void duplicateIngredientEntriesInOneRecipeReturnOneRecipe() {
		// Setup: one recipe contains the same focused ingredient more than once for the same role.
		PluginFixture fixture = createFixture(List.of(FIRST_RECIPE), List.of());
		ITypedIngredient<TestIngredient> input = fixture.typedIngredient(INPUT);
		fixture.addRecipeIngredients(FIRST_RECIPE, RecipeIngredientRole.INPUT, input, input);

		// Operation: request recipes for the duplicated ingredient.
		List<String> recipes = fixture.getRecipes(RecipeIngredientRole.INPUT, input);

		// Assertions: duplicate ingredient entries inside one recipe do not duplicate the recipe result.
		assertEquals(List.of(FIRST_RECIPE), recipes);
	}

	private static PluginFixture createFixture(List<String> recipes, List<TestIngredient> catalysts) {
		RegisteredIngredients registeredIngredients = createRegisteredIngredients();
		EnumMap<RecipeIngredientRole, RecipeMap> roleMaps = createRoleMaps(registeredIngredients);
		TestRecipeCategory recipeCategory = new TestRecipeCategory();
		List<ITypedIngredient<?>> typedCatalysts = catalysts.stream()
			.<ITypedIngredient<?>>map(ingredient -> typedIngredient(registeredIngredients, ingredient))
			.toList();
		RecipeCatalystBuilder recipeCatalystBuilder = new RecipeCatalystBuilder(registeredIngredients, roleMaps.get(RecipeIngredientRole.CATALYST));
		recipeCatalystBuilder.addCategoryCatalysts(recipeCategory, typedCatalysts);

		RecipeTypeDataMap recipeTypeDataMap = new RecipeTypeDataMap(
			List.of(recipeCategory),
			recipeCatalystBuilder.buildRecipeCategoryCatalysts()
		);
		recipeTypeDataMap.get(RECIPE_TYPE).addRecipes(recipes);

		InternalRecipeManagerPlugin plugin = new InternalRecipeManagerPlugin(
			registeredIngredients,
			recipeTypeDataMap,
			roleMaps
		);
		return new PluginFixture(registeredIngredients, roleMaps, recipeCategory, plugin);
	}

	private static EnumMap<RecipeIngredientRole, RecipeMap> createRoleMaps(RegisteredIngredients registeredIngredients) {
		Comparator<RecipeType<?>> recipeTypeComparator = Comparator.comparing(recipeType -> recipeType.getUid().toString());
		EnumMap<RecipeIngredientRole, RecipeMap> roleMaps = new EnumMap<>(RecipeIngredientRole.class);
		for (RecipeIngredientRole role : RecipeIngredientRole.values()) {
			roleMaps.put(role, new RecipeMap(recipeTypeComparator, registeredIngredients, role));
		}
		return roleMaps;
	}

	private static RegisteredIngredients createRegisteredIngredients() {
		SubtypeManager subtypeManager = new SubtypeManager(new SubtypeRegistration());
		RegisteredIngredientsBuilder builder = new RegisteredIngredientsBuilder(subtypeManager);
		builder.register(
			INGREDIENT_TYPE,
			List.of(CATALYST, INPUT),
			new TestIngredientHelper(),
			new TestIngredientRenderer()
		);
		return builder.build();
	}

	private static ITypedIngredient<TestIngredient> typedIngredient(RegisteredIngredients registeredIngredients, TestIngredient ingredient) {
		return TypedIngredient.createTyped(registeredIngredients, INGREDIENT_TYPE, ingredient)
			.orElseThrow();
	}

	private record PluginFixture(
		RegisteredIngredients registeredIngredients,
		EnumMap<RecipeIngredientRole, RecipeMap> roleMaps,
		TestRecipeCategory recipeCategory,
		InternalRecipeManagerPlugin plugin
	) {
		private ITypedIngredient<TestIngredient> typedIngredient(TestIngredient ingredient) {
			return InternalRecipeManagerPluginTest.typedIngredient(registeredIngredients, ingredient);
		}

		@SafeVarargs
		private final void addRecipeIngredients(String recipe, RecipeIngredientRole role, ITypedIngredient<?>... ingredients) {
			IIngredientSupplier ingredientSupplier = new IIngredientSupplier() {
				@Override
				public Stream<? extends IIngredientType<?>> getIngredientTypes(RecipeIngredientRole queriedRole) {
					if (queriedRole == role) {
						return Stream.of(INGREDIENT_TYPE);
					}
					return Stream.of();
				}

				@Override
				public <T> Stream<T> getIngredientStream(IIngredientType<T> ingredientType, RecipeIngredientRole queriedRole) {
					if (queriedRole == role && ingredientType == INGREDIENT_TYPE) {
						@SuppressWarnings("unchecked")
						Stream<T> stream = Arrays.stream(ingredients)
							.map(ITypedIngredient::getIngredient)
							.map(ingredient -> (T) ingredient);
						return stream;
					}
					return Stream.of();
				}
			};
			roleMaps.get(role).addRecipe(RECIPE_TYPE, recipe, ingredientSupplier);
		}

		private List<String> getRecipes(RecipeIngredientRole role, ITypedIngredient<TestIngredient> ingredient) {
			Focus<TestIngredient> focus = new Focus<>(role, ingredient);
			return plugin.getRecipes(recipeCategory, focus);
		}
	}

	private record TestIngredient(int number) {
	}

	private static class TestIngredientHelper implements IIngredientHelper<TestIngredient> {
		@Override
		public IIngredientType<TestIngredient> getIngredientType() {
			return INGREDIENT_TYPE;
		}

		@Override
		public String getDisplayName(TestIngredient ingredient) {
			return "Ingredient " + ingredient.number();
		}

		@Override
		public String getUniqueId(TestIngredient ingredient, UidContext context) {
			return Integer.toString(ingredient.number());
		}

		@Override
		public String getModId(TestIngredient ingredient) {
			return "test";
		}

		@Override
		public String getResourceId(TestIngredient ingredient) {
			return Integer.toString(ingredient.number());
		}

		@Override
		public ResourceLocation getResourceLocation(TestIngredient ingredient) {
			return new ResourceLocation("test", Integer.toString(ingredient.number()));
		}

		@Override
		public TestIngredient copyIngredient(TestIngredient ingredient) {
			return ingredient;
		}

		@Override
		public String getErrorInfo(@Nullable TestIngredient ingredient) {
			return String.valueOf(ingredient);
		}
	}

	private static class TestIngredientRenderer implements IIngredientRenderer<TestIngredient> {
		@Override
		public void render(PoseStack stack, TestIngredient ingredient) {

		}

		@Override
		public List<Component> getTooltip(TestIngredient ingredient, TooltipFlag tooltipFlag) {
			return List.of(new TextComponent(Integer.toString(ingredient.number())));
		}
	}

	private static class TestRecipeCategory implements IRecipeCategory<String> {
		@Override
		public RecipeType<String> getRecipeType() {
			return RECIPE_TYPE;
		}

		@Override
		public ResourceLocation getUid() {
			return RECIPE_TYPE.getUid();
		}

		@Override
		public Class<? extends String> getRecipeClass() {
			return String.class;
		}

		@Override
		public Component getTitle() {
			return new TextComponent("Internal Plugin Test");
		}

		@Override
		public IDrawable getBackground() {
			return DummyDrawable.INSTANCE;
		}

		@Override
		public @Nullable IDrawable getIcon() {
			return null;
		}

		@Override
		public void setRecipe(IRecipeLayoutBuilder builder, String recipe, IFocusGroup focuses) {

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
		public void draw(PoseStack poseStack, int xOffset, int yOffset) {

		}
	}

}
