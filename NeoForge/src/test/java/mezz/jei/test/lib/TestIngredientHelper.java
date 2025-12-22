package mezz.jei.test.lib;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class TestIngredientHelper implements IIngredientHelper<TestIngredient> {
	@Override
	public IIngredientType<TestIngredient> getIngredientType() {
		return TestIngredient.TYPE;
	}

	@Override
	public String getDisplayName(TestIngredient ingredient) {
		return "§eTest Ingredient Display Name " + ingredient;
	}

	@Override
	public Object getUid(TestIngredient ingredient, UidContext context) {
		return ingredient.number();
	}

	@Override
	public Object getGroupingUid(TestIngredient ingredient) {
		return TestIngredient.class;
	}

	@Override
	public Iterable<Integer> getColors(TestIngredient ingredient) {
		return List.of(0xFF000000);
	}

	@Override
	public Identifier getIdentifier(TestIngredient ingredient) {
		return Identifier.fromNamespaceAndPath("jei_test_mod", "test_ingredient_" + ingredient.number());
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
