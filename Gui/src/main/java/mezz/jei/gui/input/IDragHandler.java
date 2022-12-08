package mezz.jei.gui.input;

import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;

public interface IDragHandler {
	Optional<IDragHandler> handleDragStart(Screen screen, UserInput input);

	boolean handleDragComplete(Screen screen, UserInput input);

	default void handleDragCanceled() {

	}
}
