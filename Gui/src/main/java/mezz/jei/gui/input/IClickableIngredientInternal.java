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
