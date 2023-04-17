package mezz.jei.input;

import net.minecraft.client.gui.screen.Screen;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class CombinedMouseDragHandler implements IMouseDragHandler {
	private final List<IMouseDragHandler> handlers;

	public CombinedMouseDragHandler(IMouseDragHandler... handlers) {
		this.handlers = Arrays.asList(handlers);
	}

	@Nullable
	@Override
	public IMouseDragHandler handleDragStart(Screen screen, double mouseX, double mouseY) {
		for (IMouseDragHandler handler : handlers) {
			IMouseDragHandler handled = handler.handleDragStart(screen, mouseX, mouseY);
			if (handled != null) {
				return handled;
			}
		}
		return null;
	}

	@Nullable
	@Override
	public IMouseDragHandler handleDragComplete(Screen screen, double mouseX, double mouseY) {
		for (IMouseDragHandler handler : handlers) {
			IMouseDragHandler handled = handler.handleDragComplete(screen, mouseX, mouseY);
			if (handled != null) {
				return handled;
			}
		}
		return null;
	}

	@Override
	public void handleDragCanceled() {
		for (IMouseDragHandler handler : handlers) {
			handler.handleDragCanceled();
		}
	}
}
