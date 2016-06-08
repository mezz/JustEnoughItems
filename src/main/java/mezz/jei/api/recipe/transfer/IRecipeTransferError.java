package mezz.jei.api.recipe.transfer;

import javax.annotation.Nonnull;

import mezz.jei.api.gui.IRecipeLayout;
import net.minecraft.client.Minecraft;

/**
 * A reason that the recipe transfer couldn't happen.
 * See {@link Type#INTERNAL} and {@link Type#USER_FACING}.
 */
public interface IRecipeTransferError {
	enum Type {
		/**
		 * Errors where the Transfer handler is broken, or does not work, or the server is not present.
		 * These errors will hide the recipe transfer button, but do not display anything to the user.
		 */
		INTERNAL,

		/**
		 * Errors that the player can fix. Missing items, inventory full, etc.
		 * Something informative will be shown to the player.
		 */
		USER_FACING
	}

	@Nonnull
	Type getType();

	/** Called on USER_FACING errors */
	void showError(@Nonnull Minecraft minecraft, int mouseX, int mouseY, @Nonnull IRecipeLayout recipeLayout, int recipeX, int recipeY);
}
