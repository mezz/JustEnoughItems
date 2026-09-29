package mezz.jei.common.input;

import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.runtime.IJeiKeyMapping;
import mezz.jei.common.platform.IPlatformInputHelper;
import mezz.jei.common.platform.Services;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.Optional;
import java.util.function.Predicate;

/** A key press, mouse button press, or mouse button release, with the mouse pointer's position. */
public abstract class UserInput implements IJeiUserInput {
	@FunctionalInterface
	public interface IMouseClickable {
		boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean doubleClick);
	}

	public static UserInput fromVanilla(KeyEvent keyEvent) {
		return new KeyUserInput(keyEvent);
	}

	public static Optional<UserInput> fromVanilla(MouseButtonEvent mouseButtonEvent, boolean doubleClick, InputPhase phase) {
		int mouseButton = mouseButtonEvent.button();
		if (mouseButton < 0) {
			return Optional.empty();
		}
		UserInput userInput = new MouseUserInput(mouseButtonEvent, doubleClick, phase);
		return Optional.of(userInput);
	}

	public abstract double getMouseX();

	public abstract double getMouseY();

	/** Copies this event with a different mouse position, for controls using coordinates relative to their parent. */
	public abstract UserInput withMousePosition(double mouseX, double mouseY);

	public abstract InputPhase getPhase();

	public abstract boolean isAllowedChatCharacter();

	/**
	 * Whether a public API handler should only check if it can handle this input.
	 * Mouse button presses return true; releases and keyboard presses return false.
	 */
	@Override
	public final boolean isSimulate() {
		return isMouseInput() && getPhase() == InputPhase.PRESS;
	}

	@Override
	public final boolean is(IJeiKeyMapping keyMapping) {
		return keyMapping.isActiveAndMatches(this.getKey());
	}

	@Override
	public final boolean is(KeyMapping keyMapping) {
		IPlatformInputHelper inputHelper = Services.PLATFORM.getInputHelper();
		return inputHelper.isActiveAndMatches(keyMapping, this);
	}

	public final boolean isKeyboardInput() {
		return getInputWithModifiers() instanceof KeyEvent;
	}

	public final boolean isMouseInput() {
		return getInputWithModifiers() instanceof MouseButtonEvent;
	}

	/** Calls the handler for keyboard input and returns whether it handled the event. */
	public boolean ifKeyboardEvent(Predicate<KeyEvent> handler) {
		return false;
	}

	/** Calls the handler for mouse input and returns whether it handled the event. */
	public boolean ifMouseEvent(IMouseClickable handler) {
		return false;
	}

}
