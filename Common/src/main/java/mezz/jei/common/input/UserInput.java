package mezz.jei.common.input;

import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.runtime.IJeiKeyMapping;
import mezz.jei.common.platform.Services;
import net.minecraft.client.KeyMapping;
import net.minecraft.util.StringUtil;

import java.util.Optional;

/** A keyboard press or mouse button event with its phase and pointer position. */
public class UserInput implements IJeiUserInput {
	@FunctionalInterface
	public interface KeyPressable {
		boolean keyPressed(int keyCode, int scanCode, int modifiers);
	}

	@FunctionalInterface
	public interface MouseClickable {
		boolean mouseClicked(double mouseX, double mouseY, int mouseButton);
	}

	public static UserInput fromVanilla(int keyCode, int scanCode, int modifiers) {
		return new UserInput(InputConstants.getKey(keyCode, scanCode), MouseUtil.getX(), MouseUtil.getY(), modifiers, InputPhase.PRESS, scanCode);
	}

	public static Optional<UserInput> fromVanilla(double mouseX, double mouseY, int mouseButton, InputPhase phase) {
		if (mouseButton < 0) {
			return Optional.empty();
		}
		return Optional.of(new UserInput(InputConstants.Type.MOUSE.getOrCreate(mouseButton), mouseX, mouseY, 0, phase));
	}

	private final InputConstants.Key key;
	private final double mouseX;
	private final double mouseY;
	private final int modifiers;
	private final InputPhase phase;
	private final int scanCode;

	public UserInput(InputConstants.Key key, double mouseX, double mouseY, int modifiers, InputPhase phase) {
		this(key, mouseX, mouseY, modifiers, phase, 0);
	}

	private UserInput(InputConstants.Key key, double mouseX, double mouseY, int modifiers, InputPhase phase, int scanCode) {
		this.scanCode = scanCode;
		this.key = key;
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.modifiers = modifiers;
		this.phase = phase;
	}

	@Override
	public InputConstants.Key getKey() {
		return key;
	}

	public double getMouseX() {
		return mouseX;
	}

	public double getMouseY() {
		return mouseY;
	}

	/** Copies this input with coordinates relative to a parent control. */
	public UserInput withMousePosition(double mouseX, double mouseY) {
		return new UserInput(key, mouseX, mouseY, modifiers, phase, scanCode);
	}

	public InputPhase getPhase() {
		return phase;
	}

	@Override
	public int getModifiers() {
		return modifiers;
	}

	@Override
	public boolean isSimulate() {
		return isMouseInput() && phase == InputPhase.PRESS;
	}

	public boolean isKeyboardInput() {
		return key.getType() != InputConstants.Type.MOUSE;
	}

	public boolean isMouseInput() {
		return key.getType() == InputConstants.Type.MOUSE;
	}

	public boolean isAllowedChatCharacter() {
		return isKeyboardInput() && StringUtil.isAllowedChatCharacter((char) key.getValue());
	}

	@Override
	public boolean is(IJeiKeyMapping keyMapping) {
		return keyMapping.isActiveAndMatches(key);
	}

	@Override
	public boolean is(KeyMapping keyMapping) {
		return Services.PLATFORM.getInputHelper().isActiveAndMatches(keyMapping, this);
	}

	public boolean ifKeyboardEvent(KeyPressable handler) {
		if (key.getType() == InputConstants.Type.KEYSYM) {
			return handler.keyPressed(key.getValue(), scanCode, modifiers);
		}
		if (key.getType() == InputConstants.Type.SCANCODE) {
			return handler.keyPressed(InputConstants.UNKNOWN.getValue(), key.getValue(), modifiers);
		}
		return false;
	}

	public boolean ifMouseEvent(MouseClickable handler) {
		return isMouseInput() && handler.mouseClicked(mouseX, mouseY, key.getValue());
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("phase", phase)
			.add("key", KeyNameUtil.getKeyDisplayName(key).getString())
			.add("modifiers", modifiers)
			.add("mouse", String.format("%s, %s", mouseX, mouseY))
			.toString();
	}
}
