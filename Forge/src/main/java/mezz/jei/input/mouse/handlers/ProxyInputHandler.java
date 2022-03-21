package mezz.jei.input.mouse.handlers;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.input.UserInput;
import mezz.jei.input.mouse.IUserInputHandler;
import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;
import java.util.function.Supplier;

public class ProxyInputHandler implements IUserInputHandler {
	private final Supplier<IUserInputHandler> source;

	public ProxyInputHandler(Supplier<IUserInputHandler> source) {
		this.source = source;
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input) {
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

	@Override
	public Optional<IUserInputHandler> handleDragStart(Screen screen, UserInput input) {
		return this.source.get().handleDragStart(screen, input);
	}

	@Override
	public Optional<IUserInputHandler> handleDragComplete(Screen screen, UserInput input) {
		return this.source.get().handleDragComplete(screen, input);
	}

	@Override
	public void handleDragCanceled() {
		this.source.get().handleDragCanceled();
	}
}
