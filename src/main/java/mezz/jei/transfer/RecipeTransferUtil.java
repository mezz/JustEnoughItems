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
	public static IRecipeTransferError getTransferRecipeError(@Nonnull Container container, @Nonnull RecipeLayout recipeLayout, @Nonnull EntityPlayer player) {
		return transferRecipe(container, recipeLayout, player, false, false);
	}

	public static boolean transferRecipe(@Nonnull Container container, @Nonnull RecipeLayout recipeLayout, @Nonnull EntityPlayer player, boolean maxTransfer) {
		IRecipeTransferError error = transferRecipe(container, recipeLayout, player, maxTransfer, true);
		return error == null;
	}

	@Nullable
	private static IRecipeTransferError transferRecipe(@Nonnull Container container, @Nonnull RecipeLayout recipeLayout, @Nonnull EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
		IRecipeTransferHandler transferHandler = Internal.getRuntime().getRecipeRegistry().getRecipeTransferHandler(container, recipeLayout.getRecipeCategory());
		if (transferHandler == null) {
			if (doTransfer) {
				Log.error("No Recipe Transfer handler for container {}", container.getClass());
			}
			return RecipeTransferErrorInternal.INSTANCE;
		}

		return transferHandler.transferRecipe(container, recipeLayout, player, maxTransfer, doTransfer);
	}
}
