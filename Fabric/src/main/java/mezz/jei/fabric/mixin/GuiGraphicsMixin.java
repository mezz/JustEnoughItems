package mezz.jei.fabric.mixin;

import mezz.jei.fabric.events.JeiScreenEvents;
import mezz.jei.gui.chat.ChatIngredientTooltip;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin {
	@Inject(
		method = "setTooltipForNextFrameInternal",
		at = @At("HEAD"),
		cancellable = true
	)
	private void jei$setTooltipForNextFrameInternal(
		Font font,
		List<ClientTooltipComponent> lines,
		int x,
		int y,
		ClientTooltipPositioner positioner,
		Identifier style,
		boolean replaceExisting,
		CallbackInfo ci
	) {
		@SuppressWarnings("DataFlowIssue")
		GuiGraphics guiGraphics = (GuiGraphics) (Object) this;
		if (!JeiScreenEvents.ALLOW_DEFERRED_TOOLTIP.invoker().allow(guiGraphics)) {
			ci.cancel();
		}
	}

	@Inject(
		method = "renderComponentHoverEffect",
		at = @At("HEAD"),
		cancellable = true
	)
	private void jei$componentHoverEffect(Font font, Style hoveredStyle, int xMouse, int yMouse, CallbackInfo ci) {
		@SuppressWarnings("DataFlowIssue")
		GuiGraphics guiGraphics = (GuiGraphics) (Object) this;
		if (ChatIngredientTooltip.setTooltipForHoveredText(guiGraphics, hoveredStyle, xMouse, yMouse)) {
			ci.cancel();
		}
	}
}
