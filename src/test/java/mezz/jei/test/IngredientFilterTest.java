package mezz.jei.test;

import io.netty.util.internal.StringUtil;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.config.EditModeConfig;
import mezz.jei.config.IClientConfig;
import mezz.jei.config.IEditModeConfig;
import mezz.jei.config.IngredientBlacklistType;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.ingredients.IIngredientListElementInfo;
import mezz.jei.ingredients.IIngredientSorter;
import mezz.jei.ingredients.IngredientBlacklistInternal;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.ingredients.IngredientListElementFactory;
import mezz.jei.ingredients.IngredientManager;
import mezz.jei.ingredients.ModIngredientRegistration;
import mezz.jei.ingredients.RegisteredIngredient;
import mezz.jei.ingredients.SubtypeManager;
import mezz.jei.load.registration.SubtypeRegistration;
import mezz.jei.test.lib.TestClientConfig;
import mezz.jei.test.lib.TestIngredient;
import mezz.jei.test.lib.TestIngredientFilterConfig;
import mezz.jei.test.lib.TestIngredientHelper;
import mezz.jei.test.lib.TestModIdHelper;
import mezz.jei.test.lib.TestPlugin;
import mezz.jei.util.Translator;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class IngredientFilterTest {
	private static final int EXTRA_INGREDIENT_COUNT = 5;
	@Nullable
	private IngredientManager ingredientManager;
	@Nullable
	private IngredientFilter ingredientFilter;
	@Nullable
	private NonNullList<IIngredientListElement<?>> baseList;
	@Nullable
	private IEditModeConfig editModeConfig;

	@BeforeEach
	public void setup() {
		TestPlugin testPlugin = new TestPlugin();

		SubtypeRegistration subtypeRegistration = new SubtypeRegistration();
		testPlugin.registerItemSubtypes(subtypeRegistration);
		testPlugin.registerFluidSubtypes(subtypeRegistration);
		SubtypeManager subtypeManager = new SubtypeManager(subtypeRegistration);

		ModIngredientRegistration modIngredientRegistration = new ModIngredientRegistration(subtypeManager);
		testPlugin.registerIngredients(modIngredientRegistration);

		IngredientBlacklistInternal blacklist = new IngredientBlacklistInternal();
		IModIdHelper modIdHelper = new TestModIdHelper();
		List<RegisteredIngredient<?>> registeredIngredients = modIngredientRegistration.getRegisteredIngredients();
		this.ingredientManager =  new IngredientManager(modIdHelper, blacklist, registeredIngredients, true);

		this.baseList = IngredientListElementFactory.createBaseList(ingredientManager);

		this.editModeConfig = new EditModeConfig(null);

		IClientConfig clientConfig = new TestClientConfig(false);

		TestIngredientFilterConfig ingredientFilterConfig = new TestIngredientFilterConfig();
		IIngredientSorter ingredientListSorter = (a, b) -> Comparator.comparing(IIngredientListElementInfo::getModNameForSorting);
		this.ingredientFilter = new IngredientFilter(blacklist, clientConfig, ingredientFilterConfig, editModeConfig, ingredientManager, ingredientListSorter, baseList, modIdHelper);
	}

	@Test
	public void testSetup() {
		Assertions.assertNotNull(ingredientFilter);

		List<IIngredientListElement<?>> ingredientList = ingredientFilter.getIngredientList("");
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, ingredientList.size());
	}

	@Test
	public void testAddingAndRemovingIngredients() {
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(ingredientManager);

		List<TestIngredient> ingredients = createIngredients();
		TestIngredient testIngredient = ingredients.get(0);
		IIngredientRenderer<TestIngredient> ingredientRenderer = ingredientManager.getIngredientRenderer(testIngredient);
		List<String> tooltipStrings = getTooltipStrings(ingredientRenderer, testIngredient);
		List<TestIngredient> filteredIngredients;

		addIngredients(ingredientFilter, ingredientManager, ingredients);
		for (String tooltipString : tooltipStrings) {
			filteredIngredients = getFilteredIngredients(ingredientFilter, tooltipString);
			assert filteredIngredients.contains(testIngredient);
		}
		for (TestIngredient ingredient : ingredients) {
			assert ingredientFilter.isIngredientVisible(ingredient);
		}

		removeIngredients(ingredientFilter, ingredientManager, ingredients);
		for (String tooltipString : tooltipStrings) {
			filteredIngredients = getFilteredIngredients(ingredientFilter, tooltipString);
			assert !filteredIngredients.contains(testIngredient);
		}
		for (TestIngredient ingredient : ingredients) {
			assert !ingredientFilter.isIngredientVisible(ingredient);
		}

		addIngredients(ingredientFilter, ingredientManager, ingredients);
		for (String tooltipString : tooltipStrings) {
			filteredIngredients = getFilteredIngredients(ingredientFilter, tooltipString);
			assert filteredIngredients.contains(testIngredient);
		}
		for (TestIngredient ingredient : ingredients) {
			assert ingredientFilter.isIngredientVisible(ingredient);
		}
	}
	
	private List<TestIngredient> getFilteredIngredients(IngredientFilter ingredientFilter, String tooltipString) {
		return ingredientFilter.getFilteredIngredients(tooltipString)
				.stream()
				.filter(i -> i instanceof TestIngredient)
				.map(i -> (TestIngredient) i)
				.collect(Collectors.toList());
	}

	@Test
	public void testRebuilding() {
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(ingredientManager);

		ingredientFilter.modesChanged();

		List<IIngredientListElement<?>> ingredientList = ingredientFilter.getIngredientList("");
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, ingredientList.size());

		List<TestIngredient> ingredients = createIngredients();

		addIngredients(ingredientFilter, ingredientManager, ingredients);

		ingredientFilter.modesChanged();

		ingredientList = ingredientFilter.getIngredientList("");
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT, ingredientList.size());

		removeIngredients(ingredientFilter, ingredientManager, ingredients);

		ingredientFilter.modesChanged();

		ingredientList = ingredientFilter.getIngredientList("");
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, ingredientList.size());
	}

	@Test
	public void testConfigBlacklist() {
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(ingredientManager);
		Assertions.assertNotNull(baseList);
		Assertions.assertNotNull(editModeConfig);

		TestIngredient blacklistedIngredient = (TestIngredient) baseList.get(0).getIngredient();
		TestIngredientHelper testIngredientHelper = new TestIngredientHelper();
		editModeConfig.addIngredientToConfigBlacklist(ingredientFilter, ingredientManager, blacklistedIngredient, IngredientBlacklistType.ITEM, testIngredientHelper);

		ingredientFilter.updateHidden();

		List<IIngredientListElement<?>> ingredientList = ingredientFilter.getIngredientList("");
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT - 1, ingredientList.size());

		// test after reloading the ingredient filter
		ingredientFilter.modesChanged();

		ingredientList = ingredientFilter.getIngredientList("");
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT - 1, ingredientList.size());
	}

	public static List<String> getTooltipStrings(IIngredientRenderer<TestIngredient> ingredientRenderer, TestIngredient testIngredient) {
		List<ITextComponent> tooltip = ingredientRenderer.getTooltip(testIngredient, ITooltipFlag.TooltipFlags.NORMAL);
		return tooltip.stream()
				.map(ITextComponent::getString)
				.map(Translator::toLowercaseWithLocale)
				.filter(line -> !StringUtil.isNullOrEmpty(line))
				.collect(Collectors.toList());
	}

	public static List<TestIngredient> createIngredients() {
		List<TestIngredient> ingredients = new ArrayList<>();
		for (int i = TestPlugin.BASE_INGREDIENT_COUNT; i < TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT; i++) {
			ingredients.add(new TestIngredient(i));
		}
		Assertions.assertEquals(EXTRA_INGREDIENT_COUNT, ingredients.size());
		return ingredients;
	}

	private static void addIngredients(IngredientFilter ingredientFilter, IngredientManager ingredientManager, List<TestIngredient> ingredients) {
		List<IIngredientListElement<TestIngredient>> listToAdd = IngredientListElementFactory.createList(ingredientManager, TestIngredient.TYPE, ingredients);
		Assertions.assertEquals(EXTRA_INGREDIENT_COUNT, listToAdd.size());

		ingredientManager.addIngredientsAtRuntime(TestIngredient.TYPE, ingredients, ingredientFilter);

		Collection<TestIngredient> testIngredients = ingredientManager.getAllIngredients(TestIngredient.TYPE);
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT, testIngredients.size());

		List<IIngredientListElement<?>> ingredientList = ingredientFilter.getIngredientList("");
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT, ingredientList.size());
	}

	private static void removeIngredients(IngredientFilter ingredientFilter, IngredientManager ingredientManager, List<TestIngredient> ingredientsToRemove) {
		List<IIngredientListElement<TestIngredient>> listToRemove = IngredientListElementFactory.createList(ingredientManager, TestIngredient.TYPE, ingredientsToRemove);
		Assertions.assertEquals(EXTRA_INGREDIENT_COUNT, listToRemove.size());

		ingredientManager.removeIngredientsAtRuntime(TestIngredient.TYPE, ingredientsToRemove, ingredientFilter);

		List<IIngredientListElement<?>> ingredientList = ingredientFilter.getIngredientList("");
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, ingredientList.size());

		Collection<TestIngredient> testIngredients = ingredientManager.getAllIngredients(TestIngredient.TYPE);
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, testIngredients.size());
	}
}
