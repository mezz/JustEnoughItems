package mezz.jei.neoforge.mixin;

import mezz.jei.neoforge.events.JeiScreenRenderForegroundEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {
	@Inject(
		method = "extractRenderStateWithTooltipAndSubtitles(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;extractDeferredElements(IIF)V",
			shift = At.Shift.BEFORE
		)
	)
	private void drawForeground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
		@SuppressWarnings("DataFlowIssue")
		Screen screen = (Screen) (Object) this;
		if (screen instanceof AbstractContainerScreen<?>) {
			return;
		}

		guiGraphics.nextStratum();
		Matrix3x2fStack pose = guiGraphics.pose();
		pose.pushMatrix();
		pose.identity();
		try {
			NeoForge.EVENT_BUS.post(new JeiScreenRenderForegroundEvent(screen, guiGraphics, mouseX, mouseY));
		} finally {
			pose.popMatrix();
		}
	}
}
