package mezz.jei.plugins.vanilla.anvil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.test.lib.TestIngredient;
import mezz.jei.test.lib.TestIngredientHelper;

public class AnvilRecipeCategoryTest {
	private static final TestIngredientHelper ingredientHelper = new TestIngredientHelper();

	@Test
	public void inputFocusLinksAllCorrelatedSlots() {
		// Setup: every ingredient at one index belongs to the same anvil recipe variant.
		List<TestIngredient> leftInputs = ingredients(1, 2);
		List<TestIngredient> rightInputs = ingredients(3, 4);
		List<TestIngredient> outputs = ingredients(5, 6);
		IFocus<TestIngredient> focus = focus(IFocus.Mode.INPUT, 2);

		// Operation: link the visible variants using a focus from the left input slot.
		AnvilRecipeCategory.FocusLinkedIngredients<TestIngredient> result = AnvilRecipeCategory.getFocusLinkedIngredients(
			leftInputs,
			rightInputs,
			outputs,
			focus,
			ingredientHelper
		);

		// Assertions: every slot is restricted to the focused variant's index.
		assertIngredients(result, new int[]{2}, new int[]{4}, new int[]{6});
	}

	@Test
	public void outputFocusLinksAllCorrelatedSlots() {
		// Setup: every ingredient at one index belongs to the same anvil recipe variant.
		List<TestIngredient> leftInputs = ingredients(1, 2);
		List<TestIngredient> rightInputs = ingredients(3, 4);
		List<TestIngredient> outputs = ingredients(5, 6);
		IFocus<TestIngredient> focus = focus(IFocus.Mode.OUTPUT, 5);

		// Operation: link the visible variants using a focus from the output slot.
		AnvilRecipeCategory.FocusLinkedIngredients<TestIngredient> result = AnvilRecipeCategory.getFocusLinkedIngredients(
			leftInputs,
			rightInputs,
			outputs,
			focus,
			ingredientHelper
		);

		// Assertions: every slot is restricted to the focused variant's index.
		assertIngredients(result, new int[]{1}, new int[]{3}, new int[]{5});
	}

	@Test
	public void nullFocusLeavesSynchronizedCyclingUnchanged() {
		// Setup: all three lists have matching sizes but there is no focus.
		List<TestIngredient> leftInputs = ingredients(1, 2);
		List<TestIngredient> rightInputs = ingredients(3, 4);
		List<TestIngredient> outputs = ingredients(5, 6);

		// Operation: attempt to link the visible variants without a focus.
		AnvilRecipeCategory.FocusLinkedIngredients<TestIngredient> result = AnvilRecipeCategory.getFocusLinkedIngredients(
			leftInputs,
			rightInputs,
			outputs,
			null,
			ingredientHelper
		);

		// Assertions: the category keeps its normal, synchronized cycling path.
		Assert.assertNull(result);
	}

	@Test
	public void linksLeftInputAndOutputWhenRightInputIsConstant() {
		// Setup: the left input and output vary together while the right input is constant.
		List<TestIngredient> leftInputs = ingredients(1, 2);
		List<TestIngredient> rightInputs = ingredients(3);
		List<TestIngredient> outputs = ingredients(4, 5);
		IFocus<TestIngredient> focus = focus(IFocus.Mode.OUTPUT, 5);

		// Operation: link the visible variants using the output focus.
		AnvilRecipeCategory.FocusLinkedIngredients<TestIngredient> result = AnvilRecipeCategory.getFocusLinkedIngredients(
			leftInputs,
			rightInputs,
			outputs,
			focus,
			ingredientHelper
		);

		// Assertions: the linked lists use one index and the constant input is preserved.
		assertIngredients(result, new int[]{2}, new int[]{3}, new int[]{5});
	}

	@Test
	public void linksRightInputAndOutputWhenLeftInputIsConstant() {
		// Setup: the right input and output vary together while the left input is constant.
		List<TestIngredient> leftInputs = ingredients(1);
		List<TestIngredient> rightInputs = ingredients(2, 3);
		List<TestIngredient> outputs = ingredients(4, 5);
		IFocus<TestIngredient> focus = focus(IFocus.Mode.INPUT, 3);

		// Operation: link the visible variants using the right input focus.
		AnvilRecipeCategory.FocusLinkedIngredients<TestIngredient> result = AnvilRecipeCategory.getFocusLinkedIngredients(
			leftInputs,
			rightInputs,
			outputs,
			focus,
			ingredientHelper
		);

		// Assertions: the linked lists use one index and the constant input is preserved.
		assertIngredients(result, new int[]{1}, new int[]{3}, new int[]{5});
	}

	@Test
	public void unequalListsRemainUnlinked() {
		// Setup: none of the supported anvil correlations can describe these list sizes.
		List<TestIngredient> leftInputs = ingredients(1, 2);
		List<TestIngredient> rightInputs = ingredients(3, 4, 5);
		List<TestIngredient> outputs = ingredients(6, 7, 8, 9);
		IFocus<TestIngredient> focus = focus(IFocus.Mode.INPUT, 2);

		// Operation: attempt to link the unrelated ingredient lists.
		AnvilRecipeCategory.FocusLinkedIngredients<TestIngredient> result = AnvilRecipeCategory.getFocusLinkedIngredients(
			leftInputs,
			rightInputs,
			outputs,
			focus,
			ingredientHelper
		);

		// Assertions: the category keeps the legacy independent-focus behavior for unknown shapes.
		Assert.assertNull(result);
	}

	private static List<TestIngredient> ingredients(int... values) {
		List<TestIngredient> ingredients = new ArrayList<>(values.length);
		for (int value : values) {
			ingredients.add(new TestIngredient(value));
		}
		return ingredients;
	}

	private static IFocus<TestIngredient> focus(IFocus.Mode mode, int value) {
		return new IFocus<TestIngredient>() {
			@Override
			public TestIngredient getValue() {
				return new TestIngredient(value);
			}

			@Override
			public Mode getMode() {
				return mode;
			}
		};
	}

	private static void assertIngredients(
		@Nullable AnvilRecipeCategory.FocusLinkedIngredients<TestIngredient> actual,
		int[] expectedLeftInputs,
		int[] expectedRightInputs,
		int[] expectedOutputs
	) {
		Assert.assertNotNull(actual);
		Assert.assertEquals(toList(expectedLeftInputs), getNumbers(actual.getLeftInputs()));
		Assert.assertEquals(toList(expectedRightInputs), getNumbers(actual.getRightInputs()));
		Assert.assertEquals(toList(expectedOutputs), getNumbers(actual.getOutputs()));
	}

	private static List<Integer> getNumbers(List<TestIngredient> ingredients) {
		List<Integer> numbers = new ArrayList<>(ingredients.size());
		for (TestIngredient ingredient : ingredients) {
			numbers.add(ingredient.getNumber());
		}
		return numbers;
	}

	private static List<Integer> toList(int[] values) {
		List<Integer> boxedValues = new ArrayList<>(values.length);
		Arrays.stream(values).forEach(boxedValues::add);
		return boxedValues;
	}
}
