package mezz.jei.input.mouse.handlers;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.input.UserInput;
import mezz.jei.input.mouse.IUserInputHandler;
import mezz.jei.util.MathUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;

import javax.annotation.Nullable;

public class LimitedAreaUserInputHandler implements IUserInputHandler {
	private final IUserInputHandler handler;
	private final Rect2i area;

	public static IUserInputHandler create(IUserInputHandler handler, @Nullable Rect2i area) {
		if (area == null) {
			return handler;
		}
		return new LimitedAreaUserInputHandler(handler, area);
	}

	private LimitedAreaUserInputHandler(IUserInputHandler handler, Rect2i area) {
		this.handler = handler;
		this.area = area;
	}

	@Override
	public IUserInputHandler handleUserInput(Screen screen, UserInput input) {
		if (input.in(this.area)) {
			if (this.handler.handleUserInput(screen, input) != null) {
				return this;
			}
		}
		return null;
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

	@Nullable
	@Override
	public IUserInputHandler handleDragStart(Screen screen, UserInput input) {
		if (input.in(this.area)) {
			if (this.handler.handleDragStart(screen, input) != null) {
				return this;
			}
		}
		return null;
	}

	@Nullable
	@Override
	public IUserInputHandler handleDragComplete(Screen screen, UserInput input) {
		if (input.in(this.area)) {
			if (this.handler.handleDragComplete(screen, input) != null) {
				return this;
			}
		}
		return null;
	}

	@Override
	public void handleDragCanceled() {
		this.handler.handleDragCanceled();
	}
}
