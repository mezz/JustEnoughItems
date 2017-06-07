package mezz.jei.api;

/**
 * Gives access to JEI functions that are available once everything has loaded.
 * The IJeiRuntime instance is passed to your mod plugin in {@link IModPlugin#onRuntimeAvailable(IJeiRuntime)}.
 */
public interface IJeiRuntime {
	IRecipeRegistry getRecipeRegistry();

	/**
	 * @since JEI 3.2.12
	 */
	IRecipesGui getRecipesGui();

	/**
	 * @since JEI 4.2.2
	 */
	IIngredientFilter getIngredientFilter();

	/**
	 * @since JEI 4.2.2
	 */
	IIngredientListOverlay getIngredientListOverlay();

	/**
	 * @deprecated since JEI 4.5.0. Use {@link #getIngredientListOverlay()}
	 */
	@Deprecated
	IItemListOverlay getItemListOverlay();
}
