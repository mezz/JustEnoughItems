package mezz.jei.gui.input;

import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.runtime.IJeiKeyMapping;
import mezz.jei.common.input.KeyNameUtil;
import mezz.jei.common.platform.IPlatformInputHelper;
import mezz.jei.common.platform.Services;
import net.minecraft.SharedConstants;
import net.minecraft.client.KeyMapping;

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
	private final InputType inputType;

	public UserInput(InputConstants.Key key, double mouseX, double mouseY, int modifiers, InputType inputType) {
		this.key = key;
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.modifiers = modifiers;
		this.inputType = inputType;
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

	public InputType getInputType() {
		return inputType;
	}

	public boolean isSimulate() {
		return inputType == InputType.SIMULATE;
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

	public boolean callVanilla(IMouseOverable mouseOverable, MouseClickable mouseClickable) {
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
				// key press simulate happens on key up, which we ignore
				return false;
			}
			return keyPressable.keyPressed(this.key.getValue(), 0, this.modifiers);
		}
		return false;
	}

	public boolean callVanilla(IMouseOverable mouseOverable, MouseClickable mouseClickable, KeyPressable keyPressable) {
		return switch (this.key.getType()) {
			case KEYSYM -> callVanilla(keyPressable);
			case MOUSE -> callVanilla(mouseOverable, mouseClickable);
			default -> false;
		};
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("inputType", inputType)
			.add("key", KeyNameUtil.getKeyDisplayName(key).getString())
			.add("modifiers", modifiers)
			.add("mouse", String.format("%s, %s", mouseX, mouseY))
			.toString();
	}
}
