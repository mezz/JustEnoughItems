package mezz.jei.common.input;

import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.input.MouseButtonEvent;

public class MouseUserInput extends UserInput {
	private final MouseButtonEvent event;
	private final boolean doubleClick;
	private final InputConstants.Key key;
	private final InputPhase phase;

	public MouseUserInput(MouseButtonEvent event, boolean doubleClick, InputPhase phase) {
		this.event = event;
		this.doubleClick = doubleClick;
		this.phase = phase;
		this.key = InputConstants.Type.MOUSE.getOrCreate(event.input());
	}

	@Override
	public InputConstants.Key getKey() {
		return key;
	}

	@Override
	public UserInput withMousePosition(double mouseX, double mouseY) {
		return new MouseUserInput(new MouseButtonEvent(mouseX, mouseY, event.buttonInfo()), doubleClick, phase);
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
	public InputPhase getPhase() {
		return phase;
	}

	@Override
	public int getModifiers() {
		return event.modifiers();
	}

	@Override
	public MouseButtonEvent getInputWithModifiers() {
		return event;
	}

	@Override
	public boolean isAllowedChatCharacter() {
		return false;
	}

	@Override
	public boolean ifMouseEvent(IMouseClickable handler) {
		return handler.mouseClicked(event, doubleClick);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("phase", phase)
			.add("key", KeyNameUtil.getKeyDisplayName(key).getString())
			.add("event", event)
			.add("doubleClick", doubleClick)
			.toString();
	}
}
