package mezz.jei.input.mouse.handlers;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.input.UserInput;
import mezz.jei.input.mouse.IUserInputHandler;
import mezz.jei.util.MathUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;

import javax.annotation.Nullable;
import java.util.Optional;

public class LimitedAreaInputHandler implements IUserInputHandler {
	private final IUserInputHandler handler;
	private final Rect2i area;

	public static IUserInputHandler create(IUserInputHandler handler, @Nullable Rect2i area) {
		if (area == null) {
			return handler;
		}
		return new LimitedAreaInputHandler(handler, area);
	}

	private LimitedAreaInputHandler(IUserInputHandler handler, Rect2i area) {
		this.handler = handler;
		this.area = area;
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input) {
		if (MathUtil.contains(this.area, input.getMouseX(), input.getMouseY())) {
			return this.handler.handleUserInput(screen, input)
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
		if (MathUtil.contains(this.area, mouseX, mouseY)) {
			return this.handler.handleMouseScrolled(mouseX, mouseY, scrollDelta);
		}
		return false;
	}

	@Override
	public Optional<IUserInputHandler> handleDragStart(Screen screen, UserInput input) {
		if (MathUtil.contains(this.area, input.getMouseX(), input.getMouseY())) {
			return this.handler.handleDragStart(screen, input)
				.map(handled -> this);
		}
		return Optional.empty();
	}

	@Override
	public Optional<IUserInputHandler> handleDragComplete(Screen screen, UserInput input) {
		if (MathUtil.contains(this.area, input.getMouseX(), input.getMouseY())) {
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
