package mezz.jei.api.runtime;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.runtime.util.IImmutableRect2i;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @since 11.5.0
 */
public interface IScreenHelper {
    boolean updateGuiExclusionAreas(Screen screen);

    Stream<IClickedIngredient<?>> getIngredientUnderMouse(Screen screen, double mouseX, double mouseY);

    <T extends Screen> Optional<IGhostIngredientHandler<T>> getGhostIngredientHandler(T guiScreen);

    boolean isInGuiExclusionArea(double mouseX, double mouseY);

    <T extends Screen> Optional<IGuiProperties> getGuiProperties(T screen);

    @Unmodifiable
    Set<? extends IImmutableRect2i> getGuiExclusionAreas();

    Optional<IGuiClickableArea> getGuiClickableArea(AbstractContainerScreen<?> guiContainer, double guiMouseX, double guiMouseY);
}
