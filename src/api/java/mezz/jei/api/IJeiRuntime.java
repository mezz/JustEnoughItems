package mezz.jei.api;

/**
 * Gives access to JEI functions that are available once everything has loaded.
 * The IJeiRuntime instance is passed to your mod plugin in {@link IModPlugin#onRuntimeAvailable(IJeiRuntime)}.
 */
public interface IJeiRuntime {
	IRecipeRegistry getRecipeRegistry();

	IItemListOverlay getItemListOverlay();

	/**
	 * @since JEI 3.2.12
	 */
	IRecipesGui getRecipesGui();
}
