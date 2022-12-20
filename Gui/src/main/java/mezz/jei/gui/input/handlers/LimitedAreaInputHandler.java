package mezz.jei.gui.input.handlers;

import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;

public class LimitedAreaInputHandler implements IUserInputHandler {
	private final IUserInputHandler handler;
	private final ImmutableRect2i area;

	public static IUserInputHandler create(IUserInputHandler handler, ImmutableRect2i area) {
		if (area.isEmpty()) {
			return handler;
		}
		return new LimitedAreaInputHandler(handler, area);
	}

	private LimitedAreaInputHandler(IUserInputHandler handler, ImmutableRect2i area) {
		this.handler = handler;
		this.area = area;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("area", this.area)
			.add("handler", this.handler)
			.toString();
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
		if (this.area.contains(input.getMouseX(), input.getMouseY())) {
			return this.handler.handleUserInput(screen, input, keyBindings)
				.map(handled -> this);
		}
		return Optional.empty();
	}

	@Override
	public void handleMouseClickedOut(InputConstants.Key input) {
		this.handler.handleMouseClickedOut(input);
	}

	@Override
	public Optional<IUserInputHandler> handleMouseScrolled(double mouseX, double mouseY, double scrollDelta) {
		if (this.area.contains(mouseX, mouseY)) {
			return this.handler.handleMouseScrolled(mouseX, mouseY, scrollDelta);
		}
		return Optional.empty();
	}
}
