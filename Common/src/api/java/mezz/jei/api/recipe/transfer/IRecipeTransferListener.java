package mezz.jei.api.recipe.transfer;

import mezz.jei.api.registration.IRecipeTransferRegistration;

/**
 * Observes recipe transfers requested through JEI.
 *
 * Register a listener with {@link IRecipeTransferRegistration#addRecipeTransferListener(IRecipeTransferListener)}.
 *
 * @since 30.30.0
 */
public interface IRecipeTransferListener {
	/**
	 * Called once when JEI attempts a recipe transfer, immediately before JEI invokes the selected transfer handler.
	 * This is not called while JEI is only checking whether a recipe can be transferred.
	 *
	 * @since 30.30.0
	 */
	default void beforeRecipeTransfer(IRecipeTransferContext<?, ?> context) {

	}

	/**
	 * Called after the selected recipe transfer handler reports that the transfer has finished.
	 * The context is the same one passed to {@link #beforeRecipeTransfer(IRecipeTransferContext)}.
	 *
	 * @since 30.30.0
	 */
	default void afterRecipeTransfer(IRecipeTransferContext<?, ?> context, RecipeTransferResult result) {

	}
}
