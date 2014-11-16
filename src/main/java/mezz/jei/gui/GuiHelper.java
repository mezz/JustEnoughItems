package mezz.jei.gui;

import mezz.jei.api.gui.IGuiHelper;
import mezz.jei.api.gui.IGuiItemStack;

public class GuiHelper implements IGuiHelper {

	@Override
	public IGuiItemStack makeGuiItemStack(int xPosition, int yPosition, int padding) {
		return new GuiItemStack(xPosition, yPosition, padding);
	}

}
