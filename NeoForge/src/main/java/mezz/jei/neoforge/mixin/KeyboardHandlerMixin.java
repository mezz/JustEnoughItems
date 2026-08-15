package mezz.jei.neoforge.mixin;

import mezz.jei.gui.input.GuiTextFieldFilter;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
	@ModifyVariable(
		method = "submitPreeditEvent",
		at = @At("HEAD"),
		argsOnly = true
	)
	private static GuiEventListener redirectPreeditToFocusedSearchField(GuiEventListener element) {
		if (element instanceof Screen screen &&
			screen.getFocused() instanceof GuiTextFieldFilter searchField &&
			searchField.isFocused()
		) {
			return searchField;
		}
		return element;
	}
}
