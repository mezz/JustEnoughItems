package mezz.jei.fabric.mixin;

import mezz.jei.fabric.events.JeiScreenEvents;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {
	@Inject(
		method = "renderWithTooltip(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screens/Screen;renderBackground(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
			ordinal = 0,
			shift = At.Shift.AFTER
		)
	)
	private void drawBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
		@SuppressWarnings("DataFlowIssue")
		Screen screen = (Screen) (Object) this;
		JeiScreenEvents.DRAW_BACKGROUND.invoker().drawBackground(screen, guiGraphics, mouseX, mouseY, partialTicks);
	}
}
