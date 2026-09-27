package mezz.jei.gui.input;

import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.UserInput;
import net.minecraft.client.Minecraft;

import java.util.stream.Stream;

public class CombinedRecipeFocusSource {
	private final GuiInputSurfaceStack surfaces;

	public CombinedRecipeFocusSource(GuiInputSurfaceStack surfaces) {
		this.surfaces = surfaces;
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
		return surfaces.getFocusSources(mouseX, mouseY)
			.flatMap(source -> source.getIngredientUnderMouse(mouseX, mouseY));
	}

	/**
	 * Some GUIs (like vanilla) shouldn't allow JEI to click to set the focus,
	 * it would conflict with their normal behavior.
	 * @see IClickableIngredientInternal#canClickToFocus()
	 */
	private static boolean isConflictingVanillaMouseButton(UserInput input, IInternalKeyMappings keyBindings) {
		Minecraft minecraft = Minecraft.getInstance();
		return input.is(keyBindings.getLeftClick()) ||
			input.is(minecraft.options.keyPickItem) ||
			input.is(keyBindings.getRightClick());
	}
}
