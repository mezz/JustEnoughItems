package mezz.jei.api.gui.handlers;

import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;

import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IModIngredientRegistration;

/**
 * Allows plugins to change how JEI is displayed next to their mod's guis.
 * Register your implementation with {@link IGuiHandlerRegistration#addGuiContainerHandler(Class, IGuiContainerHandler)}.
 */
public interface IGuiContainerHandler<T extends AbstractContainerScreen<?>> {
	/**
	 * Give JEI information about extra space that the {@link AbstractContainerScreen} takes up.
	 * Used for moving JEI out of the way of extra things like gui tabs.
	 *
	 * @return the space that the gui takes up besides the normal rectangle defined by {@link AbstractContainerScreen}.
	 */
	default List<Rect2i> getGuiExtraAreas(T containerScreen) {
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
	default Object getIngredientUnderMouse(T containerScreen, double mouseX, double mouseY) {
		return null;
	}

	/**
	 * Return the JEI-controlled clickable areas for this GUI.
	 * This is useful when you want to add a spot on your GUI that opens JEI and shows recipes.
	 *
	 * Optionally, you can restrict what you return here based on the current mouse position.
	 * @param guiMouseX the current X position of the mouse in gui-relative coordinates.
	 * @param guiMouseY the current Y position of the mouse in gui-relative coordinates.
	 * @since 6.0.1
	 */
	default Collection<IGuiClickableArea> getGuiClickableAreas(T containerScreen, double guiMouseX, double guiMouseY) {
		return Collections.emptyList();
	}
}
