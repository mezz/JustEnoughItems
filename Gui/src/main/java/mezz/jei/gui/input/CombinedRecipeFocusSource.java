package mezz.jei.gui.input;

import mezz.jei.common.input.IClickableIngredientInternal;
import mezz.jei.common.input.IInternalKeyMappings;
import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.stream.Stream;

public class CombinedRecipeFocusSource {
	private final List<IRecipeFocusSource> handlers;

	public CombinedRecipeFocusSource(IRecipeFocusSource... handlers) {
		this.handlers = List.of(handlers);
	}

	public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(UserInput input, IInternalKeyMappings keyBindings) {
		double mouseX = input.getMouseX();
		double mouseY = input.getMouseY();

		Stream<IClickableIngredientInternal<?>> stream = handlers.stream()
			.flatMap(handler -> handler.getIngredientUnderMouse(mouseX, mouseY));

		if (isConflictingVanillaMouseButton(input, keyBindings)) {
			stream = stream.filter(IClickableIngredientInternal::canClickToFocus);
		}

		return stream;
	}

	/**
	 * Some GUIs (like vanilla) shouldn't allow JEI to click to set the focus,
	 * it would conflict with their normal behavior.
	 * @see IClickableIngredientInternal#canClickToFocus()
	 */
	private static boolean isConflictingVanillaMouseButton(UserInput input, IInternalKeyMappings keyBindings) {
		if (input.isMouse()) {
			Minecraft minecraft = Minecraft.getInstance();
			return input.is(keyBindings.getLeftClick()) ||
				input.is(minecraft.options.keyPickItem) ||
				input.is(keyBindings.getRightClick());
		}
		return false;
	}
}
