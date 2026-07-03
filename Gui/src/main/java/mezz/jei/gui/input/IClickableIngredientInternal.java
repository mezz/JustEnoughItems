package mezz.jei.gui.input;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.util.FocusUtil;
import net.minecraft.world.item.ItemStack;

import java.util.List;


public interface IClickableIngredientInternal<T> {
	ITypedIngredient<T> getTypedIngredient();

	IElement<T> getElement();

	boolean isMouseOver(double mouseX, double mouseY);

	/**
	 * Returns an ItemStack if this clickable slot allows players to cheat ingredients from it
	 * (when the server has granted them permission to cheat).
	 *
	 * Returns an empty ItemStack if cheating is not allowed.
	 *
	 * This is generally only active in the JEI ingredient list and bookmark list.
	 */
	ItemStack getCheatItemStack(IIngredientManager ingredientManager);

	/**
	 * Most GUIs shouldn't allow JEI to click to set the focus,
	 * because it would conflict with their normal behavior.
	 *
	 * JEI's recipe GUI has clickable slots that do allow click to focus,
	 * in order to let players navigate recipes.
	 */
	boolean canClickToFocus();

	/**
	 * Open recipes or usages for this ingredient.
	 */
	default void show(IRecipesGui recipesGui, FocusUtil focusUtil, List<RecipeIngredientRole> roles) {
		getElement().show(recipesGui, focusUtil, roles);
	}
}
