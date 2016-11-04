package mezz.jei.api.gui;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;

public abstract class BlankAdvancedGuiHandler implements IAdvancedGuiHandler {
	@Nullable
	@Override
	public List<Rectangle> getGuiExtraAreas(GuiContainer guiContainer) {
		return null;
	}

	@Nullable
	@Override
	public Object getIngredientUnderMouse(int mouseX, int mouseY) {
		return null;
	}
}
