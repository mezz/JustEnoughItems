package mezz.jei.gui.input;

import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Either;
import mezz.jei.common.input.KeyNameUtil;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

public class MouseUserInput extends UserInput {
	private final MouseButtonEvent event;
	private final boolean doubleClick;
	private final InputConstants.Key key;
	private final InputType inputType;

	public MouseUserInput(MouseButtonEvent event, boolean doubleClick, InputType inputType) {
		this.event = event;
		this.doubleClick = doubleClick;
		this.inputType = inputType;
		this.key = InputConstants.Type.MOUSE.getOrCreate(event.input());
	}

	@Override
	public InputConstants.Key getKey() {
		return key;
	}

	@Override
	public double getMouseX() {
		return event.x();
	}

	@Override
	public double getMouseY() {
		return event.y();
	}

	@Override
	public InputType getInputType() {
		return inputType;
	}

	@Override
	public int getModifiers() {
		return event.modifiers();
	}

	@Override
	public boolean isSimulate() {
		return inputType == InputType.SIMULATE;
	}

	@Override
	public boolean isAllowedChatCharacter() {
		return false;
	}

	@Override
	public Either<MouseButtonEvent, KeyEvent> getEvent() {
		return Either.left(event);
	}

	@Override
	public boolean ifMouseEvent(MouseClickable mouseClickable) {
		return mouseClickable.mouseClicked(event, doubleClick);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("inputType", inputType)
			.add("key", KeyNameUtil.getKeyDisplayName(key).getString())
			.add("event", event)
			.toString();
	}
}
