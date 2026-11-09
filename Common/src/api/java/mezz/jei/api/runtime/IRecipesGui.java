package mezz.jei.api.runtime;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Optional;

/**
 * JEI's gui for displaying recipes. Use this interface to open recipes.
 * Get the instance from {@link IJeiRuntime#getRecipesGui()}.
 */
@ApiStatus.NonExtendable
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
	 * Returns the focuses for the current view, or an empty list when closed.
	 * Do not modify the ingredients in the returned focuses.
	 * @since 31.19.0
	 */
	@Unmodifiable
	List<IFocus<?>> getFocuses();

	/**
	 * Show entire categories of recipes.
	 *
	 * @param recipeTypes a list of recipe types to display, in order. Must not be empty.
	 */
	void showTypes(List<IRecipeType<?>> recipeTypes);

	/**
	 * Returns the categories available in the current view, in display order, or an empty list when closed.
	 * @since 31.19.0
	 */
	@Unmodifiable
	List<IRecipeCategory<?>> getRecipeCategories();

	/**
	 * Returns the selected category, or an empty result when closed.
	 * @since 31.19.0
	 */
	Optional<IRecipeCategory<?>> getSelectedRecipeCategory();

	/**
	 * Selects a category already available in the current view, preserving its focuses.
	 * @return true if the category is now selected, false if the view is closed or the category is unavailable
	 * @since 31.19.0
	 */
	boolean setRecipeCategory(IRecipeType<?> recipeType);

	/**
	 * Show specific recipes for one recipe category, with multiple {@link IFocus}.
	 * Opens the {@link IRecipesGui} if recipes are valid and the gui is closed.
	 *
	 * @see IFocusFactory#createFocus(RecipeIngredientRole, IIngredientType, Object)
	 *
	 * @since 19.1.0
	 */
	<T> void showRecipes(IRecipeCategory<T> recipeCategory, List<T> recipes, List<IFocus<?>> focuses);

	/**
	 * Returns the zero-based page index within the selected category, or {@code -1} when closed.
	 * In scrolling mode, this is the page containing the first visible recipe.
	 * @since 31.19.0
	 */
	int getPageIndex();

	/**
	 * Returns the number of pages in the selected category, or zero when closed.
	 * @since 31.19.0
	 */
	int getPageCount();

	/**
	 * Returns the visible recipe layouts in display order, or an empty list when closed.
	 * Partially clipped layouts are included. Their bounds use GUI-scaled screen coordinates.
	 * Do not reposition or tick the returned layouts.
	 * Reacquire the layouts after navigating, resizing, or reloading.
	 * @since 31.19.0
	 */
	@Unmodifiable
	List<IRecipeLayoutDrawable<?>> getVisibleRecipeLayouts();

	/**
	 * @return the ingredient that's currently under the mouse in this gui
	 */
	<T> Optional<T> getIngredientUnderMouse(IIngredientType<T> ingredientType);

	/**
	 * Returns whether the recipe view is the current screen.
	 * @since 31.19.0
	 */
	boolean isOpen();

	/**
	 * Get the screen that the {@link IRecipesGui} was opened from.
	 * When the {@link IRecipesGui} is closed, it will re-open the parent screen.
	 *
	 * If the {@link IRecipesGui} is not open, this will return {@link Optional#empty()}.
	 *
	 * @since 19.20.0
	 */
	Optional<Screen> getParentScreen();
}
