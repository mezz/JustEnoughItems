package mezz.jei.api.gui.handlers;

import org.jetbrains.annotations.Nullable;
import java.util.function.Function;

import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

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
}
