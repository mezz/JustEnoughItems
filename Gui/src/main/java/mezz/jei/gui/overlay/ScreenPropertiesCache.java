package mezz.jei.gui.overlay;

import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.GuiProperties;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;

public class ScreenPropertiesCache {
    private final IScreenHelper screenHelper;
    private @Nullable IGuiProperties previousGuiProperties;
    private Set<ImmutableRect2i> previousGuiExclusionAreas = Set.of();

    public ScreenPropertiesCache(IScreenHelper screenHelper) {
        this.screenHelper = screenHelper;
    }

    public void updateScreen(@Nullable Screen guiScreen, @Nullable Set<ImmutableRect2i> updatedGuiExclusionAreas, Runnable callback) {
        IGuiProperties currentGuiProperties = Optional.ofNullable(guiScreen)
            .flatMap(screenHelper::getGuiProperties)
            .orElse(null);

        boolean changed = false;
        if (updatedGuiExclusionAreas != null && !this.previousGuiExclusionAreas.equals(updatedGuiExclusionAreas)) {
            this.previousGuiExclusionAreas = updatedGuiExclusionAreas;
            changed = true;
        }

        if (!GuiProperties.areEqual(previousGuiProperties, currentGuiProperties)) {
            this.previousGuiProperties = currentGuiProperties;
            changed = true;
        }

        if (changed) {
            callback.run();
        }
    }

    public boolean hasValidScreen() {
        return previousGuiProperties != null;
    }

    public Optional<IGuiProperties> getGuiProperties() {
        return Optional.ofNullable(previousGuiProperties);
    }

    public Set<ImmutableRect2i> getGuiExclusionAreas() {
        return previousGuiExclusionAreas;
    }
}
