package mezz.jei.fabric.mixin;

import mezz.jei.gui.chat.ChatIngredientTooltip;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin {
	@Inject(method = "renderComponentHoverEffect", at = @At("HEAD"), cancellable = true)
	private void jei$componentHoverEffect(Font font, @Nullable Style style, int mouseX, int mouseY, CallbackInfo ci) {
		@SuppressWarnings("DataFlowIssue")
		GuiGraphics guiGraphics = (GuiGraphics) (Object) this;
		if (ChatIngredientTooltip.setTooltipForHoveredText(guiGraphics, style, mouseX, mouseY)) {
			ci.cancel();
		}
	}
}
