package mezz.jei.gui.input;

import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Either;
import mezz.jei.common.input.KeyNameUtil;
import mezz.jei.common.input.MouseButtonEventData;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

public class MouseUserInput extends UserInput {
	private final MouseButtonEventData eventData;
	private final InputConstants.Key key;
	private final InputType inputType;

	public MouseUserInput(MouseButtonEvent event, boolean doubleClick, InputType inputType) {
		this.eventData = new MouseButtonEventData(event, doubleClick);
		this.inputType = inputType;
		this.key = InputConstants.Type.MOUSE.getOrCreate(event.input());
	}

	@Override
	public InputConstants.Key getKey() {
		return key;
	}

	@Override
	public double getMouseX() {
		return eventData.event().x();
	}

	@Override
	public double getMouseY() {
		return eventData.event().y();
	}

	@Override
	public InputType getInputType() {
		return inputType;
	}

	@Override
	public int getModifiers() {
		return eventData.event().modifiers();
	}

	@Override
	public InputWithModifiers getInputWithModifiers() {
		return eventData.event();
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
	public Either<MouseButtonEventData, KeyEvent> getEvent() {
		return Either.left(eventData);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("inputType", inputType)
			.add("key", KeyNameUtil.getKeyDisplayName(key).getString())
			.add("eventData", eventData)
			.toString();
	}
}
