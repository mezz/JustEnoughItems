package mezz.jei.fabric.mixin;

import mezz.jei.fabric.events.JeiScreenEvents;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ContainerEventHandler.class)
public interface ContainerEventHandlerMixin {
	@Inject(
		method = "mouseDragged(DDIDD)Z",
		at = @At("HEAD"),
		cancellable = true
	)
	private void allowMouseDrag(double mouseX, double mouseY, int button, double dragX, double dragY, CallbackInfoReturnable<Boolean> ci) {
		if ((Object) this instanceof Screen screen &&
			!JeiScreenEvents.ALLOW_MOUSE_DRAG.invoker().allowMouseDrag(screen, mouseX, mouseY, button, dragX, dragY)
		) {
			ci.setReturnValue(true);
		}
	}
}
