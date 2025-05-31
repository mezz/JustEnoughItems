package mezz.jei.fabric.mixin;

import mezz.jei.fabric.events.JeiCharTypedEvents;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
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
	private void beforeCharTypedEvent(long windowPointer, int codePoint, int modifiers, CallbackInfo ci) {
		beforeCharTypedEventInternal(windowPointer, (char) codePoint, modifiers, ci);
	}

	@Inject(
		method = "charTyped",
		at = @At("TAIL"),
		cancellable = true
	)
	private void afterCharTypedEvent(long windowPointer, int codePoint, int modifiers, CallbackInfo ci) {
		afterCharTypedEventInternal(windowPointer, (char) codePoint, modifiers, ci);
	}

	private static void beforeCharTypedEventInternal(long windowPointer, char codePoint, int modifiers, CallbackInfo ci) {
		if (ci.isCancelled()) {
			return;
		}
		if (JeiCharTypedEvents.BEFORE_CHAR_TYPED.invoker().beforeCharTyped(windowPointer, codePoint, modifiers)) {
			ci.cancel(); // Exit the lambda
		}
	}

	private static void afterCharTypedEventInternal(long windowPointer, char codePoint, int modifiers, CallbackInfo ci) {
		if (ci.isCancelled()) {
			return;
		}
		if (JeiCharTypedEvents.AFTER_CHAR_TYPED.invoker().afterCharTyped(windowPointer, codePoint, modifiers)) {
			ci.cancel(); // Exit the lambda
		}
	}

}
