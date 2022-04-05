package mezz.jei.forge.platform;

import mezz.jei.common.platform.IPlatformScreenHelper;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;

public class ForgeScreenHelper implements IPlatformScreenHelper {
    @Override
    public @Nullable Slot getSlotUnderMouse(AbstractContainerScreen<?> containerScreen) {
        return containerScreen.getSlotUnderMouse();
    }

    @Override
    public int getGuiLeft(AbstractContainerScreen<?> containerScreen) {
        return containerScreen.getGuiLeft();
    }

    @Override
    public int getGuiTop(AbstractContainerScreen<?> containerScreen) {
        return containerScreen.getGuiTop();
    }
}
