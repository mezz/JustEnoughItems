package mezz.jei.neoforge.input;

import mezz.jei.common.input.InputPhase;
import mezz.jei.common.input.UserInput;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.util.Optional;

public final class ForgeUserInput {
	private ForgeUserInput() {}

	public static UserInput fromEvent(ScreenEvent.KeyPressed event) {
		return UserInput.fromVanilla(event.getKeyEvent());
	}

	public static Optional<UserInput> fromEvent(ScreenEvent.MouseButtonPressed event) {
		return UserInput.fromVanilla(event.getMouseButtonEvent(), event.isDoubleClick(), InputPhase.PRESS);
	}

	public static Optional<UserInput> fromEvent(ScreenEvent.MouseButtonReleased event) {
		return UserInput.fromVanilla(event.getMouseButtonEvent(), false, InputPhase.RELEASE);
	}
}
