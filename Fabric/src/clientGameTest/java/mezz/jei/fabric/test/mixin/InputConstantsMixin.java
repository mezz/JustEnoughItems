package mezz.jei.fabric.test.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.fabric.test.FabricClientTestInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InputConstants.class)
abstract class InputConstantsMixin {
	@Inject(method = "isKeyDown", at = @At("HEAD"), cancellable = true)
	private static void jei$isKeyDown(long window, int key, CallbackInfoReturnable<Boolean> cir) {
		Boolean keyState = FabricClientTestInput.getKeyState(key);
		if (keyState != null) {
			cir.setReturnValue(keyState);
		}
	}
}
