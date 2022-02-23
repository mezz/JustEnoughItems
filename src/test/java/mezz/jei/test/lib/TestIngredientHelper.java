package mezz.jei.test.lib;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;

public class TestIngredientHelper implements IIngredientHelper<TestIngredient> {
	@Override
	public IIngredientType<TestIngredient> getIngredientType() {
		return TestIngredient.TYPE;
	}

	@Override
	public String getDisplayName(TestIngredient ingredient) {
		return "Test Ingredient Display Name " + ingredient;
	}

	@Override
	public String getUniqueId(TestIngredient ingredient, UidContext context) {
		return "Test Ingredient Unique Id " + ingredient;
	}

	@Override
	public String getWildcardId(TestIngredient ingredient) {
		return "Test Ingredient Unique Id";
	}

	@SuppressWarnings("removal")
	@Override
	public String getModId(TestIngredient ingredient) {
		return "jei_test_mod";
	}

	@Override
	public Iterable<Integer> getColors(TestIngredient ingredient) {
		return List.of(0xFF000000);
	}

	@SuppressWarnings("removal")
	@Override
	public String getResourceId(TestIngredient ingredient) {
		return "test_ingredient_" + ingredient.getNumber();
	}

	@Override
	public TestIngredient copyIngredient(TestIngredient ingredient) {
		return ingredient.copy();
	}

	@Override
	public String getErrorInfo(@Nullable TestIngredient ingredient) {
		return "Test Ingredient Error Info " + ingredient;
	}
}
