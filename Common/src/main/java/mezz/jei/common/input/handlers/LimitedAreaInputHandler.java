package mezz.jei.common.input.handlers;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.IUserInputHandler;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.gui.screens.Screen;

import org.jetbrains.annotations.Nullable;
import java.util.Optional;

public class LimitedAreaInputHandler implements IUserInputHandler {
	private final IUserInputHandler handler;
	private final ImmutableRect2i area;

	public static IUserInputHandler create(IUserInputHandler handler, @Nullable ImmutableRect2i area) {
		if (area == null) {
			return handler;
		}
		return new LimitedAreaInputHandler(handler, area);
	}

	private LimitedAreaInputHandler(IUserInputHandler handler, ImmutableRect2i area) {
		this.handler = handler;
		this.area = area;
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
	public boolean handleMouseScrolled(double mouseX, double mouseY, double scrollDelta) {
		if (this.area.contains(mouseX, mouseY)) {
			return this.handler.handleMouseScrolled(mouseX, mouseY, scrollDelta);
		}
		return false;
	}

	@Override
	public Optional<IUserInputHandler> handleDragStart(Screen screen, UserInput input) {
		if (this.area.contains(input.getMouseX(), input.getMouseY())) {
			return this.handler.handleDragStart(screen, input)
				.map(handled -> this);
		}
		return Optional.empty();
	}

	@Override
	public Optional<IUserInputHandler> handleDragComplete(Screen screen, UserInput input) {
		if (this.area.contains(input.getMouseX(), input.getMouseY())) {
			return this.handler.handleDragComplete(screen, input)
				.map(handled -> this);
		}
		return Optional.empty();
	}

	@Override
	public void handleDragCanceled() {
		this.handler.handleDragCanceled();
	}
}
