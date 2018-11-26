package mezz.jei.gui.overlay.bookmarks;

import mezz.jei.Internal;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.bookmarks.BookmarkList;
import mezz.jei.config.Config;
import mezz.jei.gui.GuiHelper;
import mezz.jei.gui.PageNavigation;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.gui.ingredients.GuiItemStackGroup;
import mezz.jei.gui.overlay.IngredientGrid;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.input.ClickedIngredient;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IPaged;
import mezz.jei.input.IShowsRecipeFocuses;
import mezz.jei.runtime.JeiRuntime;
import mezz.jei.util.CommandUtil;
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

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BookmarkOverlay implements IPaged, IShowsRecipeFocuses, ILeftAreaContent {

	private static class Bookmark {
		final Rectangle area;
		final Object ingredient;

		Bookmark(Rectangle area, Object ingredient) {
			this.area = area;
			this.ingredient = ingredient;
		}
	}

	private static final int BORDER_PADDING = 2;
	private static final int NAVIGATION_HEIGHT = 20;
	private static final int BUTTON_SIZE = 20;
	private static final int INGREDIENT_PADDING = 1;
	private static final int INGREDIENT_WIDTH = GuiItemStackGroup.getWidth(INGREDIENT_PADDING);
	private static final int INGREDIENT_HEIGHT = GuiItemStackGroup.getHeight(INGREDIENT_PADDING);

	// areas
	private Rectangle parentArea;
	private Rectangle bookmarkArea = new Rectangle();
	private Rectangle naviArea = new Rectangle();
	private Set<Rectangle> exclusionAreas = Collections.emptySet();

	// display elements
	private final PageNavigation navigation;
	private final List<Rectangle> slots = new ArrayList<>();
	private final List<Bookmark> screen = new ArrayList<>();
	private final GuiIconToggleButton bookmarkButton;

	// visibility
	private boolean hasRoom = false;

	// paging
	private int page = 1;
	private int pages = 1;
	private int perPage = 0;

	// data
	private final BookmarkList bookmarkList;

	public BookmarkOverlay(Rectangle area, BookmarkList bookmarkList, GuiHelper guiHelper) {
		this.parentArea = area;
		this.bookmarkList = bookmarkList;
		this.navigation = new PageNavigation(this, false);
		this.bookmarkButton = BookmarkButton.create(this, bookmarkList, guiHelper);
	}

	private boolean isListDisplayed() {
		return Config.isBookmarkOverlayEnabled() && hasRoom && !bookmarkList.isEmpty();
	}

	public boolean hasRoom() {
		return hasRoom;
	}

	@Override
	public void updateBounds(Rectangle area) {
		this.parentArea = area;
		hasRoom = updateBounds();
		computeSlots();
	}

	public void renderNavigation(int mouseX, int mouseY) {
		if (isListDisplayed() && naviArea.width > 0) {
			navigation.draw(Minecraft.getMinecraft(), mouseX, mouseY, 0);
		}
	}

	@Override
	public void drawScreen(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
		if (Config.isBookmarkOverlayEnabled()) {
			renderBookmarks(mouseX, mouseY);
			renderNavigation(mouseX, mouseY);
		}
		this.bookmarkButton.draw(minecraft, mouseX, mouseY, partialTicks);
	}

	@Override
	public void drawOnForeground(GuiContainer gui, int mouseX, int mouseY) {
	}

	@Override
	public void drawTooltips(Minecraft minecraft, int mouseX, int mouseY) {
		if (isListDisplayed() && !screen.isEmpty()) {
			Bookmark bookmarkUnderMouse = getBookmarkUnderMouse(mouseX, mouseY);
			if (bookmarkUnderMouse != null) {
				drawHighlight(bookmarkUnderMouse.area.x, bookmarkUnderMouse.area.y, INGREDIENT_WIDTH, INGREDIENT_HEIGHT);
				renderTooltip(mouseX, mouseY, bookmarkUnderMouse.ingredient);
			}
		}
		bookmarkButton.drawTooltips(minecraft, mouseX, mouseY);
	}

	@Nullable
	private Bookmark getBookmarkUnderMouse(int mouseX, int mouseY) {
		if (!screen.isEmpty() && bookmarkArea.contains(mouseX, mouseY)) {
			for (Bookmark entry : screen) {
				if (entry.area.contains(mouseX, mouseY)) {
					return entry;
				}
			}
		}
		return null;
	}

	public void renderBookmarks(int mouseX, int mouseY) {
		if (!isListDisplayed()) {
			return;
		}
		screen.clear();

		if (slots.isEmpty()) {
			return;
		}

		List<Object> list = bookmarkList.get();
		if (list.isEmpty()) {
			return;
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
						return;
					}
				}
			}
		}
		if (skip > 0) {
			// weird...
			previousPage();
		}
	}

	private static int getMinWidth() {
		return Math.max(4 * BUTTON_SIZE, Config.smallestNumColumns * IngredientGrid.INGREDIENT_WIDTH);
	}

	public boolean updateBounds() {
		exclusionAreas = getGuiAreas();
		bookmarkArea = new Rectangle(parentArea);
		bookmarkArea = new Rectangle(bookmarkArea);
		// TODO: pull up to parentArea
		// bookmarkArea.width = guiProperties.getGuiLeft();
		// bookmarkArea.x = 2 * BORDER_PADDING;
		bookmarkArea.height -= NAVIGATION_HEIGHT + BUTTON_SIZE + 4;
		naviArea = new Rectangle(bookmarkArea);

		bookmarkArea.y += BORDER_PADDING + NAVIGATION_HEIGHT;
		this.bookmarkButton.updateBounds(new Rectangle(
			bookmarkArea.x,
			(int) Math.floor(bookmarkArea.getMaxY()),
			BUTTON_SIZE,
			BUTTON_SIZE
		));

		int yCenteringOffset = (bookmarkArea.height - ((bookmarkArea.height / INGREDIENT_HEIGHT) * INGREDIENT_HEIGHT)) / 2;
		bookmarkArea.y += yCenteringOffset;
		bookmarkArea.height -= yCenteringOffset;

		if (bookmarkArea.width < getMinWidth()) {
			return false;
		}

		bookmarkArea.width = (bookmarkArea.width / INGREDIENT_WIDTH) * INGREDIENT_WIDTH;

		naviArea.width = bookmarkArea.width;
		naviArea.height = NAVIGATION_HEIGHT;
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
	private static Set<Rectangle> getGuiAreas() {
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
		return Collections.emptySet();
	}

	// mezz.jei.gui.recipes.RecipeCategoryTab.renderIngredient(Minecraft, int, int, T)
	private static <T> void renderIngredient(int iconX, int iconY, T ingredient) {
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
	private static <V> List<String> getIngredientTooltipSafe(IIngredientRenderer<V> ingredientRenderer, V ingredient) {
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
		List<Object> list = bookmarkList.get();
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

	@Override
	public boolean handleMouseScrolled(int mouseX, int mouseY, int dWheel) {
		if (dWheel < 0) {
			nextPage();
		} else {
			previousPage();
		}
		return true;
	}

	@Override
	public boolean handleMouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (bookmarkArea.contains(mouseX, mouseY)) {
			Minecraft minecraft = Minecraft.getMinecraft();
			GuiScreen currentScreen = minecraft.currentScreen;
			if (currentScreen != null && !(currentScreen instanceof RecipesGui)
				&& (mouseButton == 0 || mouseButton == 1 || minecraft.gameSettings.keyBindPickBlock.isActiveAndMatches(mouseButton - 100))) {
				IClickedIngredient<?> clicked = getIngredientUnderMouse(mouseX, mouseY);
				if (clicked != null) {
					if (Config.isCheatItemsEnabled()) {
						ItemStack itemStack = clicked.getCheatItemStack();
						if (!itemStack.isEmpty()) {
							CommandUtil.giveStack(itemStack, mouseButton);
						}
						clicked.onClickHandled();
						return true;
					}
				}
			}
		}
		if (bookmarkButton.isMouseOver(mouseX, mouseY)) {
			return bookmarkButton.handleMouseClick(mouseX, mouseY);
		}
		return navigation.isMouseOver() && navigation.handleMouseClickedButtons(mouseX, mouseY);
	}

}
