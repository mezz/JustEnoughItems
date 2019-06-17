package mezz.jei.gui.overlay.bookmarks;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.Rectangle2d;

import mezz.jei.input.IShowsRecipeFocuses;

public interface ILeftAreaContent extends IShowsRecipeFocuses {

	void drawScreen(Minecraft minecraft, int mouseX, int mouseY, float partialTicks);

	void drawOnForeground(ContainerScreen gui, int mouseX, int mouseY);

	void drawTooltips(Minecraft minecraft, int mouseX, int mouseY);

	void updateBounds(Rectangle2d area, Set<Rectangle2d> guiExclusionAreas);

	boolean handleMouseScrolled(double mouseX, double mouseY, double dWheel);

	boolean handleMouseClicked(double mouseX, double mouseY, int mouseButton);

}
