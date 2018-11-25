package mezz.jei.gui.overlay.bookmarks;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.lwjgl.input.Keyboard;

import mezz.jei.Internal;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.api.gui.IGuiProperties;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.PageNavigation;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.gui.ingredients.GuiItemStackGroup;
import mezz.jei.input.ClickedIngredient;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IPaged;
import mezz.jei.input.IShowsRecipeFocuses;
import mezz.jei.runtime.JeiRuntime;
import mezz.jei.util.Log;
import mezz.jei.util.MathUtil;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiUtils;

public class BookmarkOverlay implements IPaged, IShowsRecipeFocuses, ILeftAreaContent {

  private static class Bookmark {
    final @Nonnull Rectangle area;
    final @Nonnull Object ingredient;

    Bookmark(@Nonnull Rectangle area, @Nonnull Object ingredient) {
      this.area = area;
      this.ingredient = ingredient;
    }
  }

  private static final int BORDER_PADDING = 2;
  private static final int NAVIGATION_HEIGHT = 20;
  private static final int SEARCH_HEIGHT = 16;
  private static final int INGREDIENT_PADDING = 1;
  private static final int INGREDIENT_WIDTH = GuiItemStackGroup.getWidth(INGREDIENT_PADDING);
  private static final int INGREDIENT_HEIGHT = GuiItemStackGroup.getHeight(INGREDIENT_PADDING);

  // areas
  private @Nonnull Rectangle parentArea;
  private @Nonnull Rectangle bookmarkArea = new Rectangle();
  private @Nonnull Rectangle naviArea = new Rectangle();
  private @Nonnull Rectangle textArea = new Rectangle();
  private @Nonnull Set<Rectangle> exclusionAreas = emptySet();

  // display elements
  private final @Nonnull PageNavigation navigation;
  private final @Nonnull List<Rectangle> slots = new ArrayList<>();
  private final @Nonnull List<Bookmark> screen = new ArrayList<>();

  // visibility
  private boolean canBeVisible = false;

  // paging
  private int page = 1;
  private int pages = 1;
  private int perPage = 0;

  public BookmarkOverlay(@Nonnull Rectangle area) {
    this.parentArea = area;
    navigation = new PageNavigation(this, false);
    updateBounds(area);
  }

  @Override
  public void updateBounds(@Nonnull Rectangle area) {
    this.parentArea = area;
    canBeVisible = updateBounds();
    computeSlots();
  }

  public boolean renderNavigation(int mouseX, int mouseY) {
    if (canBeVisible && naviArea.width > 0) {
      navigation.draw(Minecraft.getMinecraft(), mouseX, mouseY, 0);
      return true;
    }
    return false;
  }

  @Override
  public void drawScreen(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
    renderBookmarks(mouseX, mouseY);
    renderNavigation(mouseX, mouseY);
    renderKeyHint(mouseX, mouseY);
  }

  @Override
  public void drawOnForeground(GuiContainer gui, int mouseX, int mouseY) {
    if (canBeVisible && !screen.isEmpty()) {
      Bookmark bookmarkUnderMouse = getBookmarkUnderMouse(mouseX, mouseY);
      if (bookmarkUnderMouse != null) {
        drawHighlight(bookmarkUnderMouse.area.x, bookmarkUnderMouse.area.y, INGREDIENT_WIDTH, INGREDIENT_HEIGHT);
      }
    }
  }

  @Override
  public void drawTooltips(Minecraft minecraft, int mouseX, int mouseY) {
    if (canBeVisible && !screen.isEmpty()) {
      Bookmark bookmarkUnderMouse = getBookmarkUnderMouse(mouseX, mouseY);
      if (bookmarkUnderMouse != null) {
        renderTooltip(mouseX, mouseY, bookmarkUnderMouse.ingredient);
      }
    }
  }

  private @Nullable Bookmark getBookmarkUnderMouse(int mouseX, int mouseY) {
    if (!screen.isEmpty() && bookmarkArea.contains(mouseX, mouseY)) {
      for (Bookmark entry : screen) {
        if (entry.area.contains(mouseX, mouseY)) {
          return entry;
        }
      }
    }
    return null;
  }

  public boolean renderKeyHint(int mouseX, int mouseY) {
    if (!canBeVisible) {
      return false;
    }
    FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
    String str = (KeyBindings.toggleBookmarkOverlay.getKeyCode() != Keyboard.KEY_NONE)
        ? Translator.translateToLocalFormatted("gui.jei.bookmarks.key", KeyBindings.toggleBookmarkOverlay.getDisplayName())
        : Translator.translateToLocal("gui.jei.bookmarks.nokey");
    int width = fontRenderer.getStringWidth(str);
    int txtX = Math.max((int) (textArea.getCenterX() - width / 2), BORDER_PADDING);
    int txtY = (int) (textArea.getCenterY() - 8 / 2);
    fontRenderer.drawString(str, txtX, txtY, Color.white.getRGB(), true);
    return true;
  }

  public boolean renderBookmarks(int mouseX, int mouseY) {
    if (!canBeVisible) {
      return false;
    }
    screen.clear();

    if (slots.isEmpty()) {
      return false;
    }

    List<Object> list = Internal.getBookmarkList().get();
    if (list.isEmpty()) {
      return false;
    }

    setItemsPerPage(slots.size());

    int skip = getSkip();
    int slotIdx = 0;
    for (Object object : list) {
      if (object != null) {
        if (skip-- <= 0) {
          Rectangle area = slots.get(slotIdx++);
          renderIngredient(area.x + INGREDIENT_PADDING, area.y + INGREDIENT_PADDING, object);
          screen.add(new Bookmark(area, object));
          if (slotIdx >= slots.size()) {
            return true;
          }
        }
      }
    }
    if (skip > 0) {
      // weird...
      previousPage();
    }

    return true;
  }

  public boolean updateBounds() {
    JeiRuntime runtime = Internal.getRuntime();
    if (runtime == null) {
      return false;
    }

    exclusionAreas = getGuiAreas();
    bookmarkArea = new Rectangle(parentArea);
    bookmarkArea = new Rectangle(bookmarkArea);
    // TODO: pull up to parentArea
    // bookmarkArea.width = guiProperties.getGuiLeft();
    // bookmarkArea.x = 2 * BORDER_PADDING;
    bookmarkArea.height -= NAVIGATION_HEIGHT + SEARCH_HEIGHT + 4;
    naviArea = new Rectangle(bookmarkArea);

    bookmarkArea.y += BORDER_PADDING + NAVIGATION_HEIGHT;
    textArea = new Rectangle(bookmarkArea);
    textArea.y = (int) bookmarkArea.getMaxY();
    textArea.height = SEARCH_HEIGHT;

    int yCenteringOffset = (bookmarkArea.height - ((bookmarkArea.height / INGREDIENT_HEIGHT) * INGREDIENT_HEIGHT)) / 2;
    bookmarkArea.y += yCenteringOffset;
    bookmarkArea.height -= yCenteringOffset;

    int guiLeft = (int) (parentArea.getMaxX() - BORDER_PADDING);
    IGuiProperties guiProperties = runtime.getGuiProperties(Minecraft.getMinecraft().currentScreen);
    if (guiProperties != null && guiProperties.getGuiClass() == mezz.jei.gui.recipes.RecipesGui.class && exclusionAreas.isEmpty()) {
      // JEI doesn't define an exclusion area for its own side-tabs at the moment
      guiLeft -= INGREDIENT_WIDTH + 4;
    }

    while (bookmarkArea.getMaxX() > (guiLeft)) {
      bookmarkArea.width--;
      if (bookmarkArea.width <= INGREDIENT_WIDTH) {
        return false;
      }
    }

    bookmarkArea.width = (bookmarkArea.width / INGREDIENT_WIDTH) * INGREDIENT_WIDTH;

    naviArea.width = bookmarkArea.width;
    naviArea.height = NAVIGATION_HEIGHT;
    textArea.width = bookmarkArea.width;
    navigation.updateBounds(naviArea);
    navigation.updatePageState();
    return true;
  }

  private void computeSlots() {
    slots.clear();
    int x = (int) bookmarkArea.getMinX(), y = (int) bookmarkArea.getMinY();
    while (true) {
      Rectangle area = new Rectangle(x, y, INGREDIENT_WIDTH, INGREDIENT_HEIGHT);
      if (!MathUtil.intersects(exclusionAreas, area)) {
        slots.add(area);
      }
      x += INGREDIENT_WIDTH;
      if (x + INGREDIENT_WIDTH > bookmarkArea.getMaxX()) {
        x = (int) bookmarkArea.getMinX();
        y += INGREDIENT_HEIGHT;
        if (y + INGREDIENT_HEIGHT > bookmarkArea.getMaxY()) {
          return;
        }
      }
    }
  }

  // mezz.jei.gui.overlay.IngredientGridAll.getGuiAreas()
  private static @Nonnull Set<Rectangle> getGuiAreas() {
    JeiRuntime runtime = Internal.getRuntime();
    if (runtime != null) {
      final GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
      if (currentScreen instanceof GuiContainer) {
        final GuiContainer guiContainer = (GuiContainer) currentScreen;
        final Set<Rectangle> allGuiExtraAreas = new HashSet<>();
        final List<IAdvancedGuiHandler<GuiContainer>> activeAdvancedGuiHandlers = runtime.getActiveAdvancedGuiHandlers(guiContainer);
        for (IAdvancedGuiHandler<GuiContainer> advancedGuiHandler : activeAdvancedGuiHandlers) {
          final List<Rectangle> guiExtraAreas = advancedGuiHandler.getGuiExtraAreas(guiContainer);
          if (guiExtraAreas != null) {
            allGuiExtraAreas.addAll(guiExtraAreas);
          }
        }
        return allGuiExtraAreas;
      }
    }
    return emptySet();
  }

  // mezz.jei.gui.recipes.RecipeCategoryTab.renderIngredient(Minecraft, int, int, T)
  private static <T> void renderIngredient(int iconX, int iconY, @Nonnull T ingredient) {
    IIngredientRegistry ingredientRegistry = Internal.getIngredientRegistry();
    IIngredientRenderer<T> ingredientRenderer = ingredientRegistry.getIngredientRenderer(ingredient);
    GlStateManager.enableDepth();
    GlStateManager.disableLighting();
    ingredientRenderer.render(Minecraft.getMinecraft(), iconX, iconY, ingredient);
    GlStateManager.enableAlpha();
  }

  // mezz.jei.render.IngredientRenderer.drawTooltip(Minecraft, int, int)
  private static <T> void renderTooltip(int iconX, int iconY, @Nullable T ingredient) {
    if (ingredient == null) {
      return;
    }
    IIngredientRegistry ingredientRegistry = Internal.getIngredientRegistry();
    IIngredientRenderer<T> ingredientRenderer = ingredientRegistry.getIngredientRenderer(ingredient);
    List<String> tooltip = getIngredientTooltipSafe(ingredientRenderer, ingredient);
    FontRenderer fontRenderer = ingredientRenderer.getFontRenderer(Minecraft.getMinecraft(), ingredient);

    if (ingredient instanceof ItemStack) {
      ItemStack itemStack = (ItemStack) ingredient;
      TooltipRenderer.drawHoveringText(itemStack, Minecraft.getMinecraft(), tooltip, iconX, iconY, fontRenderer);
    } else {
      TooltipRenderer.drawHoveringText(Minecraft.getMinecraft(), tooltip, iconX, iconY, fontRenderer);
    }
  }

  // mezz.jei.render.IngredientRenderer.getIngredientTooltipSafe(Minecraft, IIngredientListElement<V>)
  private static @Nonnull <V> List<String> getIngredientTooltipSafe(@Nonnull IIngredientRenderer<V> ingredientRenderer, @Nonnull V ingredient) {
    try {
      ITooltipFlag.TooltipFlags tooltipFlag = Minecraft.getMinecraft().gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED
          : ITooltipFlag.TooltipFlags.NORMAL;
      return ingredientRenderer.getTooltip(Minecraft.getMinecraft(), ingredient, tooltipFlag);
    } catch (RuntimeException | LinkageError e) {
      Log.get().error("Tooltip crashed.", e);
    }

    List<String> tooltip = new ArrayList<>();
    tooltip.add(TextFormatting.RED + Translator.translateToLocal("jei.tooltip.error.crash"));
    return tooltip;
  }

  // mezz.jei.render.IngredientRenderer.drawHighlight()
  private static void drawHighlight(int iconX, int iconY, int width, int height) {
    GlStateManager.disableLighting();
    GlStateManager.disableDepth();
    GlStateManager.colorMask(true, true, true, false);
    GuiUtils.drawGradientRect(0, iconX, iconY, iconX + width, iconY + height, 0x80FFFFFF, 0x80FFFFFF);
    GlStateManager.colorMask(true, true, true, true);
    GlStateManager.enableDepth();
  }

  void setItemsPerPage(int items) {
    List<Object> list = Internal.getBookmarkList().get();
    perPage = items;
    if (list.isEmpty() || items < 1) {
      page = pages = 1;
    } else {
      pages = (int) Math.ceil(list.size() / (double) items);
      if (page > pages) {
        page = pages;
      }
    }
    navigation.updatePageState();
  }

  int getSkip() {
    return (page - 1) * perPage;
  }

  @Override
  public boolean nextPage() {
    page++;
    if (page > pages) {
      page = 1;
    }
    navigation.updatePageState();
    return true;
  }

  @Override
  public boolean previousPage() {
    page--;
    if (page < 1) {
      page = pages;
    }
    navigation.updatePageState();
    return true;
  }

  @Override
  public boolean hasNext() {
    return page < pages;
  }

  @Override
  public boolean hasPrevious() {
    return page > 1;
  }

  @Override
  public int getPageCount() {
    return pages;
  }

  @Override
  public int getPageNumber() {
    return page - 1;
  }

  @SuppressWarnings({ "unchecked", "cast", "null" })
  public static final @Nonnull <T> Set<T> emptySet() {
    return (Set<T>) Collections.EMPTY_SET;
  }

  @Override
  @Nullable
  public IClickedIngredient<?> getIngredientUnderMouse(int mouseX, int mouseY) {
    Bookmark underMouse = getBookmarkUnderMouse(mouseX, mouseY);
    if (underMouse != null) {
      ClickedIngredient<Object> clicked = ClickedIngredient.create(underMouse.ingredient, underMouse.area);
      if (clicked != null) {
        clicked.setAllowsCheating();
      }
      return clicked;
    }
    return null;
  }

  @Override
  public boolean canSetFocusWithMouse() {
    return true;
  }

}
