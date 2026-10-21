package mezz.jei.fabric.test.mixin;

import mezz.jei.fabric.test.TooltipBoundsCapture;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin {
	@Inject(
		method = "renderTooltipInternal",
		at = @At("HEAD"),
		cancellable = true
	)
	private void jei$captureTooltipBounds(Font font, List<ClientTooltipComponent> lines, int x, int y, ClientTooltipPositioner positioner, CallbackInfo callback) {
		if (TooltipBoundsCapture.record(font, lines, x, y, positioner)) {
			callback.cancel();
		}
	}
}
