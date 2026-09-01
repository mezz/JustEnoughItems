package mezz.jei.api.recipe.transfer;

/**
 * The result reported when a recipe transfer has finished.
 *
 * @since 11.62.0
 */
public enum RecipeTransferResult {
	/**
	 * The recipe transfer completed successfully.
	 */
	SUCCESS,

	/**
	 * The recipe transfer was rejected.
	 */
	REJECTED
}
