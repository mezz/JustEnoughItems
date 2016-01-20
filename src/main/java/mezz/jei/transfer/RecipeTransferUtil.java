package mezz.jei.transfer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import mezz.jei.Internal;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.gui.RecipeLayout;
import mezz.jei.util.Log;

public class RecipeTransferUtil {
	public static IRecipeTransferError getTransferRecipeError(@Nonnull RecipeLayout recipeLayout, @Nonnull EntityPlayer player) {
		return transferRecipe(recipeLayout, player, false, false);
	}

	public static boolean transferRecipe(@Nonnull RecipeLayout recipeLayout, @Nonnull EntityPlayer player, boolean maxTransfer) {
		IRecipeTransferError error = transferRecipe(recipeLayout, player, maxTransfer, true);
		return error == null;
	}

	@Nullable
	private static IRecipeTransferError transferRecipe(@Nonnull RecipeLayout recipeLayout, @Nonnull EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
		Container container = player.openContainer;

		IRecipeTransferHandler transferHandler = Internal.getRecipeRegistry().getRecipeTransferHandler(container, recipeLayout.getRecipeCategory());
		if (transferHandler == null) {
			if (doTransfer) {
				Log.error("No Recipe Transfer handler for container {}", container.getClass());
			}
			return RecipeTransferErrorInternal.instance;
		}

		try {
			return transferHandler.transferRecipe(container, recipeLayout, player, maxTransfer, doTransfer);
		} catch (AbstractMethodError ignored) {
			// older transferHandlers do not have the new method
			return transferHandler.transferRecipe(container, recipeLayout, player, doTransfer);
		}
	}
}
