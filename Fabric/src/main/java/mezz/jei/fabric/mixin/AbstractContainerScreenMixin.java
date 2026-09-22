package mezz.jei.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.fabric.events.JeiScreenEvents;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {
	@Inject(
		method = "mouseDragged(DDIDD)Z",
		at = @At("HEAD"),
		cancellable = true
	)
	private void allowMouseDrag(double mouseX, double mouseY, int button, double dragX, double dragY, CallbackInfoReturnable<Boolean> ci) {
		@SuppressWarnings("DataFlowIssue")
		AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
		if (!JeiScreenEvents.ALLOW_MOUSE_DRAG.invoker().allowMouseDrag(screen, mouseX, mouseY, button, dragX, dragY)) {
			ci.setReturnValue(true);
		}
	}

	@Inject(
		method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderLabels(Lnet/minecraft/client/gui/GuiGraphics;II)V",
			shift = At.Shift.AFTER
		)
	)
	private void drawForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
		@SuppressWarnings("DataFlowIssue")
		AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
		PoseStack pose = graphics.pose();
		float z = pose.last().pose().m32();
		pose.pushPose();
		pose.setIdentity();
		pose.translate(0, 0, z);
		try {
			JeiScreenEvents.DRAW_FOREGROUND.invoker().drawForeground(screen, graphics, mouseX, mouseY);
		} finally {
			pose.popPose();
		}
	}
}
