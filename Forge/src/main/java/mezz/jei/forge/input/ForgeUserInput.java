package mezz.jei.forge.input;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.InputType;
import mezz.jei.common.input.MouseUtil;
import mezz.jei.common.input.UserInput;
import net.minecraftforge.client.event.ScreenEvent;

import java.util.Optional;

public final class ForgeUserInput {
	private ForgeUserInput() {}

	public static UserInput fromEvent(ScreenEvent.KeyboardKeyEvent keyEvent) {
		InputConstants.Key input = InputConstants.getKey(keyEvent.getKeyCode(), keyEvent.getScanCode());
		double mouseX = MouseUtil.getX();
		double mouseY = MouseUtil.getY();
		int modifiers = keyEvent.getModifiers();
		return new UserInput(input, mouseX, mouseY, modifiers, InputType.IMMEDIATE);
	}

	public static Optional<UserInput> fromEvent(ScreenEvent.MouseClickedEvent event) {
		int button = event.getButton();
		if (button < 0) {
			return Optional.empty();
		}
		InputConstants.Key input = InputConstants.Type.MOUSE.getOrCreate(button);
		UserInput userInput = new UserInput(input, event.getMouseX(), event.getMouseY(), 0, InputType.SIMULATE);
		return Optional.of(userInput);
	}

	public static Optional<UserInput> fromEvent(ScreenEvent.MouseReleasedEvent event) {
		int button = event.getButton();
		if (button < 0) {
			return Optional.empty();
		}
		InputConstants.Key input = InputConstants.Type.MOUSE.getOrCreate(button);
		UserInput userInput = new UserInput(input, event.getMouseX(), event.getMouseY(), 0, InputType.EXECUTE);
		return Optional.of(userInput);
	}
}
