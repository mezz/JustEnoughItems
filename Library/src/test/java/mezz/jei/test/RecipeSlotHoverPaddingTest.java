package mezz.jei.test;

import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.library.focus.FocusGroup;
import mezz.jei.library.gui.ingredients.ICycler;
import mezz.jei.library.gui.recipes.layout.builder.RecipeSlotBuilder;
import mezz.jei.library.ingredients.IIngredientManagerInternal;
import net.minecraft.util.context.ContextMap;
import org.junit.jupiter.api.Test;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RecipeSlotHoverPaddingTest {
	@Test
	void hoverPaddingRemainsRelativeWhenSlotMoves() {
		RecipeSlotBuilder builder = new RecipeSlotBuilder(
			createUnusedIngredientManager(),
			ContextMap.EMPTY,
			0,
			RecipeIngredientRole.INPUT
		);
		builder
			.setPosition(10, 20)
			.setHoverPadding(1, 1, 1, 1);

		IRecipeSlotDrawable slot = builder.build(Set.of(), FocusGroup.EMPTY, createUnusedCycler()).second();

		assertTrue(slot.isMouseOver(9, 19));
		assertTrue(slot.isMouseOver(26.99, 36.99));
		assertFalse(slot.isMouseOver(8.99, 19));
		assertFalse(slot.isMouseOver(27, 37));

		slot.setPosition(30, 40);

		assertFalse(slot.isMouseOver(9, 19));
		assertTrue(slot.isMouseOver(29, 39));
		assertTrue(slot.isMouseOver(46.99, 56.99));
		assertFalse(slot.isMouseOver(47, 57));
	}

	private static IIngredientManagerInternal createUnusedIngredientManager() {
		return (IIngredientManagerInternal) Proxy.newProxyInstance(
			IIngredientManagerInternal.class.getClassLoader(),
			new Class<?>[]{IIngredientManagerInternal.class},
			(proxy, method, args) -> {
				throw new AssertionError("Ingredient manager method should not be called: " + method.getName());
			}
		);
	}

	private static ICycler createUnusedCycler() {
		return new ICycler() {
			@Override
			public <T> Optional<T> getCycled(List<@Nullable T> list) {
				throw new AssertionError("Cycler should not be called");
			}
		};
	}
}
