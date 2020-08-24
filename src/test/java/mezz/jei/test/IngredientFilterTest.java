package mezz.jei.test;

import mezz.jei.config.Config;
import mezz.jei.config.IngredientBlacklistType;
import mezz.jei.gui.GuiHelper;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.gui.textures.JeiTextureMap;
import mezz.jei.gui.textures.Textures;
import mezz.jei.ingredients.IngredientBlacklist;
import mezz.jei.ingredients.IngredientBlacklistInternal;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.ingredients.IngredientListElementFactory;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.runtime.JeiHelpers;
import mezz.jei.runtime.SubtypeRegistry;
import mezz.jei.startup.IModIdHelper;
import mezz.jei.startup.ModIngredientRegistration;
import mezz.jei.startup.StackHelper;
import mezz.jei.test.lib.TestIngredient;
import mezz.jei.test.lib.TestIngredientHelper;
import mezz.jei.test.lib.TestModIdHelper;
import mezz.jei.test.lib.TestPlugin;
import net.minecraft.util.NonNullList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class IngredientFilterTest {
	private static final int EXTRA_INGREDIENT_COUNT = 5;
	@Nullable
	private IModIdHelper modIdHelper;
	@Nullable
	private JeiHelpers jeiHelpers;
	@Nullable
	private IngredientRegistry ingredientRegistry;
	@Nullable
	private IngredientFilter ingredientFilter;
	@Nullable
	private NonNullList<IIngredientListElement> baseList;

	@Before
	public void setup() {
		TestPlugin testPlugin = new TestPlugin();

		SubtypeRegistry subtypeRegistry = new SubtypeRegistry();
		testPlugin.registerItemSubtypes(subtypeRegistry);

		ModIngredientRegistration modIngredientRegistry = new ModIngredientRegistration();
		testPlugin.registerIngredients(modIngredientRegistry);

		IngredientBlacklistInternal blacklist = new IngredientBlacklistInternal();
		this.modIdHelper = new TestModIdHelper();
		this.ingredientRegistry = modIngredientRegistry.createIngredientRegistry(modIdHelper, blacklist);

		this.baseList = IngredientListElementFactory.createBaseList(ingredientRegistry, modIdHelper);

		StackHelper stackHelper = new StackHelper(subtypeRegistry);
		Textures textures = new Textures(new JeiTextureMap("textures"));
		GuiHelper guiHelper = new GuiHelper(ingredientRegistry, textures);
		this.jeiHelpers = new JeiHelpers(guiHelper, ingredientRegistry, blacklist, stackHelper);

		this.ingredientFilter = new IngredientFilter(blacklist);
	}

	@Test
	public void testSetup() {
		Assert.assertNotNull(ingredientFilter);
		Assert.assertNotNull(baseList);

		ingredientFilter.addIngredients(baseList);
		List<IIngredientListElement> ingredientList = ingredientFilter.getIngredientList();
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

		List<IIngredientListElement> ingredientList = ingredientFilter.getIngredientList();
		Assert.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, ingredientList.size());

		addIngredients(ingredientFilter);

		ingredientFilter.modesChanged();

		ingredientList = ingredientFilter.getIngredientList();
		Assert.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT, ingredientList.size());

		removeIngredients(ingredientFilter);

		ingredientFilter.modesChanged();

		ingredientList = ingredientFilter.getIngredientList();
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
		List<IIngredientListElement> ingredientList = ingredientFilter.getIngredientList();
		Assert.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT - 1, ingredientList.size());

		// test after reloading the ingredient filter
		ingredientFilter.modesChanged();

		ingredientList = ingredientFilter.getIngredientList();
		Assert.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT - 1, ingredientList.size());
	}

	@Test
	public void testConfigBlacklist() {
		Assert.assertNotNull(ingredientFilter);
		Assert.assertNotNull(ingredientRegistry);
		Assert.assertNotNull(baseList);

		ingredientFilter.addIngredients(baseList);

		TestIngredient blacklistedIngredient = (TestIngredient) baseList.get(0).getIngredient();
		TestIngredientHelper testIngredientHelper = new TestIngredientHelper();
		Config.addIngredientToConfigBlacklist(ingredientFilter, ingredientRegistry, blacklistedIngredient, IngredientBlacklistType.ITEM, testIngredientHelper);

		ingredientFilter.updateHidden();

		List<IIngredientListElement> ingredientList = ingredientFilter.getIngredientList();
		Assert.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT - 1, ingredientList.size());

		// test after reloading the ingredient filter
		ingredientFilter.modesChanged();

		ingredientList = ingredientFilter.getIngredientList();
		Assert.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT - 1, ingredientList.size());
	}

	private void addIngredients(IngredientFilter ingredientFilter) {
		Assert.assertNotNull(ingredientRegistry);
		Assert.assertNotNull(modIdHelper);

		List<TestIngredient> ingredientsToAdd = new ArrayList<>();
		for (int i = TestPlugin.BASE_INGREDIENT_COUNT; i < TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT; i++) {
			ingredientsToAdd.add(new TestIngredient(i));
		}
		Assert.assertEquals(EXTRA_INGREDIENT_COUNT, ingredientsToAdd.size());

		List<IIngredientListElement<TestIngredient>> listToAdd = IngredientListElementFactory.createList(ingredientRegistry, TestIngredient.TYPE, ingredientsToAdd, modIdHelper);
		Assert.assertEquals(EXTRA_INGREDIENT_COUNT, listToAdd.size());

		ingredientRegistry.addIngredientsAtRuntime(TestIngredient.TYPE, ingredientsToAdd, ingredientFilter);

		Collection<TestIngredient> testIngredients = ingredientRegistry.getAllIngredients(TestIngredient.TYPE);
		Assert.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT, testIngredients.size());

		List<IIngredientListElement> ingredientList = ingredientFilter.getIngredientList();
		Assert.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT, ingredientList.size());
	}

	private void removeIngredients(IngredientFilter ingredientFilter) {
		Assert.assertNotNull(ingredientRegistry);
		Assert.assertNotNull(modIdHelper);

		List<TestIngredient> ingredientsToRemove = new ArrayList<>();
		for (int i = TestPlugin.BASE_INGREDIENT_COUNT; i < TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT; i++) {
			ingredientsToRemove.add(new TestIngredient(i));
		}
		Assert.assertEquals(EXTRA_INGREDIENT_COUNT, ingredientsToRemove.size());

		List<IIngredientListElement<TestIngredient>> listToRemove = IngredientListElementFactory.createList(ingredientRegistry, TestIngredient.TYPE, ingredientsToRemove, modIdHelper);
		Assert.assertEquals(EXTRA_INGREDIENT_COUNT, listToRemove.size());

		ingredientRegistry.removeIngredientsAtRuntime(TestIngredient.TYPE, ingredientsToRemove, ingredientFilter);

		List<IIngredientListElement> ingredientList = ingredientFilter.getIngredientList();
		Assert.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, ingredientList.size());

		Collection<TestIngredient> testIngredients = ingredientRegistry.getAllIngredients(TestIngredient.TYPE);
		Assert.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, testIngredients.size());
	}
}
