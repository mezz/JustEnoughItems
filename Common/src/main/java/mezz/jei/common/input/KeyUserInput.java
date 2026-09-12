package mezz.jei.common.input;

import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.util.StringUtil;

public class KeyUserInput extends UserInput {
	private final InputConstants.Key key;
	private final KeyEvent event;
	private final double mouseX;
	private final double mouseY;
	private final InputType inputType;

	public KeyUserInput(KeyEvent event, InputType inputType) {
		this.key = InputConstants.getKey(event);
		this.event = event;
		this.mouseX = MouseUtil.getX();
		this.mouseY = MouseUtil.getY();
		this.inputType = inputType;
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
	public InputType getInputType() {
		return inputType;
	}

	@Override
	@InputWithModifiers.Modifiers
	public int getModifiers() {
		return event.modifiers();
	}

	@Override
	public InputWithModifiers getInputWithModifiers() {
		return event;
	}

	@Override
	public boolean isSimulate() {
		return inputType == InputType.SIMULATE;
	}

	@Override
	public boolean isAllowedChatCharacter() {
		// SDL scancodes identify physical keys; the keycode contains the layout-resolved character.
		return StringUtil.isAllowedChatCharacter(this.event.keycode());
	}

	@Override
	public Either<MouseButtonEventData, KeyEvent> getEvent() {
		return Either.right(event);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("inputType", inputType)
			.add("key", KeyNameUtil.getKeyDisplayName(key).getString())
			.add("event", event)
			.add("mouse", String.format("%s, %s", mouseX, mouseY))
			.toString();
	}
}
