package mezz.jei.api.recipe.transfer;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * A recipe transfer handler moves items into a crafting area, based on the items in a recipe.
 *
 * Implementing this interface gives full control over the recipe transfer process.
 * Mods that use a regular slotted inventory can use {@link IRecipeTransferInfo} instead, which is much simpler.
 *
 * Useful functions for implementing a recipe transfer handler can be found in {@link IRecipeTransferHandlerHelper}.
 *
 * To register your recipe transfer handler, use {@link IRecipeTransferRegistration#addRecipeTransferHandler(IRecipeTransferHandler, RecipeType)}
 */
public interface IRecipeTransferHandler<C extends AbstractContainerMenu, R> {

	/**
	 * The container that this recipe transfer handler can use.
	 */
	Class<? extends C> getContainerClass();

	/**
	 * Return the optional menu type that this recipe transfer helper supports.
	 * This is used to optionally narrow down the type of container handled by this recipe transfer handler.
	 */
	Optional<MenuType<C>> getMenuType();

	/**
	 * The recipe that this recipe transfer handler can use.
	 */
	RecipeType<R> getRecipeType();

	/**
	 * @param container   the container to act on
	 * @param recipe      the raw recipe instance
	 * @param recipeSlots the view of the recipe slots, with information about the ingredients
	 * @param player      the player, to do the slot manipulation
	 * @param maxTransfer if true, transfer as many items as possible. if false, transfer one set
	 * @param doTransfer  if true, do the transfer. if false, check for errors but do not actually transfer the items
	 * @return a recipe transfer error if the recipe can't be transferred. Return null on success.
	 *
	 * @since 9.3.0
	 */
	@Nullable
	IRecipeTransferError transferRecipe(C container, R recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer);
}
