package mezz.jei.fabric.mixin;

import mezz.jei.fabric.input.FabricKeyMapping;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
public class OptionsMixin {
	@Shadow
	public KeyMapping[] keyMappings;
	@Unique
	private boolean jei$loadingOptions;

	@Inject(
		method = "load",
		at = @At("HEAD")
	)
	private void beforeLoad(CallbackInfo ci) {
		jei$loadingOptions = true;
	}

	@Inject(
		method = "load",
		at = @At("RETURN")
	)
	private void afterLoad(CallbackInfo ci) {
		jei$loadingOptions = false;
	}

	@Inject(
		method = "processOptions",
		at = @At("TAIL")
	)
	private void processJeiModifiers(Options.FieldAccess access, CallbackInfo ci) {
		for (KeyMapping mapping : keyMappings) {
			if (mapping instanceof FabricKeyMapping jeiMapping) {
				int fallbackModifiers = jeiMapping.getModifiers();
				if (jei$loadingOptions && !access.process("key_" + mapping.getName(), "").isEmpty()) {
					// Preserve existing unmodified bindings, including ones using the new default key.
					fallbackModifiers = 0;
				}
				int modifiers = access.process("jei_modifiers_" + mapping.getName(), fallbackModifiers);
				jeiMapping.setModifiers(modifiers);
			}
		}
	}
}
