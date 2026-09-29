package mezz.jei.common.input;

import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.util.StringUtil;

import java.util.function.Predicate;

public class KeyUserInput extends UserInput {
	private final InputConstants.Key key;
	private final KeyEvent event;
	private final double mouseX;
	private final double mouseY;

	public KeyUserInput(KeyEvent event) {
		this(event, MouseUtil.getX(), MouseUtil.getY());
	}

	private KeyUserInput(KeyEvent event, double mouseX, double mouseY) {
		this.key = InputConstants.getKey(event);
		this.event = event;
		this.mouseX = mouseX;
		this.mouseY = mouseY;
	}

	@Override
	public UserInput withMousePosition(double mouseX, double mouseY) {
		return new KeyUserInput(event, mouseX, mouseY);
	}

	@Override
	public InputConstants.Key getKey() {
		return key;
	}

	@Override
	public double getMouseX() {
		return mouseX;
	}

	@Override
	public double getMouseY() {
		return mouseY;
	}

	@Override
	public InputPhase getPhase() {
		return InputPhase.PRESS;
	}

	@Override
	@InputWithModifiers.Modifiers
	public int getModifiers() {
		return event.modifiers();
	}

	@Override
	public KeyEvent getInputWithModifiers() {
		return event;
	}

	@Override
	public boolean isAllowedChatCharacter() {
		// SDL scancodes identify physical keys; the keycode contains the layout-resolved character.
		return StringUtil.isAllowedChatCharacter(this.event.keycode());
	}

	@Override
	public boolean ifKeyboardEvent(Predicate<KeyEvent> handler) {
		return handler.test(event);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("phase", getPhase())
			.add("key", KeyNameUtil.getKeyDisplayName(key).getString())
			.add("event", event)
			.add("mouse", String.format("%s, %s", mouseX, mouseY))
			.toString();
	}
}
