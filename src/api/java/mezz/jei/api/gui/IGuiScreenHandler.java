package mezz.jei.api.gui;

import javax.annotation.Nullable;
import java.util.function.Function;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

import mezz.jei.api.IModRegistry;
import mezz.jei.api.ingredients.IModIngredientRegistration;

/**
 * Creates {@link IGuiProperties} from a {@link GuiScreen} so JEI can draw next to it.
 * By default, JEI already handles this for all {@link GuiContainer}.
 * Register a {@link IGuiScreenHandler} with JEI by using {@link IModRegistry#addGuiScreenHandler(Class, IGuiScreenHandler)}
 *
 * @since JEI 4.8.4
 */
@FunctionalInterface
public interface IGuiScreenHandler<T extends GuiScreen> extends Function<T, IGuiProperties> {
	@Override
	@Nullable
	IGuiProperties apply(T guiScreen);

	/**
	 * Return anything under the mouse that JEI could not normally detect, used for JEI recipe lookups.
	 * <p>
	 * This is useful for screens that don't have normal slots (which is how JEI normally detects items under the mouse).
	 * <p>
	 * This can also be used to let JEI look up liquids in tanks directly, by returning a FluidStack.
	 * Works with any ingredient type that has been registered with {@link IModIngredientRegistration}.
	 *
	 * @param mouseX the current X position of the mouse in screen coordinates.
	 * @param mouseY the current Y position of the mouse in screen coordinates.
	 * @since JEI 4.16.4
	 */
	@Nullable
	default Object getIngredientUnderMouse(T guiScreen, int mouseX, int mouseY) {
		return null;
	}
}
