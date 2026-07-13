package mezz.jei.api.gui.handlers;

import mezz.jei.api.gui.builder.IClickableIngredientFactory;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.runtime.IClickableIngredient;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

/**
 * Creates {@link IGuiProperties} from a {@link Screen} so JEI can draw next to it.
 * By default, JEI already handles this for all {@link AbstractContainerScreen}.
 * Register a {@link IScreenHandler} with JEI by using {@link IGuiHandlerRegistration#addGuiScreenHandler(Class, IScreenHandler)}
 */
@FunctionalInterface
public interface IScreenHandler<T extends Screen> extends Function<T, IGuiProperties> {
	@Override
	@Nullable
	IGuiProperties apply(T guiScreen);

	/**
	 * Return a clickable ingredient under the mouse that JEI could not normally detect, used for JEI recipe lookups.
	 *
	 * This is useful for screens that don't have normal slots (which is how JEI normally detects items under the mouse).
	 *
	 * This can also be used to let JEI look up liquids in tanks directly, by returning a FluidStack.
	 * Works with any ingredient type that has been registered with {@link IModIngredientRegistration}.
	 *
	 * @param mouseX the current X position of the mouse in screen coordinates.
	 * @param mouseY the current Y position of the mouse in screen coordinates.
	 *
	 * @since 19.36.0
	 */
	default Optional<? extends IClickableIngredient<?>> getClickableIngredientUnderMouse(
		IClickableIngredientFactory factory,
		T screen,
		double mouseX,
		double mouseY
	) {
		return Optional.empty();
	}
}
