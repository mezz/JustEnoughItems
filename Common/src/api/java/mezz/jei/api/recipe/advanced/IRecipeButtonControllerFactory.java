package mezz.jei.api.recipe.advanced;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.buttons.IIconButtonController;
import org.jspecify.annotations.Nullable;

/**
 * Factory for creating {@link IIconButtonController} instances for recipe layouts.
 *
 * <p>
 * Implementations are used to create button controllers that are
 * drawn next to a specific {@link IRecipeLayoutDrawable} instance.
 * </p>
 *
 * <p>
 * A new controller instance is expected to be created for each recipe layout
 * to allow the controller to store per-recipe state if needed.
 * </p>
 *
 * @since 27.2.0
 */
public interface IRecipeButtonControllerFactory {
	/**
	 * Creates a button controller for the given recipe layout.
	 *
	 * <p>
	 * This method is called when a recipe layout is created.
	 * The returned controller will be used to manage button behavior
	 * and state for the lifetime of the recipe layout.
	 * </p>
	 *
	 * @param recipeLayoutDrawable the recipe layout this controller is associated with
	 * @param <T> the recipe type
	 *
	 * @return a new button controller for the given recipe layout
	 *
	 * @since 27.2.0
	 */
	@Nullable
	<T> IIconButtonController createButtonController(IRecipeLayoutDrawable<T> recipeLayoutDrawable);
}
