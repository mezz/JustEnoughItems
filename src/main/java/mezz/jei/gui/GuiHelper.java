package mezz.jei.gui;

import mezz.jei.api.gui.IGuiHelper;
import mezz.jei.api.gui.IGuiItemStacks;

import javax.annotation.Nonnull;

public class GuiHelper implements IGuiHelper {

	@Nonnull
	@Override
	public IGuiItemStacks makeGuiItemStacks() {
		return new GuiItemStacks();
	}

}
