package mezz.jei.neoforge.input;

import mezz.jei.gui.input.InputType;
import mezz.jei.gui.input.KeyUserInput;
import mezz.jei.gui.input.MouseUserInput;
import mezz.jei.gui.input.UserInput;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.util.Optional;

public final class ForgeUserInput {
	private ForgeUserInput() {}

	public static UserInput fromEvent(ScreenEvent.KeyPressed keyPressedEvent) {
		// execute the input immediately, on key pressed. do not wait for key released
		return new KeyUserInput(keyPressedEvent.getKeyEvent(), InputType.IMMEDIATE);
	}

	public static Optional<UserInput> fromEvent(ScreenEvent.MouseButtonPressed event) {
		int button = event.getButton();
		if (button < 0) {
			return Optional.empty();
		}
		UserInput userInput = new MouseUserInput(event.getMouseButtonEvent(), event.isDoubleClick(), InputType.SIMULATE);
		return Optional.of(userInput);
	}

	public static Optional<UserInput> fromEvent(ScreenEvent.MouseButtonReleased event) {
		int button = event.getButton();
		if (button < 0) {
			return Optional.empty();
		}
		UserInput userInput = new MouseUserInput(event.getMouseButtonEvent(), false, InputType.EXECUTE);
		return Optional.of(userInput);
	}
}
