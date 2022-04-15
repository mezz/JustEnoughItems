package mezz.jei.fabric.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccess {
    @Accessor
    @Nullable
    Slot getHoveredSlot();

    @Accessor
    int getLeftPos();

    @Accessor
    int getTopPos();

    @Accessor
    int getImageWidth();

    @Accessor
    int getImageHeight();
}
