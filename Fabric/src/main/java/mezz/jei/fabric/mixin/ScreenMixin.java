package mezz.jei.fabric.mixin;

import mezz.jei.fabric.events.JeiScreenEvents;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.gui.screens.Screen.class)
public class ScreenMixin {
	@Inject(
		method = "renderBackground(Lnet/minecraft/client/gui/GuiGraphics;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/GuiGraphics;fillGradient(IIIIII)V",
			shift = At.Shift.AFTER,
			ordinal = 0
		)
	)
	private void afterRenderBackground(GuiGraphics guiGraphics, CallbackInfo ci) {
		JeiScreenEvents.AFTER_RENDER_BACKGROUND.invoker().afterRenderBackground(guiGraphics);
	}
}
