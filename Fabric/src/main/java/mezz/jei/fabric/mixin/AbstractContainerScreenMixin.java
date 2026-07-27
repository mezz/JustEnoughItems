package mezz.jei.fabric.mixin;

import mezz.jei.fabric.events.JeiScreenEvents;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {
	@Inject(
		method = "mouseDragged(DDIDD)Z",
		at = @At("HEAD"),
		cancellable = true
	)
	private void allowMouseDrag(double mouseX, double mouseY, int button, double dragX, double dragY, CallbackInfoReturnable<Boolean> ci) {
		@SuppressWarnings("DataFlowIssue")
		AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
		if (!JeiScreenEvents.ALLOW_MOUSE_DRAG.invoker().allowMouseDrag(screen, mouseX, mouseY, button, dragX, dragY)) {
			ci.setReturnValue(true);
		}
	}
}
