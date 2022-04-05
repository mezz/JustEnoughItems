package mezz.jei.common.platform;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;

public interface IPlatformScreenHelper {
    @Nullable
    Slot getSlotUnderMouse(AbstractContainerScreen<?> containerScreen);

    int getGuiLeft(AbstractContainerScreen<?> containerScreen);

    int getGuiTop(AbstractContainerScreen<?> containerScreen);
}
