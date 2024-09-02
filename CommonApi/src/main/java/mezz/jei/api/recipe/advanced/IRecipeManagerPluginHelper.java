package mezz.jei.api.recipe.advanced;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeType;

/**
 * Helpers for implementing {@link IRecipeManagerPlugin}s.
 *
 * @since 19.15.1
 */
public interface IRecipeManagerPluginHelper {
	/**
	 * @return true if the given focus should be treated as a catalyst of this recipe type.
	 * @since 19.15.1
	 */
	boolean isRecipeCatalyst(RecipeType<?> recipeType, IFocus<?> focus);
}
