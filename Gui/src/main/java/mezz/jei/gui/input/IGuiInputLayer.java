package mezz.jei.gui.input;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * A foreground layer that owns its rendering, hit testing, and input handling.
 */
public interface IGuiInputLayer extends IUserInputHandler, IMouseOverable {
	void draw(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY);
}
