package mezz.jei.fabric.mixin;

import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.fabric.plugins.fabric.FabricGuiPlugin;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EffectsInInventory.class)
public abstract class EffectsInInventoryMixin {
	@ModifyVariable(
		method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;II)V",
		name = "maxWidth",
		at = @At("STORE")
	)
	public int modifyEffectWidth(int m) {
		boolean ingredientListDisplayed = FabricGuiPlugin.getRuntime()
			.map(IJeiRuntime::getIngredientListOverlay)
			.map(IIngredientListOverlay::isListDisplayed)
			.orElse(false);

		if (ingredientListDisplayed) {
			// make the potion effects render in compact mode.
			return 32;
		}
		return m;
	}
}
