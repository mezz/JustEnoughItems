package mezz.jei.gui.input;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.util.FocusUtil;

import java.util.List;

public interface IClickableIngredientInternal<T> {
	ITypedIngredient<T> getTypedIngredient();

	IElement<T> getElement();

	boolean isMouseOver(double mouseX, double mouseY);

	/**
	 * Whether ordinary cheat controls may give ingredients from this slot.
	 * Recipe and inventory slots reserve these controls for other actions.
	 */
	boolean canClickToCheat();

	/**
	 * Whether JEI may use Minecraft's normal item controls to open recipes or usages for this ingredient.
	 * For example, a player left-clicking an inventory slot should pick up its stack.
	 * Left-clicking an ingredient in JEI's recipe display can instead show that ingredient's recipes.
	 */
	boolean canClickToFocus();

	/**
	 * Open recipes or usages for this ingredient.
	 */
	default void show(IRecipesGui recipesGui, FocusUtil focusUtil, List<RecipeIngredientRole> roles) {
		getElement().show(recipesGui, focusUtil, roles);
	}
}
