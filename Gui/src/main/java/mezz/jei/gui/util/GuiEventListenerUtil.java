package mezz.jei.gui.util;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.ContainerEventHandler;

import java.util.stream.Stream;

public final class GuiEventListenerUtil {
	private GuiEventListenerUtil() {
	}

	public static Stream<EditBox> getEditBoxes(ContainerEventHandler container) {
		return container.children().stream()
			.flatMap(child -> {
				if (child instanceof EditBox editBox) {
					return Stream.of(editBox);
				}
				if (child instanceof ContainerEventHandler childContainer) {
					return getEditBoxes(childContainer);
				}
				return Stream.empty();
			});
	}
}
