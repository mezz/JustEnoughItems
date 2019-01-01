package mezz.jei.recipes;

import net.minecraft.util.ResourceLocation;

import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

/**
 * An IRecipeHandler provides information about one Recipe Class.
 *
 * Its main purpose is to tie recipe classes to an {@link IRecipeCategory}
 * and convert recipes to {@link IRecipeWrapper} with {@link #getRecipeWrapper(Object)}.
 */
public interface IRecipeHandler<T> {
	/**
	 * Returns the class of the Recipe handled by this IRecipeHandler.
	 */
	Class<T> getRecipeClass();

	/**
	 * Returns this recipe's unique category id.
	 *
	 * @see IRecipeCategory#getUid()
	 * @see VanillaRecipeCategoryUid
	 * @since 3.5.0
	 */
	ResourceLocation getRecipeCategoryUid(T recipe);

	/**
	 * Returns a recipe wrapper for the given recipe.
	 */
	IRecipeWrapper getRecipeWrapper(T recipe);
}
