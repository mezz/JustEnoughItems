package mezz.jei.gui.input;

import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.UserInput;
import net.minecraft.client.Minecraft;

import java.util.stream.Stream;

/** Finds ingredients under the mouse pointer in the selected input area for recipe lookup and other shortcuts. */
public class IngredientFocusSource {
	private final GuiInputScene inputScene;

	public IngredientFocusSource(GuiInputScene inputScene) {
		this.inputScene = inputScene;
	}

	public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(UserInput input, IInternalKeyMappings keyBindings) {
		double mouseX = input.getMouseX();
		double mouseY = input.getMouseY();

		Stream<IClickableIngredientInternal<?>> stream = getIngredientUnderMouse(mouseX, mouseY);

		if (isConflictingVanillaMouseButton(input, keyBindings)) {
			stream = stream.filter(IClickableIngredientInternal::canClickToFocus);
		}

		return stream;
	}

	Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		return inputScene.getFocusSources(mouseX, mouseY)
			.flatMap(source -> source.getIngredientUnderMouse(mouseX, mouseY));
	}

	/**
	 * Checks whether the input uses a binding Minecraft normally uses to interact with items.
	 * For example, left-clicking an inventory slot should pick up its stack, so JEI only
	 * uses that click for recipe lookup when the ingredient allows it.
	 *
	 * @see IClickableIngredientInternal#canClickToFocus()
	 */
	private static boolean isConflictingVanillaMouseButton(UserInput input, IInternalKeyMappings keyBindings) {
		Minecraft minecraft = Minecraft.getInstance();
		return input.is(keyBindings.getLeftClick()) ||
			input.is(minecraft.options.keyPickItem) ||
			input.is(keyBindings.getRightClick());
	}
}
