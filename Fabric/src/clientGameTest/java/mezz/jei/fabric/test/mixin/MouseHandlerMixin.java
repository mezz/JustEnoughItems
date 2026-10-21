package mezz.jei.fabric.test.mixin;

import mezz.jei.fabric.test.FabricClientTestInput;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
	@Inject(
		method = "onMove",
		at = @At("HEAD"),
		cancellable = true
	)
	private void jei$keepScriptedCursorPosition(long window, double x, double y, CallbackInfo callback) {
		if (FabricClientTestInput.shouldIgnoreCursorMove()) {
			callback.cancel();
		}
	}
}
