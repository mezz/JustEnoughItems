package mezz.jei.common.gui.overlay;

import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.gui.GuiProperties;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public class ScreenPropertiesCache {
    private final IScreenHelper screenHelper;
    private @Nullable IGuiProperties previousGuiProperties;

    public ScreenPropertiesCache(IScreenHelper screenHelper) {
        this.screenHelper = screenHelper;
    }

    public void updateScreen(@Nullable Screen guiScreen, boolean forceUpdate, Consumer<Optional<IGuiProperties>> callback) {
        IGuiProperties currentGuiProperties = Optional.ofNullable(guiScreen)
            .flatMap(screenHelper::getGuiProperties)
            .orElse(null);

        if (forceUpdate || !GuiProperties.areEqual(previousGuiProperties, currentGuiProperties)) {
            previousGuiProperties = currentGuiProperties;
            callback.accept(Optional.ofNullable(currentGuiProperties));
        }
    }

    public boolean hasValidScreen() {
        return previousGuiProperties != null;
    }
}
