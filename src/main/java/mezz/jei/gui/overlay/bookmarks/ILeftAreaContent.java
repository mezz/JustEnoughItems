package mezz.jei.gui.overlay.bookmarks;

import mezz.jei.input.IShowsRecipeFocuses;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;

import java.awt.Rectangle;
import java.util.Set;

public interface ILeftAreaContent extends IShowsRecipeFocuses {

	void drawScreen(Minecraft minecraft, int mouseX, int mouseY, float partialTicks);

	void drawOnForeground(GuiContainer gui, int mouseX, int mouseY);

	void drawTooltips(Minecraft minecraft, int mouseX, int mouseY);

	void updateBounds(Rectangle area, Set<Rectangle> guiExclusionAreas);

	boolean handleMouseScrolled(int mouseX, int mouseY, int dWheel);

	boolean handleMouseClicked(int mouseX, int mouseY, int mouseButton);

}
