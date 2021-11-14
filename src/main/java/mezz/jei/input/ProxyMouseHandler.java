package mezz.jei.input;

import mezz.jei.input.click.MouseClickState;
import net.minecraft.client.gui.screens.Screen;

import java.util.function.Supplier;

public class ProxyMouseHandler implements IMouseHandler {
	private final Supplier<IMouseHandler> mouseHandlerSource;

	public ProxyMouseHandler(Supplier<IMouseHandler> mouseHandlerSource) {
		this.mouseHandlerSource = mouseHandlerSource;
	}

	@Override
	public IMouseHandler handleClick(Screen screen, double mouseX, double mouseY, int mouseButton, MouseClickState clickState) {
		return this.mouseHandlerSource.get().handleClick(screen, mouseX, mouseY, mouseButton, clickState);
	}

	@Override
	public void handleMouseClickedOut(int mouseButton) {
		this.mouseHandlerSource.get().handleMouseClickedOut(mouseButton);
	}

	@Override
	public boolean handleMouseScrolled(double mouseX, double mouseY, double scrollDelta) {
		return this.mouseHandlerSource.get().handleMouseScrolled(mouseX, mouseY, scrollDelta);
	}
}
