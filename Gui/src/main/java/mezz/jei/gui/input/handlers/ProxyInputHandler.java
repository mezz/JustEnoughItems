package mezz.jei.gui.input.handlers;

import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
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
	public Optional<IUserInputHandler> handleUserInput(Screen screen, IGuiProperties guiProperties, UserInput input, IInternalKeyMappings keyBindings) {
		return this.source.get().handleUserInput(screen, guiProperties, input, keyBindings);
	}

	@Override
	public void unfocus() {
		this.source.get().unfocus();
	}

	@Override
	public Optional<IUserInputHandler> handleMouseScrolled(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
		return this.source.get().handleMouseScrolled(mouseX, mouseY, scrollDeltaX, scrollDeltaY);
	}

	@Override
	public Optional<IUserInputHandler> handleMouseDragged(double mouseX, double mouseY, InputConstants.Key mouseKey, double dragX, double dragY) {
		return this.source.get().handleMouseDragged(mouseX, mouseY, mouseKey, dragX, dragY);
	}
}
