package mezz.jei.api.recipe.transfer;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;

import mezz.jei.api.gui.IRecipeLayout;

/**
 * A reason that a recipe transfer couldn't happen.
 *
 * Recipe transfer errors can be created with {@link IRecipeTransferHandlerHelper} or you can implement your own.
 * These errors are returned from {@link IRecipeTransferHandler#transferRecipe(Container, IRecipeLayout, PlayerEntity, boolean, boolean)}.
 */
public interface IRecipeTransferError {
	enum Type {
		/**
		 * Errors where the Transfer handler is broken or does not work.
		 * These errors will hide the recipe transfer button, and do not display anything to the user.
		 */
		INTERNAL,

		/**
		 * Errors that the player can fix. Missing items, inventory full, etc.
		 * Something informative will be shown to the player.
		 */
		USER_FACING,

		/**
		 * Errors that still allow the usage of the recipe transfer button.
		 * Hovering over the button will display the error, however the button is active and can be used.
		 * @since JEI version 6.0.2
		 */
		COSMETIC

	}

	Type getType();

	/**
	 * Called on {@link Type#USER_FACING} errors.
	 */
	void showError(MatrixStack matrixStack, int mouseX, int mouseY, IRecipeLayout recipeLayout, int recipeX, int recipeY);
}
