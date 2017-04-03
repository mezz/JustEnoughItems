package mezz.jei.api.recipe;

/**
 * Converts recipes to {@link IRecipeWrapper}.
 * Using Java 8 you can define this using syntax like this: MyRecipeWrapper::new
 *
 * @param <T> the recipe type
 * @since JEI 4.3.0
 */
//TODO Java 8, add @FunctionalInterface
public interface IRecipeWrapperFactory<T> {
	/**
	 * Returns a recipe wrapper for the given recipe.
	 */
	IRecipeWrapper getRecipeWrapper(T recipe);
}
