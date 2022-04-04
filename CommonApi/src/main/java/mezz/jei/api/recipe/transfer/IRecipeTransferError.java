package mezz.jei.api.recipe.transfer;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * A reason that a recipe transfer couldn't happen.
 *
 * Recipe transfer errors can be created with {@link IRecipeTransferHandlerHelper} or you can implement your own.
 * These errors are returned from {@link IRecipeTransferHandler#transferRecipe(AbstractContainerMenu, Object, IRecipeSlotsView, Player, boolean, boolean)}.
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
		 * @since 6.0.2
		 */
		COSMETIC

	}

	Type getType();

	/**
	 * Called on {@link Type#USER_FACING} errors.
	 *
	 * @implNote JEI also calls {@link #showError(PoseStack, int, int, IRecipeLayout, int, int)}
	 * for backward compatibility.
	 * If you implement this new method, leave the old one unimplemented.
	 *
	 * @since 9.3.0
	 */
	default void showError(PoseStack poseStack, int mouseX, int mouseY, IRecipeSlotsView recipeSlotsView, int recipeX, int recipeY) {

	}

	/**
	 * Called on {@link Type#USER_FACING} errors.
	 *
	 * @deprecated Use {@link #showError(PoseStack, int, int, IRecipeSlotsView, int, int)} instead.
	 * {@link IRecipeLayout} is being phased-out.
	 *
	 * @implNote JEI still calls this old method for backward compatibility.
	 * If you implement the new method, leave this one unimplemented.
	 */
	@SuppressWarnings("removal")
	@Deprecated(forRemoval = true, since = "9.3.0")
	default void showError(PoseStack poseStack, int mouseX, int mouseY, IRecipeLayout recipeLayout, int recipeX, int recipeY) {

	}
}
