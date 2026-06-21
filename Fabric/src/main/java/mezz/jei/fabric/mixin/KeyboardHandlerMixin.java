package mezz.jei.fabric.mixin;

import mezz.jei.fabric.events.JeiCharTypedEvents;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.CharacterEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {

	@Inject(
		method = "charTyped",
		at = @At("HEAD"),
		cancellable = true
	)
	private void beforeCharTypedEvent(long handle, CharacterEvent event, CallbackInfo ci) {
		beforeCharTypedEventInternal(handle, event, ci);
	}

	@Inject(
		method = "charTyped",
		at = @At("TAIL"),
		cancellable = true
	)
	private void afterCharTypedEvent(long handle, CharacterEvent event, CallbackInfo ci) {
		afterCharTypedEventInternal(handle, event, ci);
	}

	@Unique
	private static void beforeCharTypedEventInternal(long windowHandle, CharacterEvent event, CallbackInfo ci) {
		if (ci.isCancelled()) {
			return;
		}
		if (JeiCharTypedEvents.BEFORE_CHAR_TYPED.invoker().beforeCharTyped(windowHandle, event)) {
			ci.cancel(); // Exit the lambda
		}
	}

	@Unique
	private static void afterCharTypedEventInternal(long windowHandle, CharacterEvent event, CallbackInfo ci) {
		if (ci.isCancelled()) {
			return;
		}
		if (JeiCharTypedEvents.AFTER_CHAR_TYPED.invoker().afterCharTyped(windowHandle, event)) {
			ci.cancel(); // Exit the lambda
		}
	}

}
