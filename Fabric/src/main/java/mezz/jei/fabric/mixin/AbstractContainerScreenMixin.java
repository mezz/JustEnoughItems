package mezz.jei.fabric.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import mezz.jei.fabric.events.JeiScreenEvents;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {
	@Inject(
		method = "extractContents",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;extractSlotHighlightFront(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V",
			ordinal = 0,
			shift = At.Shift.AFTER
		)
	)
	private void drawForeground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
		@SuppressWarnings("DataFlowIssue")
		AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
		JeiScreenEvents.DRAW_FOREGROUND.invoker().drawForeground(screen, guiGraphics, mouseX, mouseY);
	}
}
