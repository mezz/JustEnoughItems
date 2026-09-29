package mezz.jei.common.input.interaction;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.common.input.IInputTarget;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.util.MathUtil;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;
import java.util.function.Supplier;

/** Connects recipe input handlers to JEI interactions and translates coordinates for internal controls. */
public final class ApiInputAdapter {
	private ApiInputAdapter() {
	}

	/**
	 * Starts an interaction for a recipe input handler.
	 * For a public handler, a mouse press checks whether it accepts the click, and release
	 * calls it again to perform the action. Keyboard input executes immediately.
	 */
	public static Optional<IInputInteraction> beginInput(Screen screen, UserInput input, IInternalKeyMappings keys, IJeiInputHandler handler) {
		if (handler instanceof IInputTarget target) {
			return target.beginInput(screen, input, keys);
		}
		if (!handler.handleInput(input.getMouseX(), input.getMouseY(), input)) {
			return Optional.empty();
		}
		if (input.isKeyboardInput()) {
			return Optional.of(ConsumedInput.INSTANCE);
		}
		return Optional.of(new InputAction(release -> handler.handleInput(release.getMouseX(), release.getMouseY(), release)));
	}

	/**
	 * Starts an internal recipe control's interaction using coordinates relative to that control.
	 *
	 * @param area the control's current area in screen coordinates, including the recipe's position
	 */
	public static Optional<IInputInteraction> beginInputRelativeTo(Screen screen, UserInput input, IInternalKeyMappings keys, IInputTarget target, Supplier<ScreenRectangle> area) {
		ScreenRectangle bounds = area.get();
		if (!MathUtil.contains(bounds, input.getMouseX(), input.getMouseY())) {
			return Optional.empty();
		}
		return target.beginInput(screen, relative(input, bounds), keys)
			.map(interaction -> new RelativeInteraction(interaction, area));
	}

	/** Mouse movement handling for public recipe controls. */
	@FunctionalInterface
	public interface IMouseDragHandler {
		/**
		 * Handles movement over the recipe controls under the pointer.
		 *
		 * @return true if a control handled the movement, or false if none did
		 */
		boolean handleMouseDragged(double mouseX, double mouseY, InputConstants.Key button, double dragX, double dragY);
	}

	private static UserInput relative(UserInput input, ScreenRectangle area) {
		return input.withMousePosition(input.getMouseX() - area.left(), input.getMouseY() - area.top());
	}

	/**
	 * Translates screen coordinates for a JEI control inside a recipe.
	 *
	 * @param interaction the selected control's interaction, using coordinates relative to that control
	 * @param area the control's current area in screen coordinates
	 */
	private record RelativeInteraction(IInputInteraction interaction, Supplier<ScreenRectangle> area) implements IInputInteraction {
		@Override
		public void complete(UserInput input) {
			interaction.complete(relative(input, area.get()));
		}

		@Override
		public void drag(double mouseX, double mouseY, InputConstants.Key button, double dragX, double dragY) {
			ScreenRectangle current = area.get();
			interaction.drag(mouseX - current.left(), mouseY - current.top(), button, dragX, dragY);
		}

		@Override
		public boolean capturesMouseMovement() {
			return interaction.capturesMouseMovement();
		}

		@Override
		public void cancel() {
			interaction.cancel();
		}

		@Override
		public boolean keepsCaptureOnRepeatedPress() {
			return interaction.keepsCaptureOnRepeatedPress();
		}
	}
}
