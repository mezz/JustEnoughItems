package mezz.jei.fabric.mixin;

import mezz.jei.fabric.events.JeiScreenEvents;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {
	@Inject(
		method = "renderCarriedItem(Lnet/minecraft/client/gui/GuiGraphics;II)V",
		at = @At("HEAD")
	)
	private void drawForeground(GuiGraphics graphics, int mouseX, int mouseY, CallbackInfo ci) {
		@SuppressWarnings("DataFlowIssue")
		AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
		graphics.nextStratum();
		runWithIdentityPose(
			graphics,
			() -> JeiScreenEvents.DRAW_FOREGROUND.invoker().drawForeground(screen, graphics, mouseX, mouseY)
		);
	}

	private static void runWithIdentityPose(GuiGraphics graphics, Runnable runnable) {
		Matrix3x2fStack pose = graphics.pose();
		pose.pushMatrix();
		pose.identity();
		try {
			runnable.run();
		} finally {
			pose.popMatrix();
		}
	}
}
