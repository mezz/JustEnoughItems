package mezz.jei.gui.input.focus;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;

public class GuiEventListenerFocusHandler implements IFocusHandler {
	private final GuiEventListener guiEventListener;

	public static IFocusHandler create(GuiEventListener guiEventListener) {
		if (guiEventListener instanceof EditBox editBox) {
			return new EditBoxFocusHandler(editBox);
		}
		return new GuiEventListenerFocusHandler(guiEventListener);
	}

	private GuiEventListenerFocusHandler(GuiEventListener guiEventListener) {
		this.guiEventListener = guiEventListener;
	}

	@Override
	public void unFocus() {
		if (guiEventListener.isFocused()) {
			guiEventListener.setFocused(false);
		}
	}

	@Override
	public void focus() {
		if (!guiEventListener.isFocused()) {
			guiEventListener.setFocused(true);
		}
	}
}
