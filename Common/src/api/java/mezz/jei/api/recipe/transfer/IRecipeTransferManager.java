package mezz.jei.api.recipe.transfer;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Optional;

/**
 * Finds and invokes registered recipe transfer handlers.
 *
 * @since 11.5.0
 */
@ApiStatus.NonExtendable
public interface IRecipeTransferManager {
	/**
	 * Get a recipe transfer handler for the given container and recipe category, if one is registered for it.
	 *
	 * @since 11.5.0
	 */
	<C extends AbstractContainerMenu, R> Optional<IRecipeTransferHandler<C, R>> getRecipeTransferHandler(C container, IRecipeCategory<R> recipeCategory);

	/**
	 * Transfers a recipe with its registered handler, or checks whether it can be transferred.
	 *
	 * @param screen the screen for the player's current container
	 * @param recipeLayout the recipe and displayed ingredients to transfer
	 * @param player the player performing the transfer
	 * @param maxTransfer if true, transfer as many items as possible. if false, transfer one set
	 * @param doTransfer if true, do the transfer. if false, check for errors but do not transfer any items
	 * @return a transfer error or warning, or an empty result
	 *
	 * @since 19.73.0
	 */
	<C extends AbstractContainerMenu, R> Optional<IRecipeTransferError> transferRecipe(
		AbstractContainerScreen<C> screen,
		IRecipeLayoutDrawable<R> recipeLayout,
		Player player,
		boolean maxTransfer,
		boolean doTransfer
	);

	/**
	 * Returns the registered recipe transfer listeners.
	 *
	 * @since 19.52.0
	 */
	List<IRecipeTransferListener> getRecipeTransferListeners();
}
