package mezz.jei.gui.overlay.bookmarks;

import java.awt.Rectangle;

import javax.annotation.Nonnull;

import mezz.jei.input.IShowsRecipeFocuses;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;

public interface ILeftAreaContent extends IShowsRecipeFocuses {

  void drawScreen(Minecraft minecraft, int mouseX, int mouseY, float partialTicks);

  void drawOnForeground(GuiContainer gui, int mouseX, int mouseY);

  void drawTooltips(Minecraft minecraft, int mouseX, int mouseY);

  void updateBounds(@Nonnull Rectangle area);

}
