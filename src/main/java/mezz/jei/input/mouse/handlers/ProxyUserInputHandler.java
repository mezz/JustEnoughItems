package mezz.jei.input.mouse.handlers;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.input.UserInput;
import mezz.jei.input.mouse.IUserInputHandler;
import net.minecraft.client.gui.screens.Screen;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ProxyUserInputHandler implements IUserInputHandler {
	private final Supplier<IUserInputHandler> source;

	public ProxyUserInputHandler(Supplier<IUserInputHandler> source) {
		this.source = source;
	}

	@Override
	public IUserInputHandler handleUserInput(Screen screen, UserInput input) {
		return this.source.get().handleUserInput(screen, input);
	}

	@Override
	public void handleMouseClickedOut(InputConstants.Key input) {
		this.source.get().handleMouseClickedOut(input);
	}

	@Override
	public boolean handleMouseScrolled(double mouseX, double mouseY, double scrollDelta) {
		return this.source.get().handleMouseScrolled(mouseX, mouseY, scrollDelta);
	}

	@Nullable
	@Override
	public IUserInputHandler handleDragStart(Screen screen, UserInput input) {
		return this.source.get().handleDragStart(screen, input);
	}

	@Nullable
	@Override
	public IUserInputHandler handleDragComplete(Screen screen, UserInput input) {
		return this.source.get().handleDragComplete(screen, input);
	}

	@Override
	public void handleDragCanceled() {
		this.source.get().handleDragCanceled();
	}
}
