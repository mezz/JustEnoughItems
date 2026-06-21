package mezz.jei.fabric.mixin;

import de.siphalor.amecs.key_modifiers.impl.AmecsKeyModifiersEarlyInit;
import mezz.jei.fabric.input.AmecsHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This mixin into AmecsKeyModifiers Early initializer because that
 * where AmecsKeyModifiers are usually sealed.
 */
@Mixin(AmecsKeyModifiersEarlyInit.class)
public class AmecsKeyModifiersEarlyInitMixin {

	@Inject(
		method = "onInitialize()V",
		at = @At(
			value = "INVOKE",
			target = "Lde/siphalor/amecs/key_modifiers/api/AmecsKeyModifiers;seal()V"
		),
		require = 0
	)
	private static void onInit(CallbackInfo ci) {
		AmecsHelper.init();
	}
}
