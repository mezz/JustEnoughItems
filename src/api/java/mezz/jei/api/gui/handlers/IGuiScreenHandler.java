package mezz.jei.api.gui.handlers;

import javax.annotation.Nullable;
import java.util.function.Function;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

import mezz.jei.api.registration.IGuiHandlerRegistration;

/**
 * Creates {@link IGuiProperties} from a {@link GuiScreen} so JEI can draw next to it.
 * By default, JEI already handles this for all {@link GuiContainer}.
 * Register a {@link IGuiScreenHandler} with JEI by using {@link IGuiHandlerRegistration#addGuiScreenHandler(Class, IGuiScreenHandler)}
 */
@FunctionalInterface
public interface IGuiScreenHandler<T extends GuiScreen> extends Function<T, IGuiProperties> {
	@Override
	@Nullable
	IGuiProperties apply(T guiScreen);
}
