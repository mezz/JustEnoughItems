package mezz.jei.api.recipe.transfer;

import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError.Type;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jspecify.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Helper functions for implementing an {@link IRecipeTransferHandler}.
 * Get an instance from {@link IRecipeTransferRegistration#getTransferHelper()}.
 */
@ApiStatus.NonExtendable
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
		IRecipeType<R> recipeType,
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
	 * Try transferring a recipe with alternative sets of inputs.
	 * Each {@link IRecipeSlotsView} describes one complete input alternative. They are checked in order without
	 * transferring, and the first successful alternative is used for the transfer. If every alternative fails,
	 * the error from the first one is returned. If there are no alternatives, the original recipe slots from the
	 * context are used.
	 *
	 * @param recipeTransferHandler the handler to check and perform the transfer, typically one created by
	 * {@link #createUnregisteredRecipeTransferHandler(IRecipeTransferInfo)}
	 * @param context the original recipe transfer context
	 * @param inputAlternatives complete sets of recipe inputs to try, in priority order
	 * @param doTransfer if true, transfer the first successful alternative; if false, only check for errors
	 * @return a recipe transfer error if none of the alternatives can be transferred. Return null on success.
	 *
	 * @since 29.41.0
	 */
	<C extends AbstractContainerMenu, R> @Nullable IRecipeTransferError transferRecipeWithInputAlternatives(
		IRecipeTransferHandler<C, R> recipeTransferHandler,
		IRecipeTransferContext<R, C> context,
		List<IRecipeSlotsView> inputAlternatives,
		boolean doTransfer
	);

	/**
	 * Create a recipe slots view from a list of slot views.
	 * This is useful for altering the slot results from other recipe transfer handlers.
	 *
	 * @since 11.3.0
	 */
	IRecipeSlotsView createRecipeSlotsView(List<IRecipeSlotView> slotViews);

	/**
	 * Create a copy of a recipe slot view with replacement item stacks.
	 * The copy retains the original slot's role, name, and error highlight behavior.
	 *
	 * This is useful together with {@link #createRecipeSlotsView(List)} when creating input alternatives for
	 * {@link #transferRecipeWithInputAlternatives(IRecipeTransferHandler, IRecipeTransferContext, List, boolean)}.
	 *
	 * @since 29.41.0
	 */
	IRecipeSlotView copyWithIngredients(IRecipeSlotView recipeSlot, List<ItemStack> ingredients);

	/**
	 * @return true if JEI is currently present on the server and supports recipe transfer.
	 *
	 * @since 11.3.0
	 */
	boolean recipeTransferHasServerSupport();

	/**
	 * Get the original ingredients from a crafting recipe, indexed by the original gui slots and
	 * not the {@link IRecipeSlotsView} indexes JEI uses.
	 * Used to help with optimizing crafting recipe transfer and for displaying errors.
	 *
	 * These indexes do not always match because JEI places smaller recipes into the 3x3 crafting grid
	 * so that they are centered horizontally, and some will be aligned to the bottom. (i.e. slab recipes).
	 * This logic is controlled by {@link ICraftingGridHelper}.
	 *
	 * @since 20.0.0
	 */
	Map<Integer, SlotDisplay> getGuiSlotIndexToIngredientMap(RecipeHolder<CraftingRecipe> recipeHolder);
}
