package mezz.jei.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.fabric.events.JeiScreenEvents;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {
    @Inject(
        method = "renderBackground(Lcom/mojang/blaze3d/vertex/PoseStack;I)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/Screen;fillGradient(Lcom/mojang/blaze3d/vertex/PoseStack;IIIIII)V",
            shift = At.Shift.AFTER
        )
    )
    private void afterRenderBackground(PoseStack poseStack, int i, CallbackInfo ci) {
        Screen screen = (Screen) (Object) this;
        JeiScreenEvents.AFTER_RENDER_BACKGROUND.invoker().afterRenderBackground(screen, poseStack);
    }
}
