package mezz.jei.gui.input.handlers;

import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.input.IUserInputHandler;
import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;
import java.util.function.Supplier;

public class ProxyInputHandler implements IUserInputHandler {
	private final Supplier<IUserInputHandler> source;

	public ProxyInputHandler(Supplier<IUserInputHandler> source) {
		this.source = source;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("source", this.source.get())
			.toString();
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
		return this.source.get().handleUserInput(screen, input, keyBindings);
	}

	@Override
	public void handleMouseClickedOut(InputConstants.Key input) {
		this.source.get().handleMouseClickedOut(input);
	}

	@Override
	public Optional<IUserInputHandler> handleMouseScrolled(double mouseX, double mouseY, double scrollDelta) {
		return this.source.get().handleMouseScrolled(mouseX, mouseY, scrollDelta);
	}
}
