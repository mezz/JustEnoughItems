package mezz.jei.api.gui;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;

import mezz.jei.api.IModRegistry;
import mezz.jei.api.ingredients.IModIngredientRegistration;

/**
 * Allows plugins to change how JEI is displayed next to their mod's guis.
 * Register your implementation with {@link IModRegistry#addAdvancedGuiHandlers(IAdvancedGuiHandler[])}.
 */
public interface IAdvancedGuiHandler<T extends GuiContainer> {
	/**
	 * @return the class that this IAdvancedGuiHandler handles.
	 */
	Class<T> getGuiContainerClass();

	/**
	 * Give JEI information about extra space that the GuiContainer takes up.
	 * Used for moving JEI out of the way of extra things like gui tabs.
	 *
	 * @return the space that the gui takes up besides the normal rectangle defined by GuiContainer.
	 */
	@Nullable
	default List<Rectangle> getGuiExtraAreas(T guiContainer) {
		return null;
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
	 * @since JEI 3.13.2
	 */
	@Nullable
	default Object getIngredientUnderMouse(T guiContainer, int mouseX, int mouseY) {
		return null;
	}
}
