package mezz.jei.input;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.input.mouse.InputType;
import net.minecraft.SharedConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.ScreenEvent;

import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.Optional;

public class UserInput {
	@FunctionalInterface
	public interface KeyPressable {
		boolean keyPressed(int keyCode, int scanCode, int modifiers);
	}

	@FunctionalInterface
	public interface MouseClickable {
		boolean mouseClicked(double mouseX, double mouseY, int mouseButton);
	}

	@FunctionalInterface
	public interface MouseOverable {
		boolean isMouseOver(double mouseX, double mouseY);
	}

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

	public static UserInput fromVanilla(int keyCode, int scanCode, int modifiers) {
		InputConstants.Key input = InputConstants.getKey(keyCode, scanCode);
		return new UserInput(input, MouseUtil.getX(), MouseUtil.getY(), modifiers, InputType.IMMEDIATE);
	}

	@Nullable
	public static UserInput fromVanilla(double mouseX, double mouseY, int mouseButton) {
		if (mouseButton < 0) {
			return null;
		}
		InputConstants.Key input = InputConstants.Type.MOUSE.getOrCreate(mouseButton);
		return new UserInput(input, mouseX, mouseY, 0, InputType.IMMEDIATE);
	}

	private final InputConstants.Key key;
	private final double mouseX;
	private final double mouseY;
	private final int modifiers;
	private final InputType clickState;

	public UserInput(InputConstants.Key key, double mouseX, double mouseY, int modifiers, InputType clickState) {
		this.key = key;
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.modifiers = modifiers;
		this.clickState = clickState;
	}

	public InputConstants.Key getKey() {
		return key;
	}

	public double getMouseX() {
		return mouseX;
	}

	public double getMouseY() {
		return mouseY;
	}

	public InputType getClickState() {
		return clickState;
	}

	public boolean isSimulate() {
		return clickState == InputType.SIMULATE;
	}

	public boolean isMouse() {
		return this.key.getType() == InputConstants.Type.MOUSE;
	}

	public boolean isKeyboard() {
		return this.key.getType() == InputConstants.Type.KEYSYM;
	}

	public boolean isAllowedChatCharacter() {
		return isKeyboard() && SharedConstants.isAllowedChatCharacter((char) this.key.getValue());
	}

	public boolean is(KeyMapping keyMapping) {
		return keyMapping.isActiveAndMatches(this.key);
	}

	public boolean is(Collection<KeyMapping> keyMappings) {
		return keyMappings.stream().anyMatch(this::is);
	}

	public boolean callVanilla(MouseOverable mouseOverable, MouseClickable mouseClickable) {
		if (this.key.getType() == InputConstants.Type.MOUSE) {
			if (mouseOverable.isMouseOver(mouseX, mouseY)) {
				if (this.isSimulate()) {
					// we can't easily simulate the click, just say we could handle it
					return true;
				}
				return mouseClickable.mouseClicked(mouseX, mouseY, this.key.getValue());
			}
		}
		return false;
	}

	public boolean callVanilla(KeyPressable keyPressable) {
		if (this.key.getType() == InputConstants.Type.KEYSYM) {
			if (this.isSimulate()) {
				// we can't easily simulate the key press, just say we could handle it
				return true;
			}
			return keyPressable.keyPressed(this.key.getValue(), 0, this.modifiers);
		}
		return false;
	}

	public boolean callVanilla(MouseOverable mouseOverable, MouseClickable mouseClickable, KeyPressable keyPressable) {
		return switch (this.key.getType()) {
			case KEYSYM -> callVanilla(keyPressable);
			case MOUSE -> callVanilla(mouseOverable, mouseClickable);
			default -> false;
		};
	}

}
