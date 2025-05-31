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
		if (widget.isFocused()) {
			screenHelper.setFocused(widget, false);
		}
	}

	@Override
	public void focus() {
		if (!widget.isFocused()) {
			screenHelper.setFocused(widget, true);
		}
	}
}
