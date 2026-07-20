package mezz.jei.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.fabric.events.JeiScreenEvents;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
	private void drawBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
		@SuppressWarnings("DataFlowIssue")
		Screen screen = (Screen) (Object) this;
		runWithIdentityPose(
			graphics,
			() -> JeiScreenEvents.DRAW_BACKGROUND.invoker().drawBackground(screen, graphics, mouseX, mouseY, partialTicks)
		);
	}

	@Inject(
		method = "renderWithTooltip(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screens/Screen;render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
			ordinal = 0,
			shift = At.Shift.AFTER
		)
	)
	private void drawForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
		@SuppressWarnings("DataFlowIssue")
		Screen screen = (Screen) (Object) this;
		if (screen instanceof AbstractContainerScreen<?> containerScreen) {
			runWithIdentityPose(
				graphics,
				() -> JeiScreenEvents.DRAW_FOREGROUND.invoker().drawForeground(containerScreen, graphics, mouseX, mouseY)
			);
		}
	}

	@Inject(
		method = "mouseDragged(DDIDD)Z",
		at = @At("HEAD"),
		cancellable = true
	)
	private void allowMouseDrag(double mouseX, double mouseY, int button, double dragX, double dragY, CallbackInfoReturnable<Boolean> ci) {
		@SuppressWarnings("DataFlowIssue")
		Screen screen = (Screen) (Object) this;
		if (!JeiScreenEvents.ALLOW_MOUSE_DRAG.invoker().allowMouseDrag(screen, mouseX, mouseY, button, dragX, dragY)) {
			ci.setReturnValue(true);
		}
	}

	private static void runWithIdentityPose(GuiGraphics graphics, Runnable runnable) {
		PoseStack pose = graphics.pose();
		pose.pushPose();
		pose.setIdentity();
		try {
			runnable.run();
		} finally {
			pose.popPose();
		}
	}
}
