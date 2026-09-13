package mezz.jei.test;

import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IEditModeConfig;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.api.search.ISearchStorageBuilder;
import mezz.jei.api.search.ISearchStorageBuilderFactory;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.search.GeneralizedSuffixTreeSearchStorage;
import mezz.jei.common.search.SearchStorageBuilderAdapter;
import mezz.jei.gui.filter.FilterTextSource;
import mezz.jei.gui.filter.IFilterTextSource;
import mezz.jei.gui.ingredients.IListElementInfo;
import mezz.jei.gui.ingredients.IngredientFilter;
import mezz.jei.gui.ingredients.IngredientListElementFactory;
import mezz.jei.gui.ingredients.ListElementInfo;
import mezz.jei.library.config.EditModeConfig;
import mezz.jei.library.ingredients.IngredientBlacklistInternal;
import mezz.jei.library.ingredients.IngredientVisibility;
import mezz.jei.library.ingredients.subtypes.SubtypeInterpreters;
import mezz.jei.library.ingredients.subtypes.SubtypeManager;
import mezz.jei.library.load.registration.IngredientManagerBuilder;
import mezz.jei.test.lib.TestClientConfig;
import mezz.jei.test.lib.TestClientToggleState;
import mezz.jei.test.lib.TestColorHelper;
import mezz.jei.test.lib.TestIngredient;
import mezz.jei.test.lib.TestIngredientFilterConfig;
import mezz.jei.test.lib.TestIngredientHelper;
import mezz.jei.test.lib.TestModIdHelper;
import mezz.jei.test.lib.TestPlugin;
import net.minecraft.network.chat.Component;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class IngredientFilterTest {
	private static final int EXTRA_INGREDIENT_COUNT = 5;
	@Nullable
	private IIngredientManager ingredientManager;
	@Nullable
	private IngredientFilter ingredientFilter;
	@Nullable
	private IIngredientVisibility ingredientVisibility;
	@Nullable
	private List<IListElementInfo<?>> baseList;
	@Nullable
	private EditModeConfig editModeConfig;
	@Nullable
	private FilterTextSource filterTextSource;
	@Nullable
	private IModIdHelper modIdHelper;

	@BeforeEach
	public void setup() {
		setup(false);
	}

	private void setup(boolean lowMemorySlowSearchEnabled) {
		TestPlugin testPlugin = new TestPlugin();

		SubtypeInterpreters subtypeInterpreters = new SubtypeInterpreters();
		SubtypeManager subtypeManager = new SubtypeManager(subtypeInterpreters);

		IColorHelper colorHelper = new TestColorHelper();
		IngredientManagerBuilder ingredientManagerBuilder = new IngredientManagerBuilder(
			subtypeManager,
			colorHelper,
			new ContextMap.Builder().create(new ContextKeySet.Builder().build())
		);
		testPlugin.registerIngredients(ingredientManagerBuilder);
		this.ingredientManager = ingredientManagerBuilder.build();

		IngredientBlacklistInternal blacklist = new IngredientBlacklistInternal(ingredientManager);
		this.modIdHelper = new TestModIdHelper();
		IClientConfig clientConfig = new TestClientConfig(lowMemorySlowSearchEnabled);

		this.baseList = IngredientListElementFactory.createBaseList(ingredientManager, modIdHelper);

		this.editModeConfig = new EditModeConfig(new NullSerializer(), ingredientManager);

		IClientToggleState toggleState = new TestClientToggleState();

		TestIngredientFilterConfig ingredientFilterConfig = new TestIngredientFilterConfig();
		this.ingredientVisibility = new IngredientVisibility(blacklist, toggleState, editModeConfig, ingredientManager);
		this.filterTextSource = new FilterTextSource();
		this.ingredientFilter = new IngredientFilter(
			filterTextSource,
			clientConfig,
			ingredientFilterConfig,
			ingredientManager,
			ingredients -> Comparator.comparingInt(Object::hashCode),
			baseList,
			modIdHelper,
			ingredientVisibility,
			colorHelper,
			new ISearchStorageBuilderFactory() {
				@Override
				public <T> ISearchStorageBuilder<T> create() {
					return new SearchStorageBuilderAdapter<>(new GeneralizedSuffixTreeSearchStorage<>());
				}
			},
			toggleState
		);

		this.ingredientManager.registerIngredientListener(blacklist);
		this.ingredientManager.registerIngredientListener(ingredientFilter);

		this.ingredientVisibility.registerListener(this.ingredientFilter);
	}

	@Test
	public void testSetup() {
		Assertions.assertNotNull(ingredientFilter);

		List<?> ingredientList = ingredientFilter.getElements();
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, ingredientList.size());
	}

	@Test
	public void testAddingAndRemovingIngredients() {
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(ingredientManager);
		Assertions.assertNotNull(ingredientVisibility);
		Assertions.assertNotNull(filterTextSource);
		Assertions.assertNotNull(modIdHelper);

		List<TestIngredient> ingredients = createIngredients();

		addIngredients(ingredientFilter, filterTextSource, ingredientVisibility, ingredientManager, modIdHelper, ingredients);
		removeIngredients(ingredientFilter, filterTextSource, ingredientVisibility, ingredientManager, modIdHelper, ingredients);
		addIngredients(ingredientFilter, filterTextSource, ingredientVisibility, ingredientManager, modIdHelper, ingredients);
	}

	@Test
	public void testHidingIngredientsInMultipleContexts() {
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(ingredientManager);
		Assertions.assertNotNull(ingredientVisibility);
		Assertions.assertNotNull(filterTextSource);
		Assertions.assertNotNull(modIdHelper);

		List<TestIngredient> ingredients = createIngredients();
		addIngredients(ingredientFilter, filterTextSource, ingredientVisibility, ingredientManager, modIdHelper, ingredients);

		ingredientVisibility.hideIngredients(
			TestIngredient.TYPE,
			ingredients,
			Set.of(
				UidContext.Ingredient,
				UidContext.Recipe
			)
		);

		filterTextSource.setFilterText("");
		List<TestIngredient> filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, filteredIngredients.size());
		for (TestIngredient ingredient : ingredients) {
			Assertions.assertFalse(filteredIngredients.contains(ingredient));
			Assertions.assertFalse(ingredientVisibility.isIngredientVisible(
				TestIngredient.TYPE,
				ingredient,
				UidContext.Ingredient
			));
			Assertions.assertFalse(ingredientVisibility.isIngredientVisible(
				TestIngredient.TYPE,
				ingredient,
				UidContext.Recipe
			));
		}

		Collection<TestIngredient> registeredIngredients = ingredientManager.getAllIngredients(TestIngredient.TYPE);
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT, registeredIngredients.size());
		Assertions.assertTrue(registeredIngredients.containsAll(ingredients));

		ingredientFilter.updateHidden();
		filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, filteredIngredients.size());

		ingredientVisibility.unhideIngredients(
			TestIngredient.TYPE,
			ingredients,
			Set.of(UidContext.Ingredient)
		);
		filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT, filteredIngredients.size());
		for (TestIngredient ingredient : ingredients) {
			Assertions.assertTrue(ingredientVisibility.isIngredientVisible(TestIngredient.TYPE, ingredient));
			Assertions.assertFalse(ingredientVisibility.isIngredientVisible(
				TestIngredient.TYPE,
				ingredient,
				UidContext.Recipe
			));
		}
	}

	@Test
	public void testUnhidingIngredientsWithLowMemorySearch() {
		setup(true);
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(ingredientManager);
		Assertions.assertNotNull(ingredientVisibility);
		Assertions.assertNotNull(filterTextSource);
		Assertions.assertNotNull(modIdHelper);

		List<TestIngredient> ingredients = createIngredients();
		addIngredients(ingredientFilter, filterTextSource, ingredientVisibility, ingredientManager, modIdHelper, ingredients);
		ingredientVisibility.hideIngredients(
			TestIngredient.TYPE,
			ingredients,
			Set.of(UidContext.Ingredient)
		);
		Assertions.assertEquals(
			TestPlugin.BASE_INGREDIENT_COUNT,
			ingredientFilter.getFilteredIngredients(TestIngredient.TYPE).size()
		);

		ingredientVisibility.unhideIngredients(
			TestIngredient.TYPE,
			ingredients,
			Set.of(UidContext.Ingredient)
		);
		Assertions.assertEquals(
			TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT,
			ingredientFilter.getFilteredIngredients(TestIngredient.TYPE).size()
		);
	}

	@Test
	public void testRecipeVisibilityUsesRecipeUid() {
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(ingredientManager);
		Assertions.assertNotNull(ingredientVisibility);
		Assertions.assertNotNull(filterTextSource);
		Assertions.assertNotNull(modIdHelper);

		List<TestIngredient> ingredients = createIngredients();
		addIngredients(ingredientFilter, filterTextSource, ingredientVisibility, ingredientManager, modIdHelper, ingredients);

		TestIngredient hiddenIngredient = ingredients.getFirst();
		TestIngredient recipeEquivalentIngredient = ingredients.get(2);
		TestIngredient differentRecipeIngredient = ingredients.get(1);
		ingredientVisibility.hideIngredients(
			TestIngredient.TYPE,
			Set.of(hiddenIngredient),
			Set.of(UidContext.Recipe)
		);

		Assertions.assertTrue(ingredientVisibility.isIngredientVisible(
			TestIngredient.TYPE,
			recipeEquivalentIngredient,
			UidContext.Ingredient
		));
		Assertions.assertFalse(ingredientVisibility.isIngredientVisible(
			TestIngredient.TYPE,
			recipeEquivalentIngredient,
			UidContext.Recipe
		));
		Assertions.assertTrue(ingredientVisibility.isIngredientVisible(
			TestIngredient.TYPE,
			differentRecipeIngredient,
			UidContext.Recipe
		));

		List<TestIngredient> filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT, filteredIngredients.size());

		ingredientVisibility.unhideIngredients(
			TestIngredient.TYPE,
			Set.of(hiddenIngredient),
			Set.of(UidContext.Recipe)
		);
		Assertions.assertTrue(ingredientVisibility.isIngredientVisible(
			TestIngredient.TYPE,
			recipeEquivalentIngredient,
			UidContext.Recipe
		));
	}

	@Test
	public void testAddingAndRemovingIngredientsWithTooltipStrings() {
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(ingredientManager);
		Assertions.assertNotNull(ingredientVisibility);
		Assertions.assertNotNull(filterTextSource);
		Assertions.assertNotNull(modIdHelper);

		List<TestIngredient> ingredients = createIngredients();
		TestIngredient testIngredient = ingredients.getFirst();
		IIngredientRenderer<TestIngredient> ingredientRenderer = ingredientManager.getIngredientRenderer(TestIngredient.TYPE);
		Set<String> tooltipStrings = getTooltipStrings(ingredientRenderer, testIngredient);

		addIngredients(ingredientFilter, filterTextSource, ingredientVisibility, ingredientManager, modIdHelper, ingredients);
		for (String tooltipString : tooltipStrings) {
			filterTextSource.setFilterText(tooltipString);
			List<TestIngredient> filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
			Assertions.assertTrue(filteredIngredients.contains(testIngredient), tooltipString);
		}

		removeIngredients(ingredientFilter, filterTextSource, ingredientVisibility, ingredientManager, modIdHelper, ingredients);
		for (String tooltipString : tooltipStrings) {
			filterTextSource.setFilterText(tooltipString);
			List<TestIngredient> filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
			Assertions.assertFalse(filteredIngredients.contains(testIngredient), tooltipString);
		}

		addIngredients(ingredientFilter, filterTextSource, ingredientVisibility, ingredientManager, modIdHelper, ingredients);
		for (String tooltipString : tooltipStrings) {
			filterTextSource.setFilterText(tooltipString);
			List<TestIngredient> filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
			Assertions.assertTrue(filteredIngredients.contains(testIngredient), tooltipString);
		}
	}

	@Test
	public void testConfigBlacklist() {
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(baseList);
		Assertions.assertNotNull(editModeConfig);

		IListElementInfo<?> elementInfo = baseList.getFirst();
		ITypedIngredient<?> typedIngredient = elementInfo.getTypedIngredient();
		@SuppressWarnings("unchecked")
		ITypedIngredient<TestIngredient> blacklistedIngredient = (ITypedIngredient<TestIngredient>) typedIngredient;
		TestIngredientHelper testIngredientHelper = new TestIngredientHelper();
		editModeConfig.addIngredientToConfigBlacklist(blacklistedIngredient, IEditModeConfig.HideMode.SINGLE, testIngredientHelper);

		ingredientFilter.updateHidden();

		List<?> ingredientList = ingredientFilter.getElements();
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT - 1, ingredientList.size());
	}

	public static Set<String> getTooltipStrings(IIngredientRenderer<TestIngredient> ingredientRenderer, TestIngredient testIngredient) {
		List<Component> components = ingredientRenderer.getTooltip(testIngredient, Item.TooltipContext.EMPTY, null, TooltipFlag.Default.NORMAL);
		return ListElementInfo.getStrings(components);
	}

	public static List<TestIngredient> createIngredients() {
		List<TestIngredient> ingredients = new ArrayList<>();
		for (int i = TestPlugin.BASE_INGREDIENT_COUNT; i < TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT; i++) {
			ingredients.add(new TestIngredient(i));
		}
		Assertions.assertEquals(EXTRA_INGREDIENT_COUNT, ingredients.size());
		return ingredients;
	}

	private static void addIngredients(
		IngredientFilter ingredientFilter,
		IFilterTextSource filterTextSource,
		IIngredientVisibility ingredientVisibility,
		IIngredientManager ingredientManager,
		IModIdHelper modIdHelper,
		List<TestIngredient> ingredientsToAdd
	) {
		List<IListElementInfo<TestIngredient>> listToAdd = IngredientListElementFactory.createTestList(ingredientManager, TestIngredient.TYPE, ingredientsToAdd, modIdHelper);
		Assertions.assertEquals(EXTRA_INGREDIENT_COUNT, listToAdd.size());

		ingredientManager.addIngredientsAtRuntime(TestIngredient.TYPE, ingredientsToAdd);

		Collection<TestIngredient> testIngredients = ingredientManager.getAllIngredients(TestIngredient.TYPE);
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT, testIngredients.size());
		for (TestIngredient testIngredient : ingredientsToAdd) {
			Assertions.assertTrue(testIngredients.contains(testIngredient));
		}

		filterTextSource.setFilterText("");
		List<TestIngredient> filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT, filteredIngredients.size());
		for (TestIngredient testIngredient : filteredIngredients) {
			Assertions.assertTrue(testIngredients.contains(testIngredient));
		}

		for (TestIngredient ingredient : ingredientsToAdd) {
			Assertions.assertTrue(ingredientVisibility.isIngredientVisible(TestIngredient.TYPE, ingredient));
		}
	}

	private static void removeIngredients(
		IngredientFilter ingredientFilter,
		IFilterTextSource filterTextSource,
		IIngredientVisibility ingredientVisibility,
		IIngredientManager ingredientManager,
		IModIdHelper modIdHelper,
		List<TestIngredient> ingredientsToRemove
	) {
		List<IListElementInfo<TestIngredient>> listToRemove = IngredientListElementFactory.createTestList(ingredientManager, TestIngredient.TYPE, ingredientsToRemove, modIdHelper);
		Assertions.assertEquals(EXTRA_INGREDIENT_COUNT, listToRemove.size());

		ingredientManager.removeIngredientsAtRuntime(TestIngredient.TYPE, ingredientsToRemove);

		filterTextSource.setFilterText("");
		List<TestIngredient> filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, filteredIngredients.size());
		for (TestIngredient testIngredient : filteredIngredients) {
			Assertions.assertFalse(ingredientsToRemove.contains(testIngredient));
		}

		Collection<TestIngredient> testIngredients = ingredientManager.getAllIngredients(TestIngredient.TYPE);
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, testIngredients.size());
		for (TestIngredient testIngredient : testIngredients) {
			Assertions.assertFalse(ingredientsToRemove.contains(testIngredient));
		}

		for (TestIngredient ingredient : ingredientsToRemove) {
			Assertions.assertFalse(ingredientVisibility.isIngredientVisible(TestIngredient.TYPE, ingredient));
			for (UidContext context : UidContext.values()) {
				Assertions.assertFalse(ingredientVisibility.isIngredientVisible(TestIngredient.TYPE, ingredient, context));
			}
		}

		ingredientVisibility.unhideIngredients(
			TestIngredient.TYPE,
			ingredientsToRemove,
			Set.of(UidContext.values())
		);
		for (TestIngredient ingredient : ingredientsToRemove) {
			for (UidContext context : UidContext.values()) {
				Assertions.assertFalse(ingredientVisibility.isIngredientVisible(TestIngredient.TYPE, ingredient, context));
			}
		}
	}

	private static class NullSerializer implements EditModeConfig.ISerializer {
		@Override
		public void initialize(EditModeConfig config) {

		}

		@Override
		public void save(EditModeConfig config) {

		}

		@Override
		public void load(EditModeConfig config) {

		}
	}
}
