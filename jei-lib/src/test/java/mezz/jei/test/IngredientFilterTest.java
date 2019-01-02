package mezz.jei.test;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.util.NonNullList;

import mezz.jei.config.HideModeConfig;
import mezz.jei.config.IHideModeConfig;
import mezz.jei.config.IngredientBlacklistType;
import mezz.jei.api.ingredients.IModIdHelper;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.ingredients.IngredientBlacklist;
import mezz.jei.ingredients.IngredientBlacklistInternal;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.ingredients.IngredientListElementFactory;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.ingredients.ModIngredientRegistration;
import mezz.jei.runtime.JeiHelpers;
import mezz.jei.runtime.SubtypeRegistry;
import mezz.jei.test.lib.TestIngredient;
import mezz.jei.test.lib.TestIngredientFilterConfig;
import mezz.jei.test.lib.TestIngredientHelper;
import mezz.jei.test.lib.TestModIdHelper;
import mezz.jei.test.lib.TestPlugin;
import mezz.jei.util.StackHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class IngredientFilterTest {
	private static final int EXTRA_INGREDIENT_COUNT = 5;
	@Nullable
	private JeiHelpers jeiHelpers;
	@Nullable
	private IngredientRegistry ingredientRegistry;
	@Nullable
	private IngredientFilter ingredientFilter;
	@Nullable
	private NonNullList<IIngredientListElement> baseList;
	@Nullable
	private IHideModeConfig hideModeConfig;

	@Before
	public void setup() {
		TestPlugin testPlugin = new TestPlugin();

		SubtypeRegistry subtypeRegistry = new SubtypeRegistry();
		testPlugin.registerItemSubtypes(subtypeRegistry);

		ModIngredientRegistration modIngredientRegistry = new ModIngredientRegistration();
		testPlugin.registerIngredients(modIngredientRegistry);

		IngredientBlacklistInternal blacklist = new IngredientBlacklistInternal();
		IModIdHelper modIdHelper = new TestModIdHelper();
		this.ingredientRegistry = modIngredientRegistry.createIngredientRegistry(modIdHelper, blacklist, true);

		this.baseList = IngredientListElementFactory.createBaseList(ingredientRegistry, modIdHelper);

		this.hideModeConfig = new HideModeConfig(modIdHelper, null);

		StackHelper stackHelper = new StackHelper(subtypeRegistry);
		this.jeiHelpers = new JeiHelpers(ingredientRegistry, blacklist, stackHelper, hideModeConfig, modIdHelper);

		TestIngredientFilterConfig ingredientFilterConfig = new TestIngredientFilterConfig();
		this.ingredientFilter = new IngredientFilter(blacklist, ingredientFilterConfig, hideModeConfig);
	}

	@Test
	public void testSetup() {
		Assert.assertNotNull(ingredientFilter);
		Assert.assertNotNull(baseList);

		ingredientFilter.addIngredients(baseList);
		List<IIngredientListElement> ingredientList = ingredientFilter.getIngredientList("");
		Assert.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, ingredientList.size());
	}

	@Test
	public void testAddingAndRemovingIngredients() {
		Assert.assertNotNull(ingredientFilter);
		Assert.assertNotNull(baseList);

		ingredientFilter.addIngredients(baseList);
		addIngredients(ingredientFilter);
		removeIngredients(ingredientFilter);
	}

	@Test
	public void testRebuilding() {
		Assert.assertNotNull(ingredientFilter);
		Assert.assertNotNull(baseList);

		ingredientFilter.addIngredients(baseList);

		ingredientFilter.modesChanged();

		List<IIngredientListElement> ingredientList = ingredientFilter.getIngredientList("");
		Assert.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, ingredientList.size());

		addIngredients(ingredientFilter);

		ingredientFilter.modesChanged();

		ingredientList = ingredientFilter.getIngredientList("");
		Assert.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT, ingredientList.size());

		removeIngredients(ingredientFilter);

		ingredientFilter.modesChanged();

		ingredientList = ingredientFilter.getIngredientList("");
		Assert.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, ingredientList.size());
	}

	@Test
	public void testApiBlacklist() {
		Assert.assertNotNull(ingredientFilter);
		Assert.assertNotNull(jeiHelpers);
		Assert.assertNotNull(baseList);

		IngredientBlacklist ingredientBlacklist = this.jeiHelpers.getIngredientBlacklist();
		Object blacklistedIngredient = baseList.get(0).getIngredient();
		ingredientBlacklist.addIngredientToBlacklist(blacklistedIngredient);

		ingredientFilter.addIngredients(baseList);
		List<IIngredientListElement> ingredientList = ingredientFilter.getIngredientList("");
		Assert.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT - 1, ingredientList.size());

		// test after reloading the ingredient filter
		ingredientFilter.modesChanged();

		ingredientList = ingredientFilter.getIngredientList("");
		Assert.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT - 1, ingredientList.size());
	}

	@Test
	public void testConfigBlacklist() {
		Assert.assertNotNull(ingredientFilter);
		Assert.assertNotNull(ingredientRegistry);
		Assert.assertNotNull(baseList);
		Assert.assertNotNull(hideModeConfig);

		ingredientFilter.addIngredients(baseList);

		TestIngredient blacklistedIngredient = (TestIngredient) baseList.get(0).getIngredient();
		TestIngredientHelper testIngredientHelper = new TestIngredientHelper();
		hideModeConfig.addIngredientToConfigBlacklist(ingredientFilter, ingredientRegistry, blacklistedIngredient, IngredientBlacklistType.ITEM, testIngredientHelper);

		ingredientFilter.updateHidden();

		List<IIngredientListElement> ingredientList = ingredientFilter.getIngredientList("");
		Assert.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT - 1, ingredientList.size());

		// test after reloading the ingredient filter
		ingredientFilter.modesChanged();

		ingredientList = ingredientFilter.getIngredientList("");
		Assert.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT - 1, ingredientList.size());
	}

	private void addIngredients(IngredientFilter ingredientFilter) {
		Assert.assertNotNull(ingredientRegistry);
		Assert.assertNotNull(jeiHelpers);

		List<TestIngredient> ingredientsToAdd = new ArrayList<>();
		for (int i = TestPlugin.BASE_INGREDIENT_COUNT; i < TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT; i++) {
			ingredientsToAdd.add(new TestIngredient(i));
		}
		Assert.assertEquals(EXTRA_INGREDIENT_COUNT, ingredientsToAdd.size());

		IModIdHelper modIdHelper = jeiHelpers.getModIdHelper();
		List<IIngredientListElement<TestIngredient>> listToAdd = IngredientListElementFactory.createList(ingredientRegistry, TestIngredient.TYPE, ingredientsToAdd, modIdHelper);
		Assert.assertEquals(EXTRA_INGREDIENT_COUNT, listToAdd.size());

		ingredientRegistry.addIngredientsAtRuntime(TestIngredient.TYPE, ingredientsToAdd, ingredientFilter);

		Collection<TestIngredient> testIngredients = ingredientRegistry.getAllIngredients(TestIngredient.TYPE);
		Assert.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT, testIngredients.size());

		List<IIngredientListElement> ingredientList = ingredientFilter.getIngredientList("");
		Assert.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT, ingredientList.size());
	}

	private void removeIngredients(IngredientFilter ingredientFilter) {
		Assert.assertNotNull(ingredientRegistry);
		Assert.assertNotNull(jeiHelpers);

		List<TestIngredient> ingredientsToRemove = new ArrayList<>();
		for (int i = TestPlugin.BASE_INGREDIENT_COUNT; i < TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT; i++) {
			ingredientsToRemove.add(new TestIngredient(i));
		}
		Assert.assertEquals(EXTRA_INGREDIENT_COUNT, ingredientsToRemove.size());

		IModIdHelper modIdHelper = jeiHelpers.getModIdHelper();
		List<IIngredientListElement<TestIngredient>> listToRemove = IngredientListElementFactory.createList(ingredientRegistry, TestIngredient.TYPE, ingredientsToRemove, modIdHelper);
		Assert.assertEquals(EXTRA_INGREDIENT_COUNT, listToRemove.size());

		ingredientRegistry.removeIngredientsAtRuntime(TestIngredient.TYPE, ingredientsToRemove, ingredientFilter);

		List<IIngredientListElement> ingredientList = ingredientFilter.getIngredientList("");
		Assert.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, ingredientList.size());

		Collection<TestIngredient> testIngredients = ingredientRegistry.getAllIngredients(TestIngredient.TYPE);
		Assert.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, testIngredients.size());
	}
}
