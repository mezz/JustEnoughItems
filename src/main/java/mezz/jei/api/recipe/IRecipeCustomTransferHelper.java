package mezz.jei.api.recipe;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;

/**
 * Allows custom transfer logic for unique containers
 */
public interface IRecipeCustomTransferHelper extends IRecipeTransferHelper {

	/**
	 * Handle custom transfer logic when the transfer button is pressed
	 */
	public void handleTransfer(@Nonnull EntityPlayer player, IRecipeWrapper recipeWrapper);
}
