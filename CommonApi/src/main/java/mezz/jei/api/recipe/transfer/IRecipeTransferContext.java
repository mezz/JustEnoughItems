package mezz.jei.api.recipe.transfer;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.ApiStatus;

/**
 * Information about a recipe transfer or transfer check requested by the player.
 *
 * @param <R> the recipe type
 * @param <C> the container type
 *
 * @since 11.62.0
 */
@ApiStatus.NonExtendable
public interface IRecipeTransferContext<R, C extends AbstractContainerMenu> {
	/**
	 * An id that identifies this recipe transfer while its work is in progress.
	 *
 * @since 11.62.0
	 */
	int getTransferId();

	/**
	 * Reports that this recipe transfer has finished.
	 * Call this after completing a transfer requested with {@code doTransfer} set to true.
	 *
	 * @param result the result of the transfer
	 *
 * @since 11.62.0
	 */
	void completeRecipeTransfer(RecipeTransferResult result);

	/**
	 * The recipe being transferred.
	 *
 * @since 11.62.0
	 */
	R getRecipe();

	/**
	 * The type of the recipe being transferred.
	 *
 * @since 11.62.0
	 */
	RecipeType<R> getRecipeType();

	/**
	 * The container that the recipe is being transferred into.
	 *
 * @since 11.62.0
	 */
	C getContainer();

	/**
	 * The screen for the container that the recipe is being transferred into.
	 *
 * @since 11.62.0
	 */
	AbstractContainerScreen<C> getScreen();

	/**
	 * A view of the recipe slots and their displayed ingredients.
	 *
 * @since 11.62.0
	 */
	IRecipeSlotsView getRecipeSlots();

	/**
	 * The player who requested the transfer.
	 *
 * @since 11.62.0
	 */
	Player getPlayer();

	/**
	 * Returns true when the player requested that as many recipe sets as possible are transferred.
	 *
 * @since 11.62.0
	 */
	boolean isMaxTransfer();
}
