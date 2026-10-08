package mezz.jei.fabric.mixin;

import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.gui.InventoryEffectRenderer;
import mezz.jei.fabric.plugins.fabric.FabricGuiPlugin;
import mezz.jei.gui.overlay.IngredientListOverlay;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EffectsInInventory.class)
public abstract class EffectsInInventoryMixin {
	@Shadow
	@Final
	private AbstractContainerScreen<?> screen;

	@ModifyVariable(
		method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;II)V",
		name = "maxWidth",
		at = @At("STORE")
	)
	public int modifyEffectWidth(int maxWidth) {
		boolean compact = FabricGuiPlugin.getRuntime()
			.map(IJeiRuntime::getIngredientListOverlay)
			.filter(IngredientListOverlay.class::isInstance)
			.map(IngredientListOverlay.class::cast)
			.map(overlay -> overlay.shouldRenderCompactInventoryEffects(this.screen))
			.orElse(false);

		if (compact) {
			return InventoryEffectRenderer.COMPACT_WIDTH;
		}
		return maxWidth;
	}
}
