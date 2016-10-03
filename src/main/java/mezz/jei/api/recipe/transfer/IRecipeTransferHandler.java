package mezz.jei.api.recipe.transfer;

import javax.annotation.Nullable;

import mezz.jei.api.gui.IRecipeLayout;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

/**
 * A recipe transfer handler moves items into a crafting area, based on the items in a recipe.
 *
 * Implementing this interface gives full control over the recipe transfer process.
 * Mods that use a regular slotted inventory can use {@link IRecipeTransferInfo} instead, which is much simpler.
 *
 * Useful functions for implementing a recipe transfer handler can be found in {@link IRecipeTransferHandlerHelper}.
 */
public interface IRecipeTransferHandler<C extends Container> {
	/**
	 * The container that this recipe transfer handler can use.
	 */
	Class<C> getContainerClass();

	/**
	 * The type of recipe that this recipe transfer handler deals with.
	 *
	 * @deprecated since JEI 3.12.4.
	 * Use the recipe-category-specific registration {@link IRecipeTransferRegistry#addRecipeTransferHandler(IRecipeTransferHandler, String)}
	 */
	@Deprecated
	String getRecipeCategoryUid();

	/**
	 * @param container    the container to act on
	 * @param recipeLayout the layout of the recipe, with information about the ingredients
	 * @param player       the player, to do the slot manipulation
	 * @param maxTransfer  if true, transfer as many items as possible. if false, transfer one set
	 * @param doTransfer   if true, do the transfer. if false, check for errors but do not actually transfer the items
	 * @return a recipe transfer error if the recipe can't be transferred. Return null on success.
	 * @since JEI 2.20.0
	 */
	@Nullable
	IRecipeTransferError transferRecipe(C container, IRecipeLayout recipeLayout, EntityPlayer player, boolean maxTransfer, boolean doTransfer);
}
