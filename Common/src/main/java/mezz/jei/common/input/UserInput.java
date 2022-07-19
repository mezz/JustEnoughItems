package mezz.jei.common.input;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.runtime.IJeiKeyMapping;
import mezz.jei.common.platform.IPlatformInputHelper;
import mezz.jei.common.platform.Services;
import net.minecraft.SharedConstants;
import net.minecraft.client.KeyMapping;

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

	public static UserInput fromVanilla(int keyCode, int scanCode, int modifiers, InputType inputType) {
		InputConstants.Key input = InputConstants.getKey(keyCode, scanCode);
		return new UserInput(input, MouseUtil.getX(), MouseUtil.getY(), modifiers, inputType);
	}

	public static Optional<UserInput> fromVanilla(double mouseX, double mouseY, int mouseButton, InputType inputType) {
		if (mouseButton < 0) {
			return Optional.empty();
		}
		InputConstants.Key input = InputConstants.Type.MOUSE.getOrCreate(mouseButton);
		UserInput userInput = new UserInput(input, mouseX, mouseY, 0, inputType);
		return Optional.of(userInput);
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

	public boolean is(IJeiKeyMapping keyMapping) {
		return keyMapping.isActiveAndMatches(this.key);
	}

	public boolean is(KeyMapping keyMapping) {
		IPlatformInputHelper inputHelper = Services.PLATFORM.getInputHelper();
		return inputHelper.isActiveAndMatches(keyMapping, this.key);
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
