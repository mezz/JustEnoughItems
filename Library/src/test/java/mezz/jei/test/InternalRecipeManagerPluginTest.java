package mezz.jei.test;

import com.mojang.serialization.Codec;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.library.focus.Focus;
import mezz.jei.library.gui.recipes.supplier.builder.IngredientSupplierBuilder;
import mezz.jei.library.focus.FocusGroup;
import mezz.jei.library.ingredients.RecipeIngredientSupplier;
import mezz.jei.library.ingredients.IIngredientManagerInternal;
import mezz.jei.library.ingredients.SlotIngredient;
import mezz.jei.library.ingredients.subtypes.SubtypeInterpreters;
import mezz.jei.library.ingredients.subtypes.SubtypeManager;
import mezz.jei.library.load.registration.IngredientManagerBuilder;
import mezz.jei.library.recipes.CraftingStationBuilder;
import mezz.jei.library.recipes.InternalRecipeManagerPlugin;
import mezz.jei.library.recipes.collect.RecipeIngredientRoleMap;
import mezz.jei.library.recipes.collect.RecipeTypeDataMap;
import mezz.jei.library.util.IngredientSupplierHelper;
import mezz.jei.library.util.RecipeDebugUtil;
import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InternalRecipeManagerPluginTest {
	private static final IIngredientType<TestIngredient> INGREDIENT_TYPE = new TestIngredientType("test");
	private static final IIngredientType<OtherTestIngredient> OTHER_INGREDIENT_TYPE = () -> OtherTestIngredient.class;
	private static final IRecipeType<String> RECIPE_TYPE = IRecipeType.create("jei", "internal_plugin_test", String.class);
	private static final TestIngredient CATALYST = new TestIngredient(1);
	private static final TestIngredient INPUT = new TestIngredient(2);
	private static final String FIRST_RECIPE = "first recipe";
	private static final String SECOND_RECIPE = "second recipe";
	private static final String THIRD_RECIPE = "third recipe";

	@BeforeAll
	static void setup() {
		SharedConstants.setVersion(DetectedVersion.BUILT_IN);
		Bootstrap.bootStrap();
	}

	@Test
	void recipeLookupDoesNotReadRenderOnlyIngredientsOrResolveTheirDisplays() {
		IIngredientManagerInternal ingredientManager = createIngredientManager();
		List<ItemStack> fuels = unreadableIngredients();
		SlotDisplay fuelDisplay = new SlotDisplay() {
			@Override
			public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
				throw new AssertionError("Recipe lookup must not resolve render-only displays");
			}

			@Override
			public Type<? extends SlotDisplay> type() {
				throw new AssertionError("Recipe lookup must not interpret render-only displays");
			}
		};
		TestRecipeCategory category = new TestRecipeCategory() {
			@Override
			public void setRecipe(IRecipeLayoutBuilder builder, String recipe, IFocusGroup focuses) {
				builder.addInputSlot().add(INGREDIENT_TYPE, INPUT);
				builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 1, 37)
					.setStandardSlotBackground()
					.addIngredients(INGREDIENT_TYPE, unreadableIngredients())
					.addItemStacks(fuels)
					.add(fuelDisplay)
					.add(INGREDIENT_TYPE, fuelDisplay);
				builder.addInvisibleIngredients(RecipeIngredientRole.RENDER_ONLY).addIngredientsUnsafe(fuels);
				builder.addOutputSlot().add(INGREDIENT_TYPE, CATALYST);
			}
		};

		RecipeIngredientSupplier ingredients = IngredientSupplierHelper.getIngredientSupplier(
			FIRST_RECIPE, category, ingredientManager, new ContextMap.Builder().create(new ContextKeySet.Builder().build()),
			EnumSet.of(RecipeIngredientRole.INPUT, RecipeIngredientRole.OUTPUT, RecipeIngredientRole.CRAFTING_STATION)
		);
		EnumMap<RecipeIngredientRole, RecipeIngredientRoleMap> roleMaps = createRoleMaps(ingredientManager);
		roleMaps.values().forEach(roleMap -> roleMap.addRecipe(RECIPE_TYPE, FIRST_RECIPE, ingredients));

		assertEquals(List.of(FIRST_RECIPE), roleMaps.get(RecipeIngredientRole.INPUT)
			.getRecipes(RECIPE_TYPE, typedIngredient(ingredientManager, INPUT)));
		assertEquals(List.of(FIRST_RECIPE), roleMaps.get(RecipeIngredientRole.OUTPUT)
			.getRecipes(RECIPE_TYPE, typedIngredient(ingredientManager, CATALYST)));
		assertEquals(List.of(), ingredients.getIngredients(RecipeIngredientRole.RENDER_ONLY));
	}

	@Test
	void fullIngredientCollectionPreservesRenderOnlyIngredientsWithoutIndexingThem() {
		IIngredientManagerInternal ingredientManager = createIngredientManager();
		TestRecipeCategory category = new TestRecipeCategory() {
			@Override
			public void setRecipe(IRecipeLayoutBuilder builder, String recipe, IFocusGroup focuses) {
				builder.addInputSlot().add(INGREDIENT_TYPE, INPUT);
				builder.addSlot(RecipeIngredientRole.RENDER_ONLY).add(INGREDIENT_TYPE, CATALYST);
			}
		};
		RecipeIngredientSupplier ingredients = IngredientSupplierHelper.getIngredientSupplier(
			FIRST_RECIPE, category, ingredientManager, new ContextMap.Builder().create(new ContextKeySet.Builder().build())
		);
		EnumMap<RecipeIngredientRole, RecipeIngredientRoleMap> roleMaps = createRoleMaps(ingredientManager);
		roleMaps.values().forEach(roleMap -> roleMap.addRecipe(RECIPE_TYPE, FIRST_RECIPE, ingredients));
		ITypedIngredient<TestIngredient> renderOnly = typedIngredient(ingredientManager, CATALYST);
		CraftingStationBuilder craftingStationBuilder = new CraftingStationBuilder(roleMaps.get(RecipeIngredientRole.CRAFTING_STATION));
		RecipeTypeDataMap recipeTypeDataMap = new RecipeTypeDataMap(List.of(category), craftingStationBuilder.build());
		recipeTypeDataMap.get(RECIPE_TYPE).addRecipes(List.of(FIRST_RECIPE));
		InternalRecipeManagerPlugin plugin = new InternalRecipeManagerPlugin(ingredientManager, recipeTypeDataMap, roleMaps);
		Focus<TestIngredient> renderOnlyFocus = new Focus<>(RecipeIngredientRole.RENDER_ONLY, renderOnly);

		assertEquals(List.of(CATALYST), ingredients.getIngredients(RecipeIngredientRole.RENDER_ONLY).stream()
			.map(ITypedIngredient::getIngredient)
			.toList());
		assertEquals(List.of(), plugin.getRecipes(RECIPE_TYPE, renderOnlyFocus));
		assertEquals(List.of(), plugin.getRecipeTypes(renderOnlyFocus));
		assertEquals(List.of(FIRST_RECIPE), roleMaps.get(RecipeIngredientRole.INPUT)
			.getRecipes(RECIPE_TYPE, typedIngredient(ingredientManager, INPUT)));
	}

	@Test
	void recipeDiagnosticsDoNotReadRenderOnlyIngredients() {
		TestRecipeCategory category = new TestRecipeCategory() {
			@Override
			public void setRecipe(IRecipeLayoutBuilder builder, String recipe, IFocusGroup focuses) {
				builder.addInputSlot().add(INGREDIENT_TYPE, INPUT);
				builder.addOutputSlot().add(INGREDIENT_TYPE, CATALYST);
				builder.addSlot(RecipeIngredientRole.RENDER_ONLY)
					.addIngredients(INGREDIENT_TYPE, unreadableIngredients());
			}
		};

		String diagnostic = RecipeDebugUtil.getDebugInfoFromRecipe(FIRST_RECIPE, category, createIngredientManager(), new ContextMap.Builder().create(new ContextKeySet.Builder().build()));

		assertTrue(diagnostic.contains(INPUT.toString()));
		assertTrue(diagnostic.contains(CATALYST.toString()));
	}

	@Test
	void renderOnlySlotsDoNotRestrictSearchableFocusLinks() {
		IIngredientManagerInternal ingredientManager = createIngredientManager();
		TestRecipeCategory category = new TestRecipeCategory() {
			@Override
			public void setRecipe(IRecipeLayoutBuilder builder, String recipe, IFocusGroup focuses) {
				var input = builder.addInputSlot().addIngredients(INGREDIENT_TYPE, List.of(INPUT, CATALYST));
				var output = builder.addOutputSlot().addIngredients(INGREDIENT_TYPE, List.of(INPUT, CATALYST));
				var renderOnly = builder.addSlot(RecipeIngredientRole.RENDER_ONLY)
					.addIngredients(INGREDIENT_TYPE, unreadableIngredients());
				builder.createFocusLink(List.of(input, renderOnly, output));
				builder.createFocusLink(renderOnly);
			}
		};
		RecipeIngredientSupplier ingredients = IngredientSupplierHelper.getIngredientSupplier(
			FIRST_RECIPE, category, ingredientManager, new ContextMap.Builder().create(new ContextKeySet.Builder().build()),
			EnumSet.of(RecipeIngredientRole.INPUT, RecipeIngredientRole.OUTPUT, RecipeIngredientRole.CRAFTING_STATION)
		);

		assertEquals(1, ingredients.getFocusLinks().size());
		assertEquals(Set.of(0), ingredients.getFocusLinks().getFirst().getVisibleIngredientIndexes(
			FocusGroup.EMPTY,
			ingredientManager,
			ingredient -> ingredient.getIngredient().equals(INPUT)
		));
	}

	@Test
	void ingredientCollectionTracksOnlySelectedRoles() {
		List<Set<RecipeIngredientRole>> selections = List.of(
			Set.of(),
			Set.of(RecipeIngredientRole.INPUT),
			Set.of(RecipeIngredientRole.OUTPUT),
			Set.of(RecipeIngredientRole.CRAFTING_STATION),
			Set.of(RecipeIngredientRole.RENDER_ONLY),
			Set.of(RecipeIngredientRole.INPUT, RecipeIngredientRole.OUTPUT),
			EnumSet.allOf(RecipeIngredientRole.class)
		);
		for (Set<RecipeIngredientRole> roles : selections) {
			TestRecipeCategory category = new TestRecipeCategory() {
				@Override
				public void setRecipe(IRecipeLayoutBuilder builder, String recipe, IFocusGroup focuses) {
					for (RecipeIngredientRole role : RecipeIngredientRole.values()) {
						List<TestIngredient> ingredients;
						if (roles.contains(role)) {
							ingredients = List.of(INPUT);
						} else {
							ingredients = unreadableIngredients();
						}
						builder.addSlot(role).addIngredients(INGREDIENT_TYPE, ingredients);
						builder.addInvisibleIngredients(role).addIngredients(INGREDIENT_TYPE, ingredients);
					}
				}
			};
			RecipeIngredientSupplier ingredients = IngredientSupplierHelper.getIngredientSupplier(
				FIRST_RECIPE, category, createIngredientManager(), new ContextMap.Builder().create(new ContextKeySet.Builder().build()), roles
			);
			for (RecipeIngredientRole role : RecipeIngredientRole.values()) {
				List<TestIngredient> expected;
				if (roles.contains(role)) {
					expected = List.of(INPUT, INPUT);
				} else {
					expected = List.of();
				}
				assertEquals(expected, ingredients.getIngredients(role).stream().map(ITypedIngredient::getIngredient).toList(), role.name());
			}
		}
	}

	@Test
	void selectedFocusLinksPreserveBlankPositions() {
		IIngredientManagerInternal ingredientManager = createIngredientManager();
		IngredientSupplierBuilder builder = new IngredientSupplierBuilder(ingredientManager, new ContextMap.Builder().create(new ContextKeySet.Builder().build()), Set.of(RecipeIngredientRole.INPUT, RecipeIngredientRole.OUTPUT));
		var input = builder.addInputSlot().addIngredients(INGREDIENT_TYPE, Arrays.asList(null, INPUT));
		var output = builder.addOutputSlot().addIngredients(INGREDIENT_TYPE, Arrays.asList(CATALYST, null));
		var renderOnly = builder.addSlot(RecipeIngredientRole.RENDER_ONLY).addIngredients(INGREDIENT_TYPE, unreadableIngredients());
		builder.createFocusLink(input, output, renderOnly);
		builder.createFocusLink(renderOnly);
		RecipeIngredientSupplier ingredients = builder.buildIngredientSupplier();

		assertEquals(List.of(INPUT), ingredients.getIngredients(RecipeIngredientRole.INPUT).stream()
			.map(ITypedIngredient::getIngredient).toList());
		assertTrue(ingredients.getIngredients(RecipeIngredientRole.RENDER_ONLY).isEmpty());
		assertEquals(1, ingredients.getFocusLinks().size());
		assertEquals(Set.of(1), ingredients.getFocusLinks().getFirst().getVisibleIngredientIndexes(
			FocusGroup.EMPTY, ingredientManager, ingredient -> ingredient.getIngredient().equals(INPUT)));
	}

	private static <T> List<T> unreadableIngredients() {
		return new AbstractList<>() {
			@Override
			public T get(int index) {
				throw new AssertionError("Unselected ingredient roles must not be read");
			}

			@Override
			public int size() {
				return 100_000;
			}
		};
	}

	@Test
	public void craftingStationFocusDoesNotDuplicateRecipeWhenIngredientIsAlsoCraftingStation() {
		// Setup: one recipe has the focused ingredient as a crafting-station role and as the category catalyst.
		PluginFixture fixture = createFixture(List.of(FIRST_RECIPE), List.of(CATALYST));
		ITypedIngredient<TestIngredient> catalyst = fixture.typedIngredient(CATALYST);
		fixture.addRecipeIngredients(FIRST_RECIPE, RecipeIngredientRole.CRAFTING_STATION, catalyst);

		// Operation: request recipes for the catalyst through the internal focused lookup path.
		List<String> recipes = fixture.getRecipes(RecipeIngredientRole.CRAFTING_STATION, catalyst);

		// Assertions: the recipe is returned once, not once from ingredients and once from catalysts.
		assertEquals(List.of(FIRST_RECIPE), recipes);
	}

	@Test
	public void craftingStationFocusReturnsAllCategoryRecipesForCatalyst() {
		// Setup: the focused ingredient is registered as the category catalyst but is not a recipe ingredient.
		PluginFixture fixture = createFixture(List.of(FIRST_RECIPE, SECOND_RECIPE), List.of(CATALYST));
		ITypedIngredient<TestIngredient> catalyst = fixture.typedIngredient(CATALYST);

		// Operation: request recipes for the catalyst through the internal focused lookup path.
		List<String> recipes = fixture.getRecipes(RecipeIngredientRole.CRAFTING_STATION, catalyst);

		// Assertions: catalyst lookup expands to all recipes in the category.
		assertEquals(List.of(FIRST_RECIPE, SECOND_RECIPE), recipes);
	}

	@Test
	public void craftingStationFocusReturnsDirectRecipeForNonCatalyst() {
		// Setup: the focused ingredient appears in a recipe but is not registered as a category catalyst.
		PluginFixture fixture = createFixture(List.of(FIRST_RECIPE, SECOND_RECIPE), List.of(CATALYST));
		ITypedIngredient<TestIngredient> input = fixture.typedIngredient(INPUT);
		fixture.addRecipeIngredients(FIRST_RECIPE, RecipeIngredientRole.CRAFTING_STATION, input);

		// Operation: request recipes for the non-catalyst ingredient.
		List<String> recipes = fixture.getRecipes(RecipeIngredientRole.CRAFTING_STATION, input);

		// Assertions: direct recipe matches still work without expanding to the whole category.
		assertEquals(List.of(FIRST_RECIPE), recipes);
	}

	@Test
	public void inputFocusDoesNotUseCraftingStationCatalystExpansion() {
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
	public void craftingStationFocusMergesDirectAndCatalystRecipesWithoutDuplicates() {
		// Setup: two recipes match directly and all three recipes match through the category catalyst.
		PluginFixture fixture = createFixture(List.of(FIRST_RECIPE, SECOND_RECIPE, THIRD_RECIPE), List.of(CATALYST));
		ITypedIngredient<TestIngredient> catalyst = fixture.typedIngredient(CATALYST);
		fixture.addRecipeIngredients(FIRST_RECIPE, RecipeIngredientRole.CRAFTING_STATION, catalyst);
		fixture.addRecipeIngredients(SECOND_RECIPE, RecipeIngredientRole.CRAFTING_STATION, catalyst);

		// Operation: request recipes for the catalyst through the internal focused lookup path.
		List<String> recipes = fixture.getRecipes(RecipeIngredientRole.CRAFTING_STATION, catalyst);

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
		RecipeIngredientRoleMap roleMap = createRoleMaps(ingredientManager).get(RecipeIngredientRole.INPUT);
		ITypedIngredient<TestIngredient> input = typedIngredient(ingredientManager, INPUT);
		ITypedIngredient<OtherTestIngredient> otherInput = ingredientManager.createTypedIngredient(
				OTHER_INGREDIENT_TYPE,
				new OtherTestIngredient(INPUT.number()),
				false
			)
			.orElseThrow();
		RecipeIngredientSupplier ingredientSupplier = new RecipeIngredientSupplier(
			Map.of(RecipeIngredientRole.INPUT, List.of(new SlotIngredient<>(input)))
		);
		roleMap.addRecipe(RECIPE_TYPE, FIRST_RECIPE, ingredientSupplier);

		assertEquals(List.of(), roleMap.getRecipes(RECIPE_TYPE, otherInput));
	}

	@Test
	public void equalIngredientTypesShareRecipeUidRows() {
		IIngredientType<TestIngredient> equalButDistinctType = new TestIngredientType("test");
		assertNotSame(INGREDIENT_TYPE, equalButDistinctType);

		IIngredientManager ingredientManager = createIngredientManager();
		RecipeIngredientRoleMap roleMap = createRoleMaps(ingredientManager).get(RecipeIngredientRole.INPUT);
		ITypedIngredient<TestIngredient> input = typedIngredient(ingredientManager, INPUT);
		ITypedIngredient<TestIngredient> equalTypeInput = ingredientManager.createTypedIngredient(equalButDistinctType, INPUT, false)
			.orElseThrow();
		RecipeIngredientSupplier ingredientSupplier = new RecipeIngredientSupplier(
			Map.of(RecipeIngredientRole.INPUT, List.of(new SlotIngredient<>(input)))
		);
		roleMap.addRecipe(RECIPE_TYPE, FIRST_RECIPE, ingredientSupplier);

		assertEquals(List.of(FIRST_RECIPE), roleMap.getRecipes(RECIPE_TYPE, equalTypeInput));
	}

	@Test
	public void equalExactAndGroupingUidsRemainSeparateLookupNamespaces() {
		IIngredientManager ingredientManager = createIngredientManager();
		RecipeIngredientRoleMap roleMap = createRoleMaps(ingredientManager).get(RecipeIngredientRole.INPUT);
		ITypedIngredient<TestIngredient> catalyst = typedIngredient(ingredientManager, CATALYST);
		ITypedIngredient<TestIngredient> input = typedIngredient(ingredientManager, INPUT);
		RecipeIngredientSupplier ingredientSupplier = new RecipeIngredientSupplier(
			Map.of(RecipeIngredientRole.INPUT, List.of(new SlotIngredient<>(catalyst)))
		);
		roleMap.addRecipe(RECIPE_TYPE, FIRST_RECIPE, ingredientSupplier);

		assertEquals(List.of(), roleMap.getRecipes(RECIPE_TYPE, input));
	}

	private static PluginFixture createFixture(List<String> recipes, List<TestIngredient> catalysts) {
		IIngredientManager ingredientManager = createIngredientManager();
		EnumMap<RecipeIngredientRole, RecipeIngredientRoleMap> roleMaps = createRoleMaps(ingredientManager);
		TestRecipeCategory recipeCategory = new TestRecipeCategory();
		List<ITypedIngredient<?>> typedCatalysts = catalysts.stream()
			.<ITypedIngredient<?>>map(ingredient -> typedIngredient(ingredientManager, ingredient))
			.toList();
		CraftingStationBuilder craftingStationBuilder = new CraftingStationBuilder(roleMaps.get(RecipeIngredientRole.CRAFTING_STATION));
		craftingStationBuilder.addCategoryCatalysts(recipeCategory, typedCatalysts);

		RecipeTypeDataMap recipeTypeDataMap = new RecipeTypeDataMap(
			List.of(recipeCategory),
			craftingStationBuilder.build()
		);
		recipeTypeDataMap.get(RECIPE_TYPE).addRecipes(recipes);

		InternalRecipeManagerPlugin plugin = new InternalRecipeManagerPlugin(
			ingredientManager,
			recipeTypeDataMap,
			roleMaps
		);
		return new PluginFixture(ingredientManager, roleMaps, plugin);
	}

	private static EnumMap<RecipeIngredientRole, RecipeIngredientRoleMap> createRoleMaps(IIngredientManager ingredientManager) {
		Comparator<IRecipeType<?>> recipeTypeComparator = Comparator.comparing(recipeType -> recipeType.getUid().toString());
		EnumMap<RecipeIngredientRole, RecipeIngredientRoleMap> roleMaps = new EnumMap<>(RecipeIngredientRole.class);
		for (RecipeIngredientRole role : EnumSet.of(RecipeIngredientRole.INPUT, RecipeIngredientRole.OUTPUT, RecipeIngredientRole.CRAFTING_STATION)) {
			roleMaps.put(role, new RecipeIngredientRoleMap(recipeTypeComparator, ingredientManager, role));
		}
		return roleMaps;
	}

	private static IIngredientManagerInternal createIngredientManager() {
		SubtypeManager subtypeManager = new SubtypeManager(new SubtypeInterpreters());
		IngredientManagerBuilder builder = new IngredientManagerBuilder(
			subtypeManager,
			DummyColorHelper.INSTANCE,
			new ContextMap.Builder().create(new ContextKeySet.Builder().build())
		);
		builder.register(
			INGREDIENT_TYPE,
			List.of(CATALYST, INPUT),
			new TestIngredientHelper(),
			new TestIngredientRenderer(),
			Codec.INT.xmap(TestIngredient::new, TestIngredient::number)
		);
		builder.register(
			OTHER_INGREDIENT_TYPE,
			List.of(new OtherTestIngredient(INPUT.number())),
			new OtherTestIngredientHelper(),
			new OtherTestIngredientRenderer(),
			Codec.INT.xmap(OtherTestIngredient::new, OtherTestIngredient::number)
		);
		return builder.build();
	}

	private static ITypedIngredient<TestIngredient> typedIngredient(IIngredientManager ingredientManager, TestIngredient ingredient) {
		return ingredientManager.createTypedIngredient(INGREDIENT_TYPE, ingredient, false)
			.orElseThrow();
	}

	private record PluginFixture(
		IIngredientManager ingredientManager,
		EnumMap<RecipeIngredientRole, RecipeIngredientRoleMap> roleMaps,
		InternalRecipeManagerPlugin plugin
	) {
		private ITypedIngredient<TestIngredient> typedIngredient(TestIngredient ingredient) {
			return InternalRecipeManagerPluginTest.typedIngredient(ingredientManager, ingredient);
		}

		@SafeVarargs
		private final void addRecipeIngredients(String recipe, RecipeIngredientRole role, ITypedIngredient<?>... ingredients) {
			List<SlotIngredient<?>> slotIngredients = Arrays.stream(ingredients)
				.<SlotIngredient<?>>map(SlotIngredient::new)
				.toList();
			RecipeIngredientSupplier ingredientSupplier = new RecipeIngredientSupplier(Map.of(role, slotIngredients));
			roleMaps.get(role).addRecipe(RECIPE_TYPE, recipe, ingredientSupplier);
		}

		private List<String> getRecipes(RecipeIngredientRole role, ITypedIngredient<TestIngredient> ingredient) {
			Focus<TestIngredient> focus = new Focus<>(role, ingredient);
			return plugin.getRecipes(RECIPE_TYPE, focus);
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
		public Object getUid(TestIngredient ingredient, UidContext context) {
			return ingredient.number();
		}

		@Override
		public Object getGroupingUid(TestIngredient ingredient) {
			return CATALYST.number();
		}

		@Override
		public Identifier getIdentifier(TestIngredient ingredient) {
			return Identifier.fromNamespaceAndPath("test", Integer.toString(ingredient.number()));
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
		public void render(GuiGraphicsExtractor guiGraphics, TestIngredient ingredient) {

		}

		@Override
		@Deprecated(since = "29.31.0", forRemoval = true)
		@SuppressWarnings("removal")
		public List<Component> getTooltip(TestIngredient ingredient, TooltipFlag tooltipFlag) {
			return getTooltip(ingredient, Item.TooltipContext.EMPTY, null, tooltipFlag);
		}

		@Override
		@Deprecated(since = "29.31.0", forRemoval = true)
		@SuppressWarnings("removal")
		public void getTooltip(ITooltipBuilder tooltip, TestIngredient ingredient, TooltipFlag tooltipFlag) {
			getTooltip(tooltip, ingredient, Item.TooltipContext.EMPTY, null, tooltipFlag);
		}

		@Override
		public List<Component> getTooltip(TestIngredient ingredient, Item.TooltipContext tooltipContext, @Nullable Player player, TooltipFlag tooltipFlag) {
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
		public Object getUid(OtherTestIngredient ingredient, UidContext context) {
			return ingredient.number();
		}

		@Override
		public Identifier getIdentifier(OtherTestIngredient ingredient) {
			return Identifier.fromNamespaceAndPath("test_other", Integer.toString(ingredient.number()));
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
		public void render(GuiGraphicsExtractor guiGraphics, OtherTestIngredient ingredient) {
		}

		@Override
		@Deprecated(since = "29.31.0", forRemoval = true)
		@SuppressWarnings("removal")
		public List<Component> getTooltip(OtherTestIngredient ingredient, TooltipFlag tooltipFlag) {
			return getTooltip(ingredient, Item.TooltipContext.EMPTY, null, tooltipFlag);
		}

		@Override
		@Deprecated(since = "29.31.0", forRemoval = true)
		@SuppressWarnings("removal")
		public void getTooltip(ITooltipBuilder tooltip, OtherTestIngredient ingredient, TooltipFlag tooltipFlag) {
			getTooltip(tooltip, ingredient, Item.TooltipContext.EMPTY, null, tooltipFlag);
		}

		@Override
		public List<Component> getTooltip(OtherTestIngredient ingredient, Item.TooltipContext tooltipContext, @Nullable Player player, TooltipFlag tooltipFlag) {
			return List.of(Component.literal(Integer.toString(ingredient.number())));
		}
	}

	private static class TestRecipeCategory implements IRecipeCategory<String> {
		@Override
		public IRecipeType<String> getRecipeType() {
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
}
