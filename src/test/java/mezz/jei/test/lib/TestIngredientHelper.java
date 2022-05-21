package mezz.jei.test.lib;

import javax.annotation.Nullable;
import java.util.Collections;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;

public class TestIngredientHelper implements IIngredientHelper<TestIngredient> {
	@Override
	@Nullable
	@Deprecated
	public TestIngredient getMatch(Iterable<TestIngredient> ingredients, TestIngredient toMatch) {
		return getMatch(ingredients, toMatch, UidContext.Ingredient);
	}

	@Nullable
	@Override
	public TestIngredient getMatch(Iterable<TestIngredient> ingredients, TestIngredient ingredientToMatch, UidContext context) {
		for (TestIngredient ingredient : ingredients) {
			if (ingredient.getNumber() == ingredientToMatch.getNumber()) {
				String keyLhs = getUniqueId(ingredientToMatch, context);
				String keyRhs = getUniqueId(ingredient, context);
				if (keyLhs.equals(keyRhs)) {
					return ingredient;
				}
			}
		}
		return null;
	}

	@Override
	public String getDisplayName(TestIngredient ingredient) {
		return "Test Ingredient Display Name " + ingredient;
	}

	@Override
	public String getUniqueId(TestIngredient ingredient) {
		return "Test Ingredient Unique Id " + ingredient;
	}

	@Override
	public String getWildcardId(TestIngredient ingredient) {
		return "Test Ingredient Unique Id";
	}

	@Override
	public String getModId(TestIngredient ingredient) {
		return "JEI Test Mod";
	}

	@Override
	public Iterable<Integer> getColors(TestIngredient ingredient) {
		return Collections.singleton(0xFF000000);
	}

	@Override
	public String getResourceId(TestIngredient ingredient) {
		return "Test Ingredient Resource Id " + ingredient;
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
