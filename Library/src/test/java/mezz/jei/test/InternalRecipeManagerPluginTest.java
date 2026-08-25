package mezz.jei.test;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.library.focus.Focus;
import mezz.jei.library.ingredients.IIngredientSupplier;
import mezz.jei.library.ingredients.subtypes.SubtypeInterpreters;
import mezz.jei.library.ingredients.subtypes.SubtypeManager;
import mezz.jei.library.load.registration.IngredientManagerBuilder;
import mezz.jei.library.recipes.InternalRecipeManagerPlugin;
import mezz.jei.library.recipes.RecipeCatalystBuilder;
import mezz.jei.library.recipes.collect.RecipeMap;
import mezz.jei.library.recipes.collect.RecipeTypeDataMap;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

public class InternalRecipeManagerPluginTest {
	private static final IIngredientType<TestIngredient> INGREDIENT_TYPE = new TestIngredientType("test");
	private static final IIngredientType<OtherTestIngredient> OTHER_INGREDIENT_TYPE = () -> OtherTestIngredient.class;
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

	@Test
	public void ingredientUidsAreScopedToTheirIngredientType() {
		IIngredientManager ingredientManager = createIngredientManager();
		RecipeMap roleMap = createRoleMaps(ingredientManager).get(RecipeIngredientRole.INPUT);
		ITypedIngredient<TestIngredient> input = typedIngredient(ingredientManager, INPUT);
		ITypedIngredient<OtherTestIngredient> otherInput = ingredientManager.createTypedIngredient(
				OTHER_INGREDIENT_TYPE,
				new OtherTestIngredient(INPUT.number()),
				false
			)
			.orElseThrow();
		IIngredientSupplier ingredientSupplier = role -> {
			if (role == RecipeIngredientRole.INPUT) {
				return List.of(input);
			}
			return List.of();
		};
		roleMap.addRecipe(RECIPE_TYPE, FIRST_RECIPE, ingredientSupplier);

		assertEquals(List.of(), roleMap.getRecipes(RECIPE_TYPE, otherInput));
	}

	@Test
	public void equalIngredientTypesShareRecipeUidRows() {
		IIngredientType<TestIngredient> equalButDistinctType = new TestIngredientType("test");
		assertNotSame(INGREDIENT_TYPE, equalButDistinctType);

		IIngredientManager ingredientManager = createIngredientManager();
		RecipeMap roleMap = createRoleMaps(ingredientManager).get(RecipeIngredientRole.INPUT);
		ITypedIngredient<TestIngredient> input = typedIngredient(ingredientManager, INPUT);
		ITypedIngredient<TestIngredient> equalTypeInput = ingredientManager.createTypedIngredient(equalButDistinctType, INPUT, false)
			.orElseThrow();
		IIngredientSupplier ingredientSupplier = role -> {
			if (role == RecipeIngredientRole.INPUT) {
				return List.of(input);
			}
			return List.of();
		};
		roleMap.addRecipe(RECIPE_TYPE, FIRST_RECIPE, ingredientSupplier);

		assertEquals(List.of(FIRST_RECIPE), roleMap.getRecipes(RECIPE_TYPE, equalTypeInput));
	}

	private static PluginFixture createFixture(List<String> recipes, List<TestIngredient> catalysts) {
		IIngredientManager ingredientManager = createIngredientManager();
		EnumMap<RecipeIngredientRole, RecipeMap> roleMaps = createRoleMaps(ingredientManager);
		TestRecipeCategory recipeCategory = new TestRecipeCategory();
		List<ITypedIngredient<?>> typedCatalysts = catalysts.stream()
			.<ITypedIngredient<?>>map(ingredient -> typedIngredient(ingredientManager, ingredient))
			.toList();
		RecipeCatalystBuilder recipeCatalystBuilder = new RecipeCatalystBuilder(roleMaps.get(RecipeIngredientRole.CATALYST));
		recipeCatalystBuilder.addCategoryCatalysts(recipeCategory, typedCatalysts);

		RecipeTypeDataMap recipeTypeDataMap = new RecipeTypeDataMap(
			List.of(recipeCategory),
			recipeCatalystBuilder.buildRecipeCategoryCatalysts()
		);
		recipeTypeDataMap.get(RECIPE_TYPE).addRecipes(recipes);

		InternalRecipeManagerPlugin plugin = new InternalRecipeManagerPlugin(
			ingredientManager,
			recipeTypeDataMap,
			roleMaps
		);
		return new PluginFixture(ingredientManager, roleMaps, recipeCategory, plugin);
	}

	private static EnumMap<RecipeIngredientRole, RecipeMap> createRoleMaps(IIngredientManager ingredientManager) {
		Comparator<RecipeType<?>> recipeTypeComparator = Comparator.comparing(recipeType -> recipeType.getUid().toString());
		EnumMap<RecipeIngredientRole, RecipeMap> roleMaps = new EnumMap<>(RecipeIngredientRole.class);
		for (RecipeIngredientRole role : RecipeIngredientRole.values()) {
			roleMaps.put(role, new RecipeMap(recipeTypeComparator, ingredientManager, role));
		}
		return roleMaps;
	}

	private static IIngredientManager createIngredientManager() {
		SubtypeManager subtypeManager = new SubtypeManager(new SubtypeInterpreters());
		IngredientManagerBuilder builder = new IngredientManagerBuilder(subtypeManager, DummyColorHelper.INSTANCE);
		builder.register(
			INGREDIENT_TYPE,
			List.of(CATALYST, INPUT),
			new TestIngredientHelper(),
			new TestIngredientRenderer()
		);
		builder.register(
			OTHER_INGREDIENT_TYPE,
			List.of(new OtherTestIngredient(INPUT.number())),
			new OtherTestIngredientHelper(),
			new OtherTestIngredientRenderer()
		);
		return builder.build();
	}

	private static ITypedIngredient<TestIngredient> typedIngredient(IIngredientManager ingredientManager, TestIngredient ingredient) {
		return ingredientManager.createTypedIngredient(INGREDIENT_TYPE, ingredient)
			.orElseThrow();
	}

	private record PluginFixture(
		IIngredientManager ingredientManager,
		EnumMap<RecipeIngredientRole, RecipeMap> roleMaps,
		TestRecipeCategory recipeCategory,
		InternalRecipeManagerPlugin plugin
	) {
		private ITypedIngredient<TestIngredient> typedIngredient(TestIngredient ingredient) {
			return InternalRecipeManagerPluginTest.typedIngredient(ingredientManager, ingredient);
		}

		@SafeVarargs
		private final void addRecipeIngredients(String recipe, RecipeIngredientRole role, ITypedIngredient<?>... ingredients) {
			IIngredientSupplier ingredientSupplier = queriedRole -> {
				if (queriedRole == role) {
					return List.of(ingredients);
				}
				return List.of();
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

	private record TestIngredientType(String id) implements IIngredientType<TestIngredient> {
		@Override
		public Class<? extends TestIngredient> getIngredientClass() {
			return TestIngredient.class;
		}
	}

	private record OtherTestIngredient(int number) {
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
		public void render(GuiGraphics guiGraphics, TestIngredient ingredient) {

		}

		@Override
		@Deprecated(since = "15.54.0", forRemoval = true)
		@SuppressWarnings("removal")
		public List<Component> getTooltip(TestIngredient ingredient, TooltipFlag tooltipFlag) {
			return getTooltip(ingredient, null, tooltipFlag);
		}

		@Override
		@Deprecated(since = "15.54.0", forRemoval = true)
		@SuppressWarnings("removal")
		public void getTooltip(ITooltipBuilder tooltip, TestIngredient ingredient, TooltipFlag tooltipFlag) {
			getTooltip(tooltip, ingredient, null, tooltipFlag);
		}

		@Override
		public List<Component> getTooltip(TestIngredient ingredient, @Nullable Player player, TooltipFlag tooltipFlag) {
			return List.of(Component.literal(Integer.toString(ingredient.number())));
		}
	}

	private static class OtherTestIngredientHelper implements IIngredientHelper<OtherTestIngredient> {
		@Override
		public IIngredientType<OtherTestIngredient> getIngredientType() {
			return OTHER_INGREDIENT_TYPE;
		}

		@Override
		public String getDisplayName(OtherTestIngredient ingredient) {
			return "Other Ingredient " + ingredient.number();
		}

		@Override
		public String getUniqueId(OtherTestIngredient ingredient, UidContext context) {
			return Integer.toString(ingredient.number());
		}

		@Override
		public Object getUid(OtherTestIngredient ingredient, UidContext context) {
			return ingredient.number();
		}

		@Override
		public ResourceLocation getResourceLocation(OtherTestIngredient ingredient) {
			return new ResourceLocation("test_other", Integer.toString(ingredient.number()));
		}

		@Override
		public OtherTestIngredient copyIngredient(OtherTestIngredient ingredient) {
			return ingredient;
		}

		@Override
		public String getErrorInfo(@Nullable OtherTestIngredient ingredient) {
			return String.valueOf(ingredient);
		}
	}

	private static class OtherTestIngredientRenderer implements IIngredientRenderer<OtherTestIngredient> {
		@Override
		public void render(GuiGraphics guiGraphics, OtherTestIngredient ingredient) {
		}

		@Override
		@Deprecated(since = "15.54.0", forRemoval = true)
		@SuppressWarnings("removal")
		public List<Component> getTooltip(OtherTestIngredient ingredient, TooltipFlag tooltipFlag) {
			return getTooltip(ingredient, null, tooltipFlag);
		}

		@Override
		@Deprecated(since = "15.54.0", forRemoval = true)
		@SuppressWarnings("removal")
		public void getTooltip(ITooltipBuilder tooltip, OtherTestIngredient ingredient, TooltipFlag tooltipFlag) {
			getTooltip(tooltip, ingredient, null, tooltipFlag);
		}

		@Override
		public List<Component> getTooltip(OtherTestIngredient ingredient, @Nullable Player player, TooltipFlag tooltipFlag) {
			return List.of(Component.literal(Integer.toString(ingredient.number())));
		}
	}

	private static class TestRecipeCategory implements IRecipeCategory<String> {
		@Override
		public RecipeType<String> getRecipeType() {
			return RECIPE_TYPE;
		}

		@Override
		public Component getTitle() {
			return Component.literal("Internal Plugin Test");
		}

		@Override
		public int getWidth() {
			return 1;
		}

		@Override
		public int getHeight() {
			return 1;
		}

		@Override
		public @Nullable IDrawable getIcon() {
			return null;
		}

		@Override
		public void setRecipe(IRecipeLayoutBuilder builder, String recipe, IFocusGroup focuses) {

		}
	}

	private enum DummyColorHelper implements IColorHelper {
		INSTANCE;

		@Override
		public List<Integer> getColors(TextureAtlasSprite textureAtlasSprite, int renderColor, int colorCount) {
			return List.of();
		}

		@Override
		public List<Integer> getColors(ItemStack itemStack, int colorCount) {
			return List.of();
		}

		@Override
		public String getClosestColorName(int color) {
			return "";
		}
	}
}
