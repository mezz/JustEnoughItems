package mezz.jei.api.gui;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.Collections;

import mezz.jei.api.IModRegistry;
import mezz.jei.api.ingredients.IModIngredientRegistration;

/**
 * Allows plugins to change how JEI is displayed next to guis.
 * This is for mods that display next to all GUIs, like JEI does, so they can draw together correctly.
 * For handling modded GUIs, you should use {@link IAdvancedGuiHandler} instead.
 *
 * Register your implementation with {@link IModRegistry#addGlobalGuiHandlers(IGlobalGuiHandler...)}.
 *
 * @see IAdvancedGuiHandler
 * @since JEI 4.14.0
 */
public interface IGlobalGuiHandler {
	/**
	 * Give JEI information about extra space that your mod takes up.
	 * Used for moving JEI out of the way of extra things like gui buttons.
	 *
	 * @return the space that the gui takes up besides the normal rectangle defined by GuiContainer.
	 */
	default Collection<Rectangle> getGuiExtraAreas() {
		return Collections.emptyList();
	}

	/**
	 * Return anything under the mouse that JEI could not normally detect, used for JEI recipe lookups.
	 * <p>
	 * This is useful for guis that don't have normal slots (which is how JEI normally detects items under the mouse).
	 * <p>
	 * This can also be used to let JEI look up liquids in tanks directly, by returning a FluidStack.
	 * Works with any ingredient type that has been registered with {@link IModIngredientRegistration}.
	 *
	 * @param mouseX the current X position of the mouse in screen coordinates.
	 * @param mouseY the current Y position of the mouse in screen coordinates.
	 */
	@Nullable
	default Object getIngredientUnderMouse(int mouseX, int mouseY) {
		return null;
	}
}
