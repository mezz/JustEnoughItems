package mezz.jei.api.recipe.advanced;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeType;
import org.jetbrains.annotations.ApiStatus;

/**
 * Helpers for implementing {@link IRecipeManagerPlugin}s.
 *
 * @since 10.41.0
 */
@ApiStatus.NonExtendable
public interface IRecipeManagerPluginHelper {
	/**
	 * @return true if the given focus should be treated as a catalyst of this recipe type.
	 * @since 10.41.0
	 */
	boolean isRecipeCatalyst(RecipeType<?> recipeType, IFocus<?> focus);
}
