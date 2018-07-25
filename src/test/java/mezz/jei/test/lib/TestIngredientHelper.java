package mezz.jei.test.lib;

import mezz.jei.api.ingredients.IIngredientHelper;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.awt.Color;
import java.util.Collections;
import java.util.List;

public class TestIngredientHelper implements IIngredientHelper<TestIngredient> {
	@Override
	public List<TestIngredient> expandSubtypes(List<TestIngredient> ingredients) {
		return ingredients;
	}

	@Nullable
	@Override
	public TestIngredient getMatch(Iterable<TestIngredient> ingredients, TestIngredient ingredientToMatch) {
		for (TestIngredient ingredient : ingredients) {
			if (ingredient.getNumber() == ingredientToMatch.getNumber()) {
				return ingredient;
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
	public Iterable<Color> getColors(TestIngredient ingredient) {
		return Collections.singleton(Color.BLACK);
	}

	@Override
	public String getResourceId(TestIngredient ingredient) {
		return "Test Ingredient Resource Id " + ingredient;
	}

	@Override
	public ItemStack cheatIngredient(TestIngredient ingredient, boolean fullStack) {
		return ItemStack.EMPTY;
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
