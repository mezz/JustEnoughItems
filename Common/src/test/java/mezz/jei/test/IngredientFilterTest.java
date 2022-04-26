package mezz.jei.test;

import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.common.config.EditModeConfig;
import mezz.jei.core.config.IClientConfig;
import mezz.jei.common.config.IEditModeConfig;
import mezz.jei.core.config.IWorldConfig;
import mezz.jei.core.config.IngredientBlacklistType;
import mezz.jei.common.gui.ingredients.IListElement;
import mezz.jei.common.filter.FilterTextSource;
import mezz.jei.common.filter.IFilterTextSource;
import mezz.jei.common.ingredients.IListElementInfo;
import mezz.jei.common.ingredients.IIngredientSorter;
import mezz.jei.common.ingredients.IngredientBlacklistInternal;
import mezz.jei.common.ingredients.IngredientFilter;
import mezz.jei.common.ingredients.IngredientListElementFactory;
import mezz.jei.common.ingredients.RegisteredIngredients;
import mezz.jei.common.ingredients.IngredientManager;
import mezz.jei.common.ingredients.IngredientVisibility;
import mezz.jei.common.load.registration.RegisteredIngredientsBuilder;
import mezz.jei.common.ingredients.IngredientInfo;
import mezz.jei.common.ingredients.subtypes.SubtypeManager;
import mezz.jei.common.load.registration.SubtypeRegistration;
import mezz.jei.test.lib.TestClientConfig;
import mezz.jei.test.lib.TestIngredient;
import mezz.jei.test.lib.TestIngredientFilterConfig;
import mezz.jei.test.lib.TestIngredientHelper;
import mezz.jei.test.lib.TestModIdHelper;
import mezz.jei.test.lib.TestPlugin;
import mezz.jei.test.lib.TestWorldConfig;
import mezz.jei.common.util.Translator;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.TooltipFlag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class IngredientFilterTest {
	private static final int EXTRA_INGREDIENT_COUNT = 5;
	@Nullable
	private RegisteredIngredients registeredIngredients;
	@Nullable
	private IIngredientManager ingredientManager;
	@Nullable
	private IngredientFilter ingredientFilter;
	@Nullable
	private IIngredientVisibility ingredientVisibility;
	@Nullable
	private NonNullList<IListElement<?>> baseList;
	@Nullable
	private IEditModeConfig editModeConfig;
	@Nullable
	private FilterTextSource filterTextSource;

	@BeforeEach
	public void setup() {
		TestPlugin testPlugin = new TestPlugin();

		SubtypeRegistration subtypeRegistration = new SubtypeRegistration();
		testPlugin.registerItemSubtypes(subtypeRegistration);
		SubtypeManager subtypeManager = new SubtypeManager(subtypeRegistration);

		RegisteredIngredientsBuilder registeredIngredientsBuilder = new RegisteredIngredientsBuilder(subtypeManager);
		testPlugin.registerIngredients(registeredIngredientsBuilder);

		IngredientBlacklistInternal blacklist = new IngredientBlacklistInternal();
		IModIdHelper modIdHelper = new TestModIdHelper();
		IClientConfig clientConfig = new TestClientConfig(false);
		this.registeredIngredients = registeredIngredientsBuilder.build();
		this.baseList = IngredientListElementFactory.createBaseList(registeredIngredients);

		this.editModeConfig = new EditModeConfig(null);

		IWorldConfig worldConfig = new TestWorldConfig();

		TestIngredientFilterConfig ingredientFilterConfig = new TestIngredientFilterConfig();
		IIngredientSorter ingredientListSorter = (a, b) -> Comparator.comparing(IListElementInfo::getModNameForSorting);
		this.ingredientVisibility = new IngredientVisibility(blacklist, worldConfig, editModeConfig, registeredIngredients);
		this.filterTextSource = new FilterTextSource();
		this.ingredientFilter = new IngredientFilter(
			filterTextSource,
			clientConfig,
			ingredientFilterConfig,
			registeredIngredients,
			ingredientListSorter,
			baseList,
			modIdHelper,
			ingredientVisibility
		);

		this.ingredientManager = new IngredientManager(modIdHelper, blacklist, clientConfig, registeredIngredients, ingredientFilter);
	}

	@Test
	public void testSetup() {
		Assertions.assertNotNull(ingredientFilter);

		List<?> ingredientList = ingredientFilter.getIngredientList();
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, ingredientList.size());
	}

	@Test
	public void testAddingAndRemovingIngredients() {
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(registeredIngredients);
		Assertions.assertNotNull(ingredientManager);
		Assertions.assertNotNull(ingredientVisibility);
		Assertions.assertNotNull(filterTextSource);

		List<TestIngredient> ingredients = createIngredients();

		addIngredients(ingredientFilter, filterTextSource, ingredientVisibility, registeredIngredients, ingredientManager, ingredients);
		removeIngredients(ingredientFilter, filterTextSource, ingredientVisibility, registeredIngredients, ingredientManager, ingredients);
		addIngredients(ingredientFilter, filterTextSource, ingredientVisibility, registeredIngredients, ingredientManager, ingredients);
	}

	@Test
	public void testAddingAndRemovingIngredientsWithTooltipStrings() {
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(registeredIngredients);
		Assertions.assertNotNull(ingredientManager);
		Assertions.assertNotNull(ingredientVisibility);
		Assertions.assertNotNull(filterTextSource);

		List<TestIngredient> ingredients = createIngredients();
		TestIngredient testIngredient = ingredients.get(0);
		IIngredientRenderer<TestIngredient> ingredientRenderer = registeredIngredients.getIngredientRenderer(TestIngredient.TYPE);
		List<String> tooltipStrings = getTooltipStrings(ingredientRenderer, testIngredient);

		addIngredients(ingredientFilter, filterTextSource, ingredientVisibility, registeredIngredients, ingredientManager, ingredients);
		for (String tooltipString : tooltipStrings) {
			filterTextSource.setFilterText(tooltipString);
			List<TestIngredient> filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
			Assertions.assertTrue(filteredIngredients.contains(testIngredient), tooltipString);
		}

		removeIngredients(ingredientFilter, filterTextSource, ingredientVisibility, registeredIngredients, ingredientManager, ingredients);
		for (String tooltipString : tooltipStrings) {
			filterTextSource.setFilterText(tooltipString);
			List<TestIngredient> filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
			Assertions.assertFalse(filteredIngredients.contains(testIngredient), tooltipString);
		}

		addIngredients(ingredientFilter, filterTextSource, ingredientVisibility, registeredIngredients, ingredientManager, ingredients);
		for (String tooltipString : tooltipStrings) {
			filterTextSource.setFilterText(tooltipString);
			List<TestIngredient> filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
			Assertions.assertTrue(filteredIngredients.contains(testIngredient), tooltipString);
		}
	}

	@Test
	public void testConfigBlacklist() {
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(registeredIngredients);
		Assertions.assertNotNull(baseList);
		Assertions.assertNotNull(editModeConfig);

		IListElement<?> element = baseList.get(0);
		ITypedIngredient<?> typedIngredient = element.getTypedIngredient();
		@SuppressWarnings("unchecked")
		ITypedIngredient<TestIngredient> blacklistedIngredient = (ITypedIngredient<TestIngredient>) typedIngredient;
		TestIngredientHelper testIngredientHelper = new TestIngredientHelper();
		editModeConfig.addIngredientToConfigBlacklist(ingredientFilter, blacklistedIngredient, IngredientBlacklistType.ITEM, testIngredientHelper);

		ingredientFilter.updateHidden();

		List<?> ingredientList = ingredientFilter.getIngredientList();
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT - 1, ingredientList.size());
	}

	public static List<String> getTooltipStrings(IIngredientRenderer<TestIngredient> ingredientRenderer, TestIngredient testIngredient) {
		List<Component> tooltip = ingredientRenderer.getTooltip(testIngredient, TooltipFlag.Default.NORMAL);
		return tooltip.stream()
			.map(Component::getString)
			.map(Translator::toLowercaseWithLocale)
			.filter(line -> !StringUtil.isNullOrEmpty(line))
			.toList();
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
		RegisteredIngredients registeredIngredients,
		IIngredientManager ingredientManager,
		List<TestIngredient> ingredientsToAdd
	) {
		List<IListElement<TestIngredient>> listToAdd = IngredientListElementFactory.createList(registeredIngredients, TestIngredient.TYPE, ingredientsToAdd);
		Assertions.assertEquals(EXTRA_INGREDIENT_COUNT, listToAdd.size());

		ingredientManager.addIngredientsAtRuntime(TestIngredient.TYPE, ingredientsToAdd);

		IngredientInfo<TestIngredient> ingredientInfo = registeredIngredients.getIngredientInfo(TestIngredient.TYPE);
		Collection<TestIngredient> testIngredients = ingredientInfo.getAllIngredients();
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
		RegisteredIngredients registeredIngredients,
		IIngredientManager ingredientManager,
		List<TestIngredient> ingredientsToRemove
	) {
		List<IListElement<TestIngredient>> listToRemove = IngredientListElementFactory.createList(registeredIngredients, TestIngredient.TYPE, ingredientsToRemove);
		Assertions.assertEquals(EXTRA_INGREDIENT_COUNT, listToRemove.size());

		ingredientManager.removeIngredientsAtRuntime(TestIngredient.TYPE, ingredientsToRemove);

		filterTextSource.setFilterText("");
		List<TestIngredient> filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, filteredIngredients.size());
		for (TestIngredient testIngredient : filteredIngredients) {
			Assertions.assertFalse(ingredientsToRemove.contains(testIngredient));
		}

		IngredientInfo<TestIngredient> ingredientInfo = registeredIngredients.getIngredientInfo(TestIngredient.TYPE);
		Collection<TestIngredient> testIngredients = ingredientInfo.getAllIngredients();
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, testIngredients.size());
		for (TestIngredient testIngredient : testIngredients) {
			Assertions.assertFalse(ingredientsToRemove.contains(testIngredient));
		}

		for (TestIngredient ingredient : ingredientsToRemove) {
			Assertions.assertFalse(ingredientVisibility.isIngredientVisible(TestIngredient.TYPE, ingredient));
		}
	}
}
