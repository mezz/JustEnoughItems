package mezz.jei.api.recipe.transfer;

import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError.Type;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Helper functions for implementing an {@link IRecipeTransferHandler}.
 * Get an instance from {@link IRecipeTransferRegistration#getTransferHelper()}.
 */
public interface IRecipeTransferHandlerHelper {
	/**
	 * Create an error with {@link Type#INTERNAL}.
	 * It is recommended that you also log a message to the console.
	 */
	IRecipeTransferError createInternalError();

	/**
	 * Create an error with type {@link Type#USER_FACING} that shows a tooltip.
	 *
	 * @param tooltipMessage the message to show on the tooltip for the recipe transfer button.
	 * @since 7.6.4
	 */
	IRecipeTransferError createUserErrorWithTooltip(Component tooltipMessage);

	/**
	 * Create an error with type {@link Type#USER_FACING} that shows a tooltip and highlights missing item slots.
	 *
	 * @param tooltipMessage   the message to show on the tooltip for the recipe transfer button.
	 * @param missingItemSlots the slot indexes for items that are missing. Must not be empty.
	 *
	 * @since 9.3.0
	 */
	IRecipeTransferError createUserErrorForMissingSlots(Component tooltipMessage, Collection<IRecipeSlotView> missingItemSlots);

	/**
	 * Helper to create a basic recipe transfer info, that works the same as the ones created by
	 * {@link IRecipeTransferRegistration#addRecipeTransferHandler}
	 *
	 * This is useful for implementing your own recipe transfer logic that slightly tweaks the basic logic.
	 *
	 * @see #createUnregisteredRecipeTransferHandler(IRecipeTransferInfo)
	 *
	 * @since 11.3.0
	 */
	<C extends AbstractContainerMenu, R> IRecipeTransferInfo<C, R> createBasicRecipeTransferInfo(
		Class<? extends C> containerClass,
		@Nullable MenuType<C> menuType,
		RecipeType<R> recipeType,
		int recipeSlotStart,
		int recipeSlotCount,
		int inventorySlotStart,
		int inventorySlotCount
	);

	/**
	 * Create an unregistered recipe transfer handler that uses JEI's default transfer logic.
	 * This is useful for implementing your own recipe transfer logic that slightly tweaks the default logic.
	 *
	 * @since 11.3.0
	 */
	<C extends AbstractContainerMenu, R> IRecipeTransferHandler<C, R> createUnregisteredRecipeTransferHandler(IRecipeTransferInfo<C, R> recipeTransferInfo);

	/**
	 * Create a recipe slots view from a list of slot views.
	 * This is useful for altering the slot results from other recipe transfer handlers.
	 *
	 * @since 11.3.0
	 */
	IRecipeSlotsView createRecipeSlotsView(List<IRecipeSlotView> slotViews);

	/**
	 * @return true if JEI is currently present on the server and supports recipe transfer.
	 *
	 * @since 11.3.0
	 */
	boolean recipeTransferHasServerSupport();
}
