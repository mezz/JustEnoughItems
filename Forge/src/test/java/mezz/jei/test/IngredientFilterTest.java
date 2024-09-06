package mezz.jei.test;

import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IEditModeConfig;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.gui.filter.FilterTextSource;
import mezz.jei.gui.filter.IFilterTextSource;
import mezz.jei.gui.ingredients.IListElementInfo;
import mezz.jei.gui.ingredients.IngredientFilter;
import mezz.jei.gui.ingredients.IngredientListElementFactory;
import mezz.jei.gui.ingredients.ListElementInfoTooltip;
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

import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;
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
		TestPlugin testPlugin = new TestPlugin();

		SubtypeInterpreters subtypeInterpreters = new SubtypeInterpreters();
		SubtypeManager subtypeManager = new SubtypeManager(subtypeInterpreters);

		IColorHelper colorHelper = new TestColorHelper();
		IngredientManagerBuilder ingredientManagerBuilder = new IngredientManagerBuilder(subtypeManager, colorHelper);
		testPlugin.registerIngredients(ingredientManagerBuilder);

		IngredientBlacklistInternal blacklist = new IngredientBlacklistInternal();
		this.modIdHelper = new TestModIdHelper();
		IClientConfig clientConfig = new TestClientConfig(false);
		this.ingredientManager = ingredientManagerBuilder.build();

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
			Comparator.comparingInt(Object::hashCode),
			baseList,
			modIdHelper,
			ingredientVisibility,
			colorHelper,
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
	public void testAddingAndRemovingIngredientsWithTooltipStrings() {
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(ingredientManager);
		Assertions.assertNotNull(ingredientVisibility);
		Assertions.assertNotNull(filterTextSource);
		Assertions.assertNotNull(modIdHelper);

		List<TestIngredient> ingredients = createIngredients();
		TestIngredient testIngredient = ingredients.get(0);
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

		IListElementInfo<?> elementInfo = baseList.get(0);
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
		ListElementInfoTooltip tooltip = new ListElementInfoTooltip();
		ingredientRenderer.getTooltip(tooltip, testIngredient, TooltipFlag.Default.NORMAL);
		return tooltip.getStrings();
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
