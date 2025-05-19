package mezz.jei.api.recipe;

import mezz.jei.api.recipe.types.IRecipeType;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * This is a helper class for looking up recipes.
 * Create one with {@link IRecipeManager#createRecipeLookup(IRecipeType)},
 * then set its properties and call {@link #get()} to get the results.
 *
 * @since 9.5.0
 */
public interface IRecipeLookup<R> {
	/**
	 * Limit the results to only recipes matching the given focuses.
	 *
	 * @since 9.5.0
	 */
	IRecipeLookup<R> limitFocus(Collection<? extends IFocus<?>> focuses);

	/**
	 * By default, hidden results are not returned.
	 * Calling this will make this lookup include hidden recipes.
	 *
	 * @since 9.5.0
	 */
	IRecipeLookup<R> includeHidden();

	/**
	 * Get the recipe results for this lookup.
	 *
	 * @since 9.5.0
	 */
	Stream<R> get();
}
