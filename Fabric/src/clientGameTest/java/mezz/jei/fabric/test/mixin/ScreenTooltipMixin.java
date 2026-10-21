package mezz.jei.fabric.test.mixin;

import mezz.jei.fabric.test.TooltipBoundsCapture;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Screen.class)
public class ScreenTooltipMixin {
	@Inject(
		method = "renderTooltipInternal",
		at = @At("HEAD"),
		cancellable = true
	)
	private void jei$captureTooltipBounds(PoseStack poseStack, List<ClientTooltipComponent> lines, int x, int y, CallbackInfo callback) {
		if (TooltipBoundsCapture.record(Minecraft.getInstance().font, lines, x, y)) {
			callback.cancel();
		}
	}
}
