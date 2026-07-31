package mezz.jei.gui.input.focus;

import mezz.jei.common.platform.IPlatformScreenHelper;
import mezz.jei.common.platform.Services;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.jetbrains.annotations.Nullable;

public class GuiEventListenerFocusHandler implements IFocusHandler {
	private final AbstractWidget widget;
	private final IPlatformScreenHelper screenHelper;
	private boolean unfocused;

	public static @Nullable IFocusHandler create(GuiEventListener guiEventListener) {
		if (guiEventListener instanceof EditBox editBox) {
			return new EditBoxFocusHandler(editBox);
		}
		if (guiEventListener instanceof AbstractWidget widget) {
			return new GuiEventListenerFocusHandler(widget);
		}
		return null;
	}

	private GuiEventListenerFocusHandler(AbstractWidget widget) {
		this.widget = widget;
		this.screenHelper = Services.PLATFORM.getScreenHelper();
	}

	@Override
	public void unFocus() {
		this.unfocused = widget.isFocused();
		if (this.unfocused) {
			screenHelper.setFocused(widget, false);
		}
	}

	@Override
	public void focus() {
		if (this.unfocused && !widget.isFocused()) {
			screenHelper.setFocused(widget, true);
		}
		this.unfocused = false;
	}
}
