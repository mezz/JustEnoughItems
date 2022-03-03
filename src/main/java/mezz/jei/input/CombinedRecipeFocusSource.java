package mezz.jei.input;

import mezz.jei.config.KeyBindings;
import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.stream.Stream;

public class CombinedRecipeFocusSource {
	private final List<IRecipeFocusSource> handlers;

	public CombinedRecipeFocusSource(IRecipeFocusSource... handlers) {
		this.handlers = List.of(handlers);
	}

	public Stream<IClickedIngredient<?>> getIngredientUnderMouse(UserInput input) {
		double mouseX = input.getMouseX();
		double mouseY = input.getMouseY();

		Stream<IClickedIngredient<?>> stream = handlers.stream()
			.flatMap(handler -> handler.getIngredientUnderMouse(mouseX, mouseY));

		if (isConflictingVanillaMouseButton(input)) {
			stream = stream.filter(IClickedIngredient::canOverrideVanillaClickHandler);
		}

		return stream;
	}

	/**
	 * Some GUIs (like vanilla) shouldn't allow JEI to click to set the focus,
	 * it would conflict with their normal behavior.
	 * @see IClickedIngredient#canOverrideVanillaClickHandler()
	 */
	private static boolean isConflictingVanillaMouseButton(UserInput input) {
		if (input.isMouse()) {
			Minecraft minecraft = Minecraft.getInstance();
			return input.is(KeyBindings.leftClick) ||
				input.is(minecraft.options.keyPickItem) ||
				input.is(KeyBindings.rightClick);
		}
		return false;
	}
}
