package mezz.jei.api.gui;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;

public abstract class BlankAdvancedGuiHandler<T extends GuiContainer> implements IAdvancedGuiHandler<T> {
	@Nullable
	@Override
	public List<Rectangle> getGuiExtraAreas(T guiContainer) {
		return null;
	}

	@Nullable
	@Override
	public Object getIngredientUnderMouse(T guiContainer, int mouseX, int mouseY) {
		return null;
	}
}
