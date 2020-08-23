package mezz.jei.api.recipe.transfer;

import javax.annotation.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.registration.IRecipeTransferRegistration;

/**
 * A recipe transfer handler moves items into a crafting area, based on the items in a recipe.
 *
 * Implementing this interface gives full control over the recipe transfer process.
 * Mods that use a regular slotted inventory can use {@link IRecipeTransferInfo} instead, which is much simpler.
 *
 * Useful functions for implementing a recipe transfer handler can be found in {@link IRecipeTransferHandlerHelper}.
 *
 * To register your recipe transfer handler, use {@link IRecipeTransferRegistration#addRecipeTransferHandler(IRecipeTransferHandler, ResourceLocation)}
 */
public interface IRecipeTransferHandler<C extends Container> {
	/**
	 * The container that this recipe transfer handler can use.
	 */
	Class<C> getContainerClass();

	/**
	 * @param container    the container to act on
	 * @param recipe       the raw recipe instance
	 * @param recipeLayout the layout of the recipe, with information about the ingredients
	 * @param player       the player, to do the slot manipulation
	 * @param maxTransfer  if true, transfer as many items as possible. if false, transfer one set
	 * @param doTransfer   if true, do the transfer. if false, check for errors but do not actually transfer the items
	 * @return a recipe transfer error if the recipe can't be transferred. Return null on success.
	 *
	 * @since JEI 7.1.3
	 */
	@Nullable
	default IRecipeTransferError transferRecipe(C container, Object recipe, IRecipeLayout recipeLayout, PlayerEntity player, boolean maxTransfer, boolean doTransfer) {
		return transferRecipe(container, recipeLayout, player, maxTransfer, doTransfer);
	}

	/**
	 * @param container    the container to act on
	 * @param recipeLayout the layout of the recipe, with information about the ingredients
	 * @param player       the player, to do the slot manipulation
	 * @param maxTransfer  if true, transfer as many items as possible. if false, transfer one set
	 * @param doTransfer   if true, do the transfer. if false, check for errors but do not actually transfer the items
	 * @return a recipe transfer error if the recipe can't be transferred. Return null on success.
	 *
	 * @deprecated since JEI 7.1.3. Use {@link #transferRecipe(Container, Object, IRecipeLayout, PlayerEntity, boolean, boolean)}
	 */
	@Deprecated
	@Nullable
	default IRecipeTransferError transferRecipe(C container, IRecipeLayout recipeLayout, PlayerEntity player, boolean maxTransfer, boolean doTransfer) {
		return null;
	}
}
