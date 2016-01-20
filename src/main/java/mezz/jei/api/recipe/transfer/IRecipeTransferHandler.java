package mezz.jei.api.recipe.transfer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.gui.RecipeLayout;

/**
 * A recipe transfer handler moves items into a crafting area, based on the items in a recipe.
 *
 * Implementing this interface gives full control over the recipe transfer process.
 * Mods that use a regular slotted inventory can use IRecipeTransferInfo instead, which is much simpler.
 */
public interface IRecipeTransferHandler {
	/**
	 * The container that this recipe transfer handler can use.
	 */
	Class<? extends Container> getContainerClass();

	/**
	 * The type of recipe that this recipe transfer handler deals with.
	 */
	String getRecipeCategoryUid();

	/**
	 * @deprecated since JEI 2.20.0, use the version with the maxTransfer parameter
	 */
	@Deprecated
	IRecipeTransferError transferRecipe(@Nonnull Container container, @Nonnull RecipeLayout recipeLayout, @Nonnull EntityPlayer player, boolean doTransfer);

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
	IRecipeTransferError transferRecipe(@Nonnull Container container, @Nonnull IRecipeLayout recipeLayout, @Nonnull EntityPlayer player, boolean maxTransfer, boolean doTransfer);
}
