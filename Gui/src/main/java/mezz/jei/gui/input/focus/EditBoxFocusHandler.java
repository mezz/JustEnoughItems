package mezz.jei.gui.input.focus;

import mezz.jei.common.platform.IPlatformScreenHelper;
import mezz.jei.common.platform.Services;
import net.minecraft.client.gui.components.EditBox;

public class EditBoxFocusHandler implements IFocusHandler {
	private final EditBox editBox;
	private final boolean canLoseFocus;

	public EditBoxFocusHandler(EditBox editBox) {
		this.editBox = editBox;
		IPlatformScreenHelper screenHelper = Services.PLATFORM.getScreenHelper();
		this.canLoseFocus = screenHelper.canLoseFocus(this.editBox);
	}

	@Override
	public void unFocus() {
		if (editBox.isFocused()) {
			if (!canLoseFocus) {
				this.editBox.setCanLoseFocus(true);
			}
			this.editBox.setFocused(false);
		}
	}

	@Override
	public void focus() {
		if (!editBox.isFocused()) {
			this.editBox.setFocused(true);
			if (!canLoseFocus) {
				this.editBox.setCanLoseFocus(false);
			}
		}
	}
}
