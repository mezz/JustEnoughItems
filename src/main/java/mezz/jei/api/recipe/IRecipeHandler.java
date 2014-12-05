package mezz.jei.api.recipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An IRecipeHandler provides information about one Recipe Class.
 * This class must not import anything that could be missing at runtime (i.e. code from any mod).
 */
public interface IRecipeHandler {

	/**
	 *  Returns the class of the Recipe handled by this IRecipeHandler.
	 *  To avoid importing classes, use Class.forName() to get the recipe class.
	 *  Returns null if the class is not available.
	 */
	@Nullable Class getRecipeClass();

	/* Returns the category of this recipe. */
	Class<? extends IRecipeCategory> getRecipeCategoryClass();

	/* Returns a recipe wrapper for the given recipe. */
	IRecipeWrapper getRecipeWrapper(@Nonnull Object recipe);

}
