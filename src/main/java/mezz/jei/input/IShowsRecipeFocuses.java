package mezz.jei.input;

import javax.annotation.Nullable;

import mezz.jei.gui.Focus;

public interface IShowsRecipeFocuses {

	@Nullable
	Focus getFocusUnderMouse(int mouseX, int mouseY);

}
