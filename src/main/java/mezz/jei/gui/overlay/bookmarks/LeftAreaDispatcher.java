package mezz.jei.gui.overlay.bookmarks;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mezz.jei.Internal;
import mezz.jei.api.gui.IGuiProperties;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IShowsRecipeFocuses;
import mezz.jei.runtime.JeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

public class LeftAreaDispatcher implements IShowsRecipeFocuses {

  private static final int BORDER_PADDING = 2;

  private final @Nonnull List<ILeftAreaContent> content = new ArrayList<>();
  private int current = 0;
  private IGuiProperties guiProperties;
  private @Nonnull Rectangle displayArea = new Rectangle();
  private boolean canShow = false;

  public LeftAreaDispatcher() {
    content.add(new BookmarkOverlay(new Rectangle()));
  }

  private boolean hasContent() {
    return current >= 0 && current < content.size();
  }

  public void drawScreen(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
    if (canShow && hasContent()) {
      content.get(current).drawScreen(minecraft, mouseX, mouseY, partialTicks);
    }
  }

  public void drawOnForeground(GuiContainer gui, int mouseX, int mouseY) {
    if (canShow && hasContent()) {
      content.get(current).drawOnForeground(gui, mouseX, mouseY);
    }
  }

  public void drawTooltips(Minecraft minecraft, int mouseX, int mouseY) {
    if (canShow && hasContent()) {
      content.get(current).drawTooltips(minecraft, mouseX, mouseY);
    }
  }

  public void updateScreen(@Nullable GuiScreen guiScreen) {
    canShow = false;
    if (hasContent()) {
      JeiRuntime runtime = Internal.getRuntime();
      if (runtime == null) {
        return;
      }
      IGuiProperties currentGuiProperties = runtime.getGuiProperties(guiScreen);
      if (currentGuiProperties == null) {
        guiProperties = null;
      } else if (!areGuiPropertiesEqual(guiProperties, currentGuiProperties)) {
        guiProperties = currentGuiProperties;
        displayArea = getDisplayArea(currentGuiProperties);
        content.get(current).updateBounds(displayArea);
        canShow = true;
      }
    }
  }

  private static boolean areGuiPropertiesEqual(IGuiProperties guiProperties1, @Nonnull IGuiProperties guiProperties2) {
    return guiProperties1 != null && guiProperties1.getGuiClass().equals(guiProperties2.getGuiClass())
        && guiProperties1.getGuiLeft() == guiProperties2.getGuiLeft() && guiProperties1.getGuiXSize() == guiProperties2.getGuiXSize()
        && guiProperties1.getScreenWidth() == guiProperties2.getScreenWidth() && guiProperties1.getScreenHeight() == guiProperties2.getScreenHeight();
  }

  private static @Nonnull Rectangle getDisplayArea(@Nonnull IGuiProperties guiProperties) {
    final int x = BORDER_PADDING;
    final int y = BORDER_PADDING;
    final int width = guiProperties.getGuiLeft() - x - BORDER_PADDING;
    final int height = guiProperties.getScreenHeight() - y - BORDER_PADDING;
    return new Rectangle(x, y, width, height);
  }

  @Override
  @Nullable
  public IClickedIngredient<?> getIngredientUnderMouse(int mouseX, int mouseY) {
    if (canShow && hasContent()) {
      return content.get(current).getIngredientUnderMouse(mouseX, mouseY);
    }
    return null;
  }

  @Override
  public boolean canSetFocusWithMouse() {
    if (canShow && hasContent()) {
      return content.get(current).canSetFocusWithMouse();
    }
    return false;
  }

}
