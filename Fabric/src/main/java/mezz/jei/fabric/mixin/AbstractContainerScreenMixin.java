package mezz.jei.fabric.mixin;

import net.minecraft.client.gui.GuiGraphics;
import mezz.jei.fabric.events.JeiScreenEvents;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {
	@Inject(
		method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderLabels(Lnet/minecraft/client/gui/GuiGraphics;II)V",
			ordinal = 0,
			shift = At.Shift.AFTER
		)
	)
	private void drawForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
		AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
		JeiScreenEvents.DRAW_FOREGROUND.invoker().drawForeground(screen, guiGraphics, mouseX, mouseY);
	}

	@Inject(
		method = "mouseDragged(DDIDD)Z",
		at = @At("HEAD"),
		cancellable = true
	)
	private void allowMouseDrag(double mouseX, double mouseY, int button, double dragX, double dragY, CallbackInfoReturnable<Boolean> ci) {
		AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
		if (!JeiScreenEvents.ALLOW_MOUSE_DRAG.invoker().allowMouseDrag(screen, mouseX, mouseY, button, dragX, dragY)) {
			ci.setReturnValue(true);
		}
	}
}
