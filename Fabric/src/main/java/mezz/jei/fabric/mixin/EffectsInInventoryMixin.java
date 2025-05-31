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
//	public EffectsInInventoryMixin(AbstractContainerScreen<?> screen) {
//		super(screen);
//	}

	@ModifyVariable(
		method = "renderEffects(Lnet/minecraft/client/gui/GuiGraphics;II)V",
		index = 7,
		name = "bl",
		at = @At("STORE")
	)
	public boolean modifyHasRoom(boolean bl) {
		boolean ingredientListDisplayed = FabricGuiPlugin.getRuntime()
			.map(IJeiRuntime::getIngredientListOverlay)
			.map(IIngredientListOverlay::isListDisplayed)
			.orElse(false);

		if (ingredientListDisplayed) {
			// make the potion effects think that there is not enough room,
			// so they render in compact mode.
			return false;
		}
		return bl;
	}
}
