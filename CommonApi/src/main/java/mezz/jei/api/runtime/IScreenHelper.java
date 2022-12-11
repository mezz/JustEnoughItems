package mezz.jei.api.runtime;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.gui.handlers.IScreenHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Helper for all runtime Screen functions.
 *
 * @since 11.5.0
 */
public interface IScreenHelper {
    /**
     * Get the ingredient under the mouse for the given screen.
     *
     * This uses information from plugins via {@link IGuiContainerHandler#getClickableIngredientUnderMouse}
     * and from {@link IGlobalGuiHandler#getClickableIngredientUnderMouse}
     *
     * @since 11.5.0
     */
    Stream<IClickableIngredient<?>> getClickableIngredientUnderMouse(Screen screen, double mouseX, double mouseY);

    /**
     * Get gui properties for the given screen, if they are known.
     *
     * This uses information from plugins that have registered an {@link IScreenHandler}.
     *
     * @since 11.5.0
     */
    <T extends Screen> Optional<IGuiProperties> getGuiProperties(T screen);

    /**
     * Get gui clickable areas for the given screen under the mouse, if there are any.
     *
     * This uses information from plugins that have registered an {@link IGuiContainerHandler}
     * and implemented {@link IGuiContainerHandler#getGuiClickableAreas}.
     *
     * @since 11.5.0
     */
    Stream<IGuiClickableArea> getGuiClickableArea(AbstractContainerScreen<?> guiContainer, double guiMouseX, double guiMouseY);

    /**
     * Get gui exclusion areas for the given screen, if there are any.
     * These are areas that are drawn to by the screen, in addition to
     * the regular rectangle area defined in {@link IGuiProperties}
     *
     * This uses information from plugins that have registered an {@link IGuiContainerHandler}
     * and implemented {@link IGuiContainerHandler#getGuiExtraAreas}.
     *
     * @since 11.5.0
     */
    Stream<Rect2i> getGuiExclusionAreas(Screen screen);

    /**
     * Get the ghost ingredient handler for the given screen, if there is one.
     *
     * This uses information from plugins that have registered
     * ghost ingredient handlers via {@link IGuiHandlerRegistration#addGhostIngredientHandler}
     *
     * @since 11.5.0
     */
    <T extends Screen> Optional<IGhostIngredientHandler<T>> getGhostIngredientHandler(T guiScreen);
}
