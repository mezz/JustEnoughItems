package mezz.jei.neoforge.mixin;

import mezz.jei.gui.input.GuiTextFieldFilter;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.input.PreeditEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin {
	@Inject(
		method = "preeditUpdated",
		at = @At("HEAD"),
		cancellable = true
	)
	private void jei$preeditUpdated(PreeditEvent event, CallbackInfoReturnable<Boolean> cir) {
		CreativeModeInventoryScreen screen = (CreativeModeInventoryScreen) (Object) this;
		GuiEventListener focused = screen.getFocused();
		if (focused instanceof GuiTextFieldFilter searchField && searchField.isFocused()) {
			cir.setReturnValue(searchField.preeditUpdated(event));
		}
	}
}
