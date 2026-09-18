package mezz.jei.test;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.common.ingredients.TypedIngredient;
import mezz.jei.library.gui.ingredients.RecipeSlotIngredients;
import org.junit.jupiter.api.Test;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RecipeSlotIngredientVisibilityTest {
	private static final IIngredientType<Integer> TYPE = () -> Integer.class;

	@Test
	void lateHiddenIngredientDoesNotExpandTheRotationLimit() {
		List<@Nullable ITypedIngredient<?>> ingredients = IntStream.range(0, 1000)
			.<ITypedIngredient<?>>mapToObj(i -> TypedIngredient.createUnvalidated(TYPE, i)).toList();

		var rotation = RecipeSlotIngredients.filterVisibleIngredients(ingredients, ingredient -> !ingredient.getIngredient().equals(900), 100);

		assertEquals(ingredients.subList(0, 100), rotation);
	}

	@Test
	void visibleIngredientsBeyondTheRotationLimitCanStillBeSelected() {
		List<@Nullable ITypedIngredient<?>> ingredients = IntStream.range(0, 1000)
			.<ITypedIngredient<?>>mapToObj(i -> TypedIngredient.createUnvalidated(TYPE, i)).toList();

		var rotation = RecipeSlotIngredients.filterVisibleIngredients(ingredients, ingredient -> ingredient.getIngredient().equals(999), 100);

		assertEquals(List.of(ingredients.get(ingredients.size() - 1)), rotation);
	}

	@Test
	void allHiddenFallbackAndBlankPositionsRespectTheLimit() {
		ITypedIngredient<Integer> hidden = TypedIngredient.createUnvalidated(TYPE, 1);
		List<@Nullable ITypedIngredient<?>> blanks = Arrays.asList(hidden, null, hidden);

		var rotationWithBlank = RecipeSlotIngredients.filterVisibleIngredients(blanks, ingredient -> false, 1);
		var allHiddenFallback = RecipeSlotIngredients.filterVisibleIngredients(List.of(hidden, hidden), ingredient -> false, 1);

		assertEquals(1, rotationWithBlank.size());
		assertNull(rotationWithBlank.get(0));
		assertEquals(List.of(hidden), allHiddenFallback);
	}
}
