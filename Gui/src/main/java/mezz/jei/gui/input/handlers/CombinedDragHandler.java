package mezz.jei.gui.input.handlers;

import mezz.jei.gui.input.IDragHandler;
import mezz.jei.gui.input.UserInput;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;
import java.util.Optional;

public class CombinedDragHandler implements IDragHandler {
	private final List<IDragHandler> dragHandlers;

	public CombinedDragHandler(IDragHandler... dragHandlers) {
		this.dragHandlers = List.of(dragHandlers);
	}

	@Override
	public Optional<IDragHandler> handleDragStart(Screen screen, UserInput input) {
		return dragHandlers.stream()
			.flatMap(d -> d.handleDragStart(screen, input).stream())
			.findFirst();
	}

	@Override
	public boolean handleDragComplete(Screen screen, UserInput input) {
		for (IDragHandler handler : dragHandlers) {
			if (handler.handleDragComplete(screen, input)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void handleDragCanceled() {
		for (IDragHandler handler : dragHandlers) {
			handler.handleDragCanceled();
		}
	}
}
