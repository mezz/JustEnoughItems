package mezz.jei.fabric.mixin;

import mezz.jei.fabric.input.AmecsSupport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Registers JEI's Command modifier before AMECS seals its modifier registry. */
@Pseudo
@Mixin(targets = "de.siphalor.amecs.key_modifiers.impl.AmecsKeyModifiersEarlyInit", remap = false)
public class AmecsKeyModifiersEarlyInitMixin {
	@Inject(
		method = "onInitialize",
		at = @At(value = "INVOKE", target = "Lde/siphalor/amecs/key_modifiers/api/AmecsKeyModifiers;seal()V")
	)
	private void beforeSeal(CallbackInfo ci) {
		AmecsSupport.init();
	}
}
