package mezz.jei.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.fabric.events.JeiScreenEvents;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {
    @Inject(
        method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderLabels(Lcom/mojang/blaze3d/vertex/PoseStack;II)V",
            shift = At.Shift.AFTER
        )
    )
    private void drawForeground(PoseStack poseStack, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        JeiScreenEvents.DRAW_FOREGROUND.invoker().drawForeground(screen, poseStack, mouseX, mouseY);
    }
}
