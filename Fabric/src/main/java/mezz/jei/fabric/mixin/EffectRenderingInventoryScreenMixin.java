package mezz.jei.fabric.mixin;

import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.fabric.plugins.fabric.FabricGuiPlugin;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EffectRenderingInventoryScreen.class)
public abstract class EffectRenderingInventoryScreenMixin extends AbstractContainerScreen<AbstractContainerMenu> {
	public EffectRenderingInventoryScreenMixin(AbstractContainerMenu menu, Inventory inventory, Component component) {
		super(menu, inventory, component);
	}

	@ModifyVariable(
		method = "renderEffects(Lnet/minecraft/client/gui/GuiGraphics;II)V",
		index = 7,
		name = "bl",
		at = @At("STORE")
	)
	public boolean modifyHasRoom(boolean bl) {
		boolean compact = FabricGuiPlugin.getRuntime()
			.map(IJeiRuntime::getIngredientListOverlay)
			.filter(IngredientListOverlay.class::isInstance)
			.map(IngredientListOverlay.class::cast)
			.map(overlay -> overlay.shouldRenderCompactInventoryEffects(this))
			.orElse(false);

		if (compact) {
			// Use the compact icons when the full bars overlap JEI.
			return false;
		}
		return bl;
	}
}
