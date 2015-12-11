package mezz.jei.transfer;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;

import mezz.jei.gui.RecipeLayout;

/** A reason that the recipe transfer couldn't happen. See IRecipeTransferError.Type */
public interface IRecipeTransferError {
	enum Type {
		/**
		 * Errors where the Transfer helper is missing or does not work, or the server is not present.
		 * These errors will hide the recipe transfer button and do not display anything.
		 */
		INTERNAL,

		/**
		 * Errors that the player can fix. Missing items, inventory full, etc.
		 * Something informative will be shown to the player.
		 */
		USER_FACING
	}

	Type getType();

	/** Called on USER_FACING errors */
	void showError(@Nonnull Minecraft minecraft, int mouseX, int mouseY, @Nonnull RecipeLayout recipeLayout);
}
