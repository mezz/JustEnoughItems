package mezz.jei.test;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.ingredients.TypedIngredient;
import mezz.jei.library.focus.Focus;
import mezz.jei.library.focus.FocusGroup;
import mezz.jei.library.gui.recipes.supplier.builder.IngredientSupplierBuilder;
import mezz.jei.library.ingredients.RecipeIngredientSupplier;
import mezz.jei.library.ingredients.RecipeIngredientSupplier.FocusLink;
import org.junit.jupiter.api.Test;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class RecipeFocusLinkTest {
	private static final IIngredientType<String> INGREDIENT_TYPE = () -> String.class;

	@Test
	void keepsOnlyIndexesVisibleInEveryLinkedSlot() {
		FocusLink focusLink = createFocusLink(
			createIngredients("hidden", "first", "second"),
			createIngredients("a", "b", "hidden")
		);

		Set<Integer> result = focusLink.getVisibleIngredientIndexes(
			FocusGroup.EMPTY,
			createUnusedIngredientManager(),
			RecipeFocusLinkTest::isVisible
		);

		assertEquals(Set.of(1), result);
	}

	@Test
	void appliesVisibilityToFocusedIndexes() {
		FocusLink focusLink = createFocusLink(
			createIngredients("focused", "focused", "not focused"),
			createIngredients("hidden", "visible", "other")
		);
		Focus<String> focus = new Focus<>(RecipeIngredientRole.INPUT, createTypedIngredient("focused"));

		Set<Integer> result = focusLink.getVisibleIngredientIndexes(
			focus,
			createIngredientManagerForFocusMatching(),
			RecipeFocusLinkTest::isVisible
		);

		assertEquals(Set.of(1), result);
	}

	@Test
	void hidesRecipeWhenEveryLinkedPairContainsAHiddenIngredient() {
		FocusLink focusLink = createFocusLink(
			createIngredients("hidden", "visible"),
			createIngredients("visible", "hidden")
		);

		var result = focusLink.getVisibleIngredientIndexes(
			FocusGroup.EMPTY,
			createUnusedIngredientManager(),
			RecipeFocusLinkTest::isVisible
		);

		assertNull(result);
	}

	@Test
	void blankIngredientsRemainLinked() {
		List<@Nullable ITypedIngredient<?>> firstSlot = Arrays.asList(null, createIngredient("hidden"));
		FocusLink focusLink = createFocusLink(
			firstSlot,
			createIngredients("visible", "visible")
		);

		Set<Integer> result = focusLink.getVisibleIngredientIndexes(
			FocusGroup.EMPTY,
			createUnusedIngredientManager(),
			RecipeFocusLinkTest::isVisible
		);

		assertEquals(Set.of(0), result);
	}

	@Test
	void leavesUnfocusedVisibleSlotsUnrestricted() {
		FocusLink focusLink = createFocusLink(
			createIngredients("first", "second"),
			createIngredients("a", "b")
		);

		Set<Integer> result = focusLink.getVisibleIngredientIndexes(
			FocusGroup.EMPTY,
			createUnusedIngredientManager(),
			RecipeFocusLinkTest::isVisible
		);

		assertEquals(Set.of(), result);
	}

	@Test
	void ingredientSupplierRecordsFocusLinksForRecipeVisibility() {
		IIngredientManager ingredientManager = createIngredientManagerForFocusMatching();
		IngredientSupplierBuilder builder = new IngredientSupplierBuilder(ingredientManager);
		var firstSlot = builder.addSlot(RecipeIngredientRole.INPUT)
			.addTypedIngredient(createTypedIngredient("hidden"))
			.addTypedIngredient(createTypedIngredient("visible"));
		var secondSlot = builder.addSlot(RecipeIngredientRole.OUTPUT)
			.addTypedIngredient(createTypedIngredient("visible"))
			.addTypedIngredient(createTypedIngredient("hidden"));
		builder.createFocusLink(firstSlot, secondSlot);
		RecipeIngredientSupplier ingredientSupplier = builder.buildIngredientSupplier();

		var visibleIndexes = ingredientSupplier.getFocusLinks()
			.get(0)
			.getVisibleIngredientIndexes(FocusGroup.EMPTY, ingredientManager, RecipeFocusLinkTest::isVisible);

		assertNull(visibleIndexes);
	}

	private static FocusLink createFocusLink(
		List<@Nullable ITypedIngredient<?>> inputIngredients,
		List<@Nullable ITypedIngredient<?>> outputIngredients
	) {
		return new FocusLink(List.of(
			new FocusLink.Slot(RecipeIngredientRole.INPUT, inputIngredients),
			new FocusLink.Slot(RecipeIngredientRole.OUTPUT, outputIngredients)
		));
	}

	private static List<@Nullable ITypedIngredient<?>> createIngredients(String... ingredients) {
		return Arrays.stream(ingredients)
			.<@Nullable ITypedIngredient<?>>map(RecipeFocusLinkTest::createIngredient)
			.toList();
	}

	private static ITypedIngredient<String> createIngredient(String ingredient) {
		return createTypedIngredient(ingredient);
	}

	private static ITypedIngredient<String> createTypedIngredient(String ingredient) {
		return TypedIngredient.createUnvalidated(INGREDIENT_TYPE, ingredient);
	}

	private static boolean isVisible(ITypedIngredient<?> ingredient) {
		return !ingredient.getIngredient().equals("hidden");
	}

	private static IIngredientManager createUnusedIngredientManager() {
		return (IIngredientManager) Proxy.newProxyInstance(
			IIngredientManager.class.getClassLoader(),
			new Class<?>[]{IIngredientManager.class},
			(proxy, method, args) -> {
				throw new AssertionError("Ingredient manager method should not be called: " + method.getName());
			}
		);
	}

	private static IIngredientManager createIngredientManagerForFocusMatching() {
		IIngredientHelper<?> ingredientHelper = (IIngredientHelper<?>) Proxy.newProxyInstance(
			IIngredientHelper.class.getClassLoader(),
			new Class<?>[]{IIngredientHelper.class},
			(proxy, method, args) -> {
				if (method.getName().equals("isValidIngredient")) {
					return true;
				}
				if (method.getName().equals("getUid")) {
					Object ingredient = args[0];
					if (ingredient instanceof ITypedIngredient<?> typedIngredient) {
						return typedIngredient.getIngredient();
					}
					return ingredient;
				}
				throw new AssertionError("Ingredient helper method should not be called: " + method.getName());
			}
		);
		return (IIngredientManager) Proxy.newProxyInstance(
			IIngredientManager.class.getClassLoader(),
			new Class<?>[]{IIngredientManager.class},
			(proxy, method, args) -> {
				if (method.getName().equals("getIngredientHelper")) {
					return ingredientHelper;
				}
				throw new AssertionError("Ingredient manager method should not be called: " + method.getName());
			}
		);
	}
}
