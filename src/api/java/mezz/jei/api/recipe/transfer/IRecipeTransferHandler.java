package mezz.jei.api.recipe.transfer;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import mezz.jei.api.gui.IRecipeLayout;

/**
 * A recipe transfer handler moves items into a crafting area, based on the items in a recipe.
 * <p>
 * Implementing this interface gives full control over the recipe transfer process.
 * Mods that use a regular slotted inventory can use {@link IRecipeTransferInfo} instead, which is much simpler.
 * <p>
 * Useful functions for implementing a recipe transfer handler can be found in {@link IRecipeTransferHandlerHelper}.
 * <p>
 * To register your recipe transfer handler, use {@link IRecipeTransferRegistry#addRecipeTransferHandler(IRecipeTransferHandler, String)}.
 */
public interface IRecipeTransferHandler<C extends Container> {
	/**
	 * The container that this recipe transfer handler can use.
	 */
	Class<C> getContainerClass();

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
