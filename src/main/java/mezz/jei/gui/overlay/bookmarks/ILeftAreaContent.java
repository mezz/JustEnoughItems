package mezz.jei.gui.overlay.bookmarks;

import java.awt.Rectangle;

import javax.annotation.Nonnull;

import mezz.jei.input.IShowsRecipeFocuses;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;

public interface ILeftAreaContent extends IShowsRecipeFocuses {

  void drawScreen(@Nonnull Minecraft minecraft, int mouseX, int mouseY, float partialTicks);

  void drawOnForeground(@Nonnull GuiContainer gui, int mouseX, int mouseY);

  void drawTooltips(@Nonnull Minecraft minecraft, int mouseX, int mouseY);

  void updateBounds(@Nonnull Rectangle area);

  boolean handleMouseScrolled(int mouseX, int mouseY, int dWheel);

  boolean handleMouseClicked(int mouseX, int mouseY, int mouseButton);

}
