package mezz.jei.gui;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mezz.jei.Internal;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.config.Config;
import mezz.jei.gui.ingredients.GuiIngredientFast;
import mezz.jei.gui.ingredients.GuiIngredientFastList;
import mezz.jei.gui.ingredients.GuiItemStackGroup;
import mezz.jei.input.ClickedIngredient;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IKeyable;
import mezz.jei.input.IMouseHandler;
import mezz.jei.input.IShowsRecipeFocuses;
import mezz.jei.util.Java6Helper;
import mezz.jei.util.StackHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

public class BookmarksOverlayInternal implements IShowsRecipeFocuses, IMouseHandler, IKeyable {

  private static final int borderPadding = 2;
  private static final int searchHeight = 16;
  private static final int buttonSize = 20;
  private static final String nextLabel = ">";
  private static final String backLabel = "<";

  private static final int itemStackPadding = 1;
  private static final int itemStackWidth = GuiItemStackGroup.getWidth(itemStackPadding);
  private static final int itemStackHeight = GuiItemStackGroup.getHeight(itemStackPadding);
  private static int firstItemIndex = 0;

  private static final String clearLabel = "Clr";

  private final GuiIngredientFastList guiIngredientList;
  private final GuiProperties guiProperties;
  private final List<Rectangle> guiAreas;
  private final BookmarksOverlay parent;

  private GuiIngredientFast hovered = null;

  private final GuiButton clearButton;

  private List<IAdvancedGuiHandler<?>> activeAdvancedGuiHandlers = Collections.emptyList();

  public BookmarksOverlayInternal(BookmarksOverlay parent, IIngredientRegistry ingredientRegistry, GuiScreen guiScreen,
      GuiProperties guiProperties) {

    this.parent = parent;
    this.guiProperties = guiProperties;
    this.guiIngredientList = new GuiIngredientFastList(ingredientRegistry);

    this.activeAdvancedGuiHandlers = getActiveAdvancedGuiHandlers(guiScreen);
    if (!activeAdvancedGuiHandlers.isEmpty() && guiScreen instanceof GuiContainer) {
      GuiContainer guiContainer = (GuiContainer) guiScreen;
      guiAreas = getGuiAreas(guiContainer);
    } else {
      guiAreas = Collections.emptyList();
    }

    final int columns = getColumns(guiProperties);
    final int rows = getRows(guiProperties);
    final int xSize = columns * itemStackWidth;
    final int xEmptySpace = guiProperties.getScreenWidth() - guiProperties.getGuiLeft() - guiProperties.getGuiXSize()
        - xSize;

    final int leftEdge = guiProperties.getGuiLeft() + guiProperties.getGuiXSize() + (xEmptySpace / 2);
    final int rightEdge = leftEdge + xSize;

    final int yItemButtonSpace = getItemButtonYSpace(guiProperties);
    final int itemButtonsHeight = rows * itemStackHeight;

    final int buttonStartY = buttonSize + (2 * borderPadding) + (yItemButtonSpace - itemButtonsHeight) / 2;
    createItemButtons(guiIngredientList, guiAreas, leftEdge, buttonStartY, columns, rows);

    clearButton = new GuiButton(0, rightEdge - buttonSize, borderPadding, buttonSize, buttonSize, nextLabel);
  }

  public void drawScreen(Minecraft minecraft, int mouseX, int mouseY) {
    GlStateManager.disableLighting();

    clearButton.drawButton(minecraft, mouseX, mouseY);

    GlStateManager.disableBlend();

    if (shouldShowDeleteItemTooltip(minecraft)) {
      hovered = guiIngredientList.render(minecraft, false, mouseX, mouseY);
    } else {
      boolean mouseOver = isMouseOver(mouseX, mouseY);
      hovered = guiIngredientList.render(minecraft, mouseOver, mouseX, mouseY);
    }

    if (hovered != null) {
      hovered.drawHovered(minecraft);
    }

    GlStateManager.enableAlpha();
  }

  private List<Rectangle> getGuiAreas(GuiContainer guiContainer) {
    List<Rectangle> guiAreas = new ArrayList<Rectangle>();
    for (IAdvancedGuiHandler<?> advancedGuiHandler : activeAdvancedGuiHandlers) {
      List<Rectangle> guiExtraAreas = getGuiAreas(guiContainer, advancedGuiHandler);
      if (guiExtraAreas != null) {
        guiAreas.addAll(guiExtraAreas);
      }
    }
    return guiAreas;
  }

  private <T extends GuiContainer> List<Rectangle> getGuiAreas(GuiContainer gui,
      IAdvancedGuiHandler<T> advancedGuiHandler) {
    Class<T> guiClass = advancedGuiHandler.getGuiContainerClass();
    if (guiClass.isInstance(gui)) {
      T guiT = guiClass.cast(gui);
      return advancedGuiHandler.getGuiExtraAreas(guiT);
    }
    return null;
  }

  private boolean shouldShowDeleteItemTooltip(Minecraft minecraft) {
    if (Config.isDeleteItemsInCheatModeActive()) {
      EntityPlayer player = minecraft.thePlayer;
      if (player.inventory.getItemStack() != null) {
        return true;
      }
    }
    return false;
  }

  private List<IAdvancedGuiHandler<?>> getActiveAdvancedGuiHandlers(GuiScreen guiScreen) {
    List<IAdvancedGuiHandler<?>> activeAdvancedGuiHandler = new ArrayList<IAdvancedGuiHandler<?>>();
    if (guiScreen instanceof GuiContainer) {
      for (IAdvancedGuiHandler<?> advancedGuiHandler : parent.getAdvancedGuiHandlers()) {
        Class<?> guiContainerClass = advancedGuiHandler.getGuiContainerClass();
        if (guiContainerClass.isInstance(guiScreen)) {
          activeAdvancedGuiHandler.add(advancedGuiHandler);
        }
      }
    }
    return activeAdvancedGuiHandler;
  }

  @Override
  public IClickedIngredient<?> getIngredientUnderMouse(int mouseX, int mouseY) {
    if (!isMouseOver(mouseX, mouseY)) {
      return null;
    }

    ClickedIngredient<?> clicked = guiIngredientList.getIngredientUnderMouse(mouseX, mouseY);
    if (clicked != null) {
      setKeyboardFocus(false);
      clicked.setAllowsCheating();
    }
    return clicked;
  }

  @Override
  public boolean canSetFocusWithMouse() {
    return true;
  }

  @Override
  public boolean handleMouseScrolled(int mouseX, int mouseY, int scrollDelta) {
    return false;
  }

  @Override
  public boolean isMouseOver(int mouseX, int mouseY) {

    for (Rectangle guiArea : guiAreas) {
      if (guiArea.contains(mouseX, mouseY)) {
        return false;
      }
    }

    return true;
  }

  @Override
  public boolean hasKeyboardFocus() {
    return false;
  }

  @Override
  public void setKeyboardFocus(boolean focus) {
  }

  @Override
  public boolean onKeyPressed(char typedChar, int keyCode) {
    return false;
  }

  @Override
  public boolean handleMouseClicked(int mouseX, int mouseY, int mouseButton) {
    return false;
  }

  public ItemStack getStackUnderMouse() {
    if (hovered != null) {
      Object ingredient = hovered.getIngredient();
      if (ingredient instanceof ItemStack) {
        return (ItemStack) ingredient;
      }
    }
    return null;
  }

  public static int getColumns(GuiProperties guiProperties) {
    return getItemButtonXSpace(guiProperties) / itemStackWidth;
  }

  private static int getItemButtonXSpace(GuiProperties guiProperties) {
    return guiProperties.getScreenWidth()
        - (guiProperties.getGuiLeft() + guiProperties.getGuiXSize() + (2 * borderPadding));
  }

  public static int getRows(GuiProperties guiProperties) {
    return getItemButtonYSpace(guiProperties) / itemStackHeight;
  }

  private static int getItemButtonYSpace(GuiProperties guiProperties) {
    return guiProperties.getScreenHeight() - (buttonSize + searchHeight + 2 + (4 * borderPadding));
  }

  private static void createItemButtons(GuiIngredientFastList guiItemStacks, List<Rectangle> guiAreas,
      final int xStart, final int yStart, final int columnCount, final int rowCount) {
    guiItemStacks.clear();

    for (int row = 0; row < rowCount; row++) {
      int y = yStart + (row * itemStackHeight);
      for (int column = 0; column < columnCount; column++) {
        int x = xStart + (column * itemStackWidth);
        GuiIngredientFast guiIngredientFast = new GuiIngredientFast(x, y, itemStackPadding);
        if (guiAreas != null) {
          Rectangle stackArea = guiIngredientFast.getArea();
          if (intersects(guiAreas, stackArea)) {
            continue;
          }
        }
        guiItemStacks.add(guiIngredientFast);
      }
    }
  }

  private static boolean intersects(List<Rectangle> areas, Rectangle comparisonArea) {
    for (Rectangle area : areas) {
      if (area.intersects(comparisonArea)) {
        return true;
      }
    }
    return false;
  }

  public void close() {
    setKeyboardFocus(false);
    Config.saveFilterText();
  }
  	public boolean hasScreenChanged(GuiScreen guiScreen) {
		if (!Config.isOverlayEnabled()) {
			return true;
		}
		GuiProperties guiProperties = GuiProperties.create(guiScreen);
		if (guiProperties == null) {
			return true;
		}
		if (!this.guiProperties.equals(guiProperties)) {
			return true;
		} else if (!activeAdvancedGuiHandlers.isEmpty() && guiScreen instanceof GuiContainer) {
			GuiContainer guiContainer = (GuiContainer) guiScreen;
			List<Rectangle> guiAreas = getGuiAreas(guiContainer);
			if (!Java6Helper.equals(this.guiAreas, guiAreas)) {
				return true;
			}
		}

		return false;
	}
}
