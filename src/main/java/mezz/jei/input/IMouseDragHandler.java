package mezz.jei.input;

import net.minecraft.client.gui.screens.Screen;

import javax.annotation.Nullable;

public interface IMouseDragHandler {
	@Nullable
	default IMouseDragHandler handleDragStart(Screen screen, double mouseX, double mouseY) {
		return null;
	}

	@Nullable
	default IMouseDragHandler handleDragComplete(Screen screen, double mouseX, double mouseY) {
		return null;
	}

	default void handleDragCanceled() {

	}
}
