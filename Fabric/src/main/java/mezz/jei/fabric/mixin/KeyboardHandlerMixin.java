package mezz.jei.fabric.mixin;

import mezz.jei.fabric.events.JeiCharTypedEvents;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    @Inject(
        method = "method_1458(Lnet/minecraft/client/gui/components/events/GuiEventListener;II)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/events/GuiEventListener;charTyped(CI)Z"),
        cancellable = true
    )
    private static void beforeCharTypedEvent(GuiEventListener guiEventListener, int i, int modifiers, CallbackInfo ci) {
        beforeCharTypedEventInternal(guiEventListener, (char) i, modifiers, ci);
    }

    @Inject(
        method = "method_1473(Lnet/minecraft/client/gui/components/events/GuiEventListener;CI)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/events/GuiEventListener;charTyped(CI)Z"),
        cancellable = true
    )
    private static void beforeCharTypedEvent2(GuiEventListener guiEventListener, char codepoint, int modifiers, CallbackInfo ci) {
        beforeCharTypedEventInternal(guiEventListener, codepoint, modifiers, ci);
    }

    @Inject(
        method = "method_1458(Lnet/minecraft/client/gui/components/events/GuiEventListener;II)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/events/GuiEventListener;charTyped(CI)Z",
            shift = At.Shift.AFTER
        ),
        cancellable = true
    )
    private static void afterCharTypedEvent(GuiEventListener guiEventListener, int i, int modifiers, CallbackInfo ci) {
        afterCharTypedEventInternal(guiEventListener, (char) i, modifiers, ci);
    }

    @Inject(
        method = "method_1473(Lnet/minecraft/client/gui/components/events/GuiEventListener;CI)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/events/GuiEventListener;charTyped(CI)Z",
            shift = At.Shift.AFTER
        ),
        cancellable = true
    )
    private static void afterCharTypedEvent2(GuiEventListener guiEventListener, char codepoint, int modifiers, CallbackInfo ci) {
        afterCharTypedEventInternal(guiEventListener, codepoint, modifiers, ci);
    }

    private static void beforeCharTypedEventInternal(GuiEventListener guiEventListener, char codepoint, int modifiers, CallbackInfo ci) {
        if (ci.isCancelled()) {
            return;
        }
        if (JeiCharTypedEvents.BEFORE_CHAR_TYPED.invoker().beforeCharTyped(guiEventListener, codepoint, modifiers)) {
            ci.cancel(); // Exit the lambda
        }
    }

    private static void afterCharTypedEventInternal(GuiEventListener guiEventListener, char codepoint, int modifiers, CallbackInfo ci) {
        if (ci.isCancelled()) {
            return;
        }
        if (JeiCharTypedEvents.AFTER_CHAR_TYPED.invoker().afterCharTyped(guiEventListener, codepoint, modifiers)) {
            ci.cancel(); // Exit the lambda
        }
    }

}
