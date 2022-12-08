package mezz.jei.api.runtime;

import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.RecipeType;

import java.util.List;
import java.util.Optional;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeIngredientRole;

import mezz.jei.api.recipe.IFocus;

/**
 * JEI's gui for displaying recipes. Use this interface to open recipes.
 * Get the instance from {@link IJeiRuntime#getRecipesGui()}.
 */
public interface IRecipesGui {
	/**
	 * Show recipes for an {@link IFocus}.
	 * Opens the {@link IRecipesGui} if recipes are found and the gui is closed.
	 *
	 * @see IFocusFactory#createFocus(RecipeIngredientRole, IIngredientType, Object)
	 */
	default <V> void show(IFocus<V> focus) {
		show(List.of(focus));
	}

	/**
	 * Show recipes for multiple {@link IFocus}.
	 * Opens the {@link IRecipesGui} if recipes are found and the gui is closed.
	 *
	 * @see IFocusFactory#createFocus(RecipeIngredientRole, IIngredientType, Object)
	 *
	 * @since 9.3.0
	 */
	void show(List<IFocus<?>> focuses);

	/**
	 * Show entire categories of recipes.
	 *
	 * @param recipeTypes a list of recipe types to display, in order. Must not be empty.
	 */
	void showTypes(List<RecipeType<?>> recipeTypes);

	/**
	 * @return the ingredient that's currently under the mouse in this gui
	 */
	<T> Optional<T> getIngredientUnderMouse(IIngredientType<T> ingredientType);
}
