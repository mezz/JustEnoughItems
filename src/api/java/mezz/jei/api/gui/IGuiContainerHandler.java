package mezz.jei.api.gui;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;

import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.registration.IGuiHandlerRegistration;

/**
 * Allows plugins to change how JEI is displayed next to their mod's guis.
 * Register your implementation with {@link IGuiHandlerRegistration#addGuiContainerHandler(Class, IGuiContainerHandler)}.
 */
public interface IGuiContainerHandler<T extends GuiContainer> {
	/**
	 * Give JEI information about extra space that the GuiContainer takes up.
	 * Used for moving JEI out of the way of extra things like gui tabs.
	 *
	 * @return the space that the gui takes up besides the normal rectangle defined by GuiContainer.
	 */
	default List<Rectangle> getGuiExtraAreas(T guiContainer) {
		return Collections.emptyList();
	}

	/**
	 * Return anything under the mouse that JEI could not normally detect, used for JEI recipe lookups.
	 *
	 * This is useful for guis that don't have normal slots (which is how JEI normally detects items under the mouse).
	 *
	 * This can also be used to let JEI look up liquids in tanks directly, by returning a FluidStack.
	 * Works with any ingredient type that has been registered with {@link IModIngredientRegistration}.
	 *
	 * @param mouseX the current X position of the mouse in screen coordinates.
	 * @param mouseY the current Y position of the mouse in screen coordinates.
	 */
	@Nullable
	default Object getIngredientUnderMouse(T guiContainer, double mouseX, double mouseY) {
		return null;
	}

	/**
	 * Return the JEI-controlled clickable areas for this GUI.
	 * This is useful when you want to add a spot on your GUI that opens JEI and shows recipes.
	 */
	default Collection<IGuiClickableArea> getGuiClickableAreas(T guiContainer) {
		return Collections.emptyList();
	}
}
