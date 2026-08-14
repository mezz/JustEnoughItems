package mezz.jei.gui.input;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * A foreground layer that owns its rendering, hit testing, and input handling.
 * While the mouse is over this layer, pointer input is captured so that it cannot reach obscured handlers below it.
 */
public interface IGuiInputLayer extends IUserInputHandler, IMouseOverable {
	void draw(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY);
}
