package mezz.jei.test;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.util.NonNullList;

import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.config.HideModeConfig;
import mezz.jei.config.IHideModeConfig;
import mezz.jei.config.IngredientBlacklistType;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.ingredients.IngredientBlacklistInternal;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.ingredients.IngredientListElementFactory;
import mezz.jei.ingredients.IngredientManager;
import mezz.jei.ingredients.ModIngredientRegistration;
import mezz.jei.ingredients.SubtypeManager;
import mezz.jei.load.registration.SubtypeRegistration;
import mezz.jei.test.lib.TestIngredient;
import mezz.jei.test.lib.TestIngredientFilterConfig;
import mezz.jei.test.lib.TestIngredientHelper;
import mezz.jei.test.lib.TestModIdHelper;
import mezz.jei.test.lib.TestPlugin;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class IngredientFilterTest {
	private static final int EXTRA_INGREDIENT_COUNT = 5;
	@Nullable
	private IngredientManager ingredientManager;
	@Nullable
	private IngredientFilter ingredientFilter;
	@Nullable
	private NonNullList<IIngredientListElement<?>> baseList;
	@Nullable
	private IHideModeConfig hideModeConfig;
	@Nullable
	private IModIdHelper modIdHelper;

	@Before
	public void setup() {
		TestPlugin testPlugin = new TestPlugin();

		SubtypeRegistration subtypeRegistration = new SubtypeRegistration();
		testPlugin.registerItemSubtypes(subtypeRegistration);
		SubtypeManager subtypeManager = new SubtypeManager(subtypeRegistration);

		ModIngredientRegistration modIngredientRegistration = new ModIngredientRegistration(subtypeManager);
		testPlugin.registerIngredients(modIngredientRegistration);

		IngredientBlacklistInternal blacklist = new IngredientBlacklistInternal();
		this.modIdHelper = new TestModIdHelper();
		this.ingredientManager = modIngredientRegistration.createIngredientManager(modIdHelper, blacklist, true);

		this.baseList = IngredientListElementFactory.createBaseList(ingredientManager);

		this.hideModeConfig = new HideModeConfig(null);

		TestIngredientFilterConfig ingredientFilterConfig = new TestIngredientFilterConfig();
		this.ingredientFilter = new IngredientFilter(blacklist, ingredientFilterConfig, hideModeConfig, ingredientManager, modIdHelper);
	}

	@Test
	public void testSetup() {
		Assert.assertNotNull(ingredientFilter);
		Assert.assertNotNull(baseList);
		Assert.assertNotNull(ingredientManager);
		Assert.assertNotNull(modIdHelper);

		ingredientFilter.addIngredients(baseList, ingredientManager, modIdHelper);
		List<IIngredientListElement<?>> ingredientList = ingredientFilter.getIngredientList("");
		Assert.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, ingredientList.size());
	}

	@Test
	public void testAddingAndRemovingIngredients() {
		Assert.assertNotNull(ingredientFilter);
		Assert.assertNotNull(baseList);
		Assert.assertNotNull(ingredientManager);
		Assert.assertNotNull(modIdHelper);

		ingredientFilter.addIngredients(baseList, ingredientManager, modIdHelper);
		addIngredients(ingredientFilter);
		removeIngredients(ingredientFilter);
	}

	@Test
	public void testRebuilding() {
		Assert.assertNotNull(ingredientFilter);
		Assert.assertNotNull(baseList);
		Assert.assertNotNull(ingredientManager);
		Assert.assertNotNull(modIdHelper);

		ingredientFilter.addIngredients(baseList, ingredientManager, modIdHelper);

		ingredientFilter.modesChanged();

		List<IIngredientListElement<?>> ingredientList = ingredientFilter.getIngredientList("");
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
	public void testConfigBlacklist() {
		Assert.assertNotNull(ingredientFilter);
		Assert.assertNotNull(ingredientManager);
		Assert.assertNotNull(baseList);
		Assert.assertNotNull(ingredientManager);
		Assert.assertNotNull(hideModeConfig);
		Assert.assertNotNull(modIdHelper);

		ingredientFilter.addIngredients(baseList, ingredientManager, modIdHelper);

		TestIngredient blacklistedIngredient = (TestIngredient) baseList.get(0).getIngredient();
		TestIngredientHelper testIngredientHelper = new TestIngredientHelper();
		hideModeConfig.addIngredientToConfigBlacklist(ingredientFilter, ingredientManager, blacklistedIngredient, IngredientBlacklistType.ITEM, testIngredientHelper);

		ingredientFilter.updateHidden();

		List<IIngredientListElement<?>> ingredientList = ingredientFilter.getIngredientList("");
		Assert.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT - 1, ingredientList.size());

		// test after reloading the ingredient filter
		ingredientFilter.modesChanged();

		ingredientList = ingredientFilter.getIngredientList("");
		Assert.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT - 1, ingredientList.size());
	}

	private void addIngredients(IngredientFilter ingredientFilter) {
		Assert.assertNotNull(ingredientManager);

		List<TestIngredient> ingredientsToAdd = new ArrayList<>();
		for (int i = TestPlugin.BASE_INGREDIENT_COUNT; i < TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT; i++) {
			ingredientsToAdd.add(new TestIngredient(i));
		}
		Assert.assertEquals(EXTRA_INGREDIENT_COUNT, ingredientsToAdd.size());

		List<IIngredientListElement<TestIngredient>> listToAdd = IngredientListElementFactory.createList(ingredientManager, TestIngredient.TYPE, ingredientsToAdd);
		Assert.assertEquals(EXTRA_INGREDIENT_COUNT, listToAdd.size());

		ingredientManager.addIngredientsAtRuntime(TestIngredient.TYPE, ingredientsToAdd, ingredientFilter);

		Collection<TestIngredient> testIngredients = ingredientManager.getAllIngredients(TestIngredient.TYPE);
		Assert.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT, testIngredients.size());

		List<IIngredientListElement<?>> ingredientList = ingredientFilter.getIngredientList("");
		Assert.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT, ingredientList.size());
	}

	private void removeIngredients(IngredientFilter ingredientFilter) {
		Assert.assertNotNull(ingredientManager);

		List<TestIngredient> ingredientsToRemove = new ArrayList<>();
		for (int i = TestPlugin.BASE_INGREDIENT_COUNT; i < TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT; i++) {
			ingredientsToRemove.add(new TestIngredient(i));
		}
		Assert.assertEquals(EXTRA_INGREDIENT_COUNT, ingredientsToRemove.size());

		List<IIngredientListElement<TestIngredient>> listToRemove = IngredientListElementFactory.createList(ingredientManager, TestIngredient.TYPE, ingredientsToRemove);
		Assert.assertEquals(EXTRA_INGREDIENT_COUNT, listToRemove.size());

		ingredientManager.removeIngredientsAtRuntime(TestIngredient.TYPE, ingredientsToRemove, ingredientFilter);

		List<IIngredientListElement<?>> ingredientList = ingredientFilter.getIngredientList("");
		Assert.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, ingredientList.size());

		Collection<TestIngredient> testIngredients = ingredientManager.getAllIngredients(TestIngredient.TYPE);
		Assert.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, testIngredients.size());
	}
}
