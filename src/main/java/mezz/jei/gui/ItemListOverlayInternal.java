package mezz.jei.gui;

import javax.annotation.Nullable;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import mezz.jei.Internal;
import mezz.jei.JustEnoughItems;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.config.Config;
import mezz.jei.config.Constants;
import mezz.jei.config.JEIModConfigGui;
import mezz.jei.gui.ingredients.GuiIngredientFast;
import mezz.jei.gui.ingredients.GuiIngredientFastList;
import mezz.jei.gui.ingredients.GuiItemStackGroup;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.input.ClickedIngredient;
import mezz.jei.input.GuiTextFieldFilter;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IKeyable;
import mezz.jei.input.IMouseHandler;
import mezz.jei.input.IShowsRecipeFocuses;
import mezz.jei.network.packets.PacketDeletePlayerItem;
import mezz.jei.network.packets.PacketJei;
import mezz.jei.util.Java6Helper;
import mezz.jei.util.MathUtil;
import mezz.jei.util.StackHelper;
import mezz.jei.util.Translator;
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
import net.minecraftforge.fml.client.config.HoverChecker;
import org.lwjgl.input.Keyboard;

public class ItemListOverlayInternal implements IShowsRecipeFocuses, IMouseHandler, IKeyable {

	private static final int borderPadding = 2;
	private static final int searchHeight = 16;
	private static final int buttonSize = 20;
	private static final String nextLabel = ">";
	private static final String backLabel = "<";

	private static final int itemStackPadding = 1;
	private static final int itemStackWidth = GuiItemStackGroup.getWidth(itemStackPadding);
	private static final int itemStackHeight = GuiItemStackGroup.getHeight(itemStackPadding);
	private static int firstItemIndex = 0;

	private final ItemListOverlay parent;

	private final GuiButton nextButton;
	private final GuiButton backButton;
	private final GuiButton configButton;
	private final GuiButton clearButton;
	private final IDrawable configButtonIcon;
	private final IDrawable configButtonCheatIcon;
	private final HoverChecker configButtonHoverChecker;
	private final GuiTextFieldFilter searchField;

	private String pageNumDisplayString = "1/1";
	private int pageNumDisplayX;
	private int pageNumDisplayY;

	private final GuiIngredientFastList guiIngredientList;
	private final GuiIngredientFastList guiBookmarks;
	@Nullable
	private GuiIngredientFast hovered = null;

	// properties of the gui we're beside
	private final GuiProperties guiProperties;
	private final List<Rectangle> guiAreas;
	private List<IAdvancedGuiHandler<?>> activeAdvancedGuiHandlers = Collections.emptyList();

	public ItemListOverlayInternal(ItemListOverlay parent, IIngredientRegistry ingredientRegistry, GuiScreen guiScreen, GuiProperties guiProperties) {
		this.parent = parent;

		this.guiBookmarks = new GuiIngredientFastList(ingredientRegistry);
		this.guiIngredientList = new GuiIngredientFastList(ingredientRegistry);

		this.guiProperties = guiProperties;
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
		final int xEmptySpace = guiProperties.getScreenWidth() - guiProperties.getGuiLeft() - guiProperties.getGuiXSize() - xSize;

		final int leftEdge = guiProperties.getGuiLeft() + guiProperties.getGuiXSize() + (xEmptySpace / 2);
		final int rightEdge = leftEdge + xSize;

		final int yItemButtonSpace = getItemButtonYSpace(guiProperties);
		final int itemButtonsHeight = rows * itemStackHeight;

		final int buttonStartY = buttonSize + (2 * borderPadding) + (yItemButtonSpace - itemButtonsHeight) / 2;
		// this renders the items gui
		createItemButtons(guiIngredientList, guiAreas, leftEdge, buttonStartY, columns, rows);
		// Alight with bottom of clear button
		createItemButtons(guiBookmarks, guiAreas, 0, guiProperties.getGuiTop() + buttonSize, columns, rows);

		nextButton = new GuiButton(0, rightEdge - buttonSize, borderPadding, buttonSize, buttonSize, nextLabel);
		backButton = new GuiButton(1, leftEdge, borderPadding, buttonSize, buttonSize, backLabel);
		// align with the top of inventory gui
		clearButton = new GuiButton(3, 0, guiProperties.getGuiTop(), buttonSize * 3, buttonSize, "CLEAR");

		final int searchFieldX;
		final int searchFieldY = guiProperties.getScreenHeight() - searchHeight - borderPadding - 2;
		final int searchFieldWidth;

		if (isSearchBarCentered(guiProperties)) {
			searchFieldX = guiProperties.getGuiLeft();
			searchFieldWidth = guiProperties.getGuiXSize() - buttonSize - 1;
		} else {
			searchFieldX = leftEdge;
			searchFieldWidth = rightEdge - leftEdge - buttonSize - 1;
		}

		FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
		searchField = new GuiTextFieldFilter(0, fontRenderer, searchFieldX, searchFieldY, searchFieldWidth, searchHeight, parent.getItemFilter());
		setKeyboardFocus(false);

		int configButtonX = searchFieldX + searchFieldWidth + 1;
		int configButtonY = guiProperties.getScreenHeight() - buttonSize - borderPadding;
		configButton = new GuiButton(2, configButtonX, configButtonY, buttonSize, buttonSize, "");
		ResourceLocation configButtonIconLocation = new ResourceLocation(Constants.RESOURCE_DOMAIN, Constants.TEXTURE_RECIPE_BACKGROUND_PATH);
		GuiHelper guiHelper = Internal.getHelpers().getGuiHelper();
		configButtonIcon = guiHelper.createDrawable(configButtonIconLocation, 0, 166, 16, 16);
		configButtonCheatIcon = guiHelper.createDrawable(configButtonIconLocation, 16, 166, 16, 16);
		configButtonHoverChecker = new HoverChecker(configButton, 0);

		updateLayout();
	}

	private static boolean isSearchBarCentered(GuiProperties guiProperties) {
		return Config.isCenterSearchBarEnabled() &&
				guiProperties.getGuiTop() + guiProperties.getGuiYSize() + searchHeight < guiProperties.getScreenHeight();
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

	@Nullable
	private <T extends GuiContainer> List<Rectangle> getGuiAreas(GuiContainer gui, IAdvancedGuiHandler<T> advancedGuiHandler) {
		Class<T> guiClass = advancedGuiHandler.getGuiContainerClass();
		if (guiClass.isInstance(gui)) {
			T guiT = guiClass.cast(gui);
			return advancedGuiHandler.getGuiExtraAreas(guiT);
		}
		return null;
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

	private static void createItemButtons(GuiIngredientFastList guiItemStacks, @Nullable List<Rectangle> guiAreas, final int xStart, final int yStart, final int columnCount, final int rowCount) {
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

	public void updateLayout() {
		ImmutableList<IIngredientListElement> ingredientList = parent.getItemFilter().getIngredientList();
		guiIngredientList.set(firstItemIndex, ingredientList);

		List<IIngredientListElement> bookmarkList = parent.getIngredientBookmarks().getIngredientList();
		guiBookmarks.set(0, bookmarkList);

		FontRenderer fontRendererObj = Minecraft.getMinecraft().fontRendererObj;

		pageNumDisplayString = (getPageNum() + 1) + "/" + getPageCount();
		int pageDisplayWidth = fontRendererObj.getStringWidth(pageNumDisplayString);
		pageNumDisplayX = ((backButton.xPosition + backButton.width) + nextButton.xPosition) / 2 - (pageDisplayWidth / 2);
		pageNumDisplayY = backButton.yPosition + Math.round((backButton.height - fontRendererObj.FONT_HEIGHT) / 2.0f);

		searchField.update();
	}

	private void nextPage() {
		final int itemsCount = parent.getItemFilter().size();
		if (itemsCount == 0) {
			firstItemIndex = 0;
			return;
		}

		firstItemIndex += guiIngredientList.size();
		if (firstItemIndex >= itemsCount) {
			firstItemIndex = 0;
		}
		updateLayout();
	}

	private void previousPage() {
		final int itemsPerPage = guiIngredientList.size();
		if (itemsPerPage == 0) {
			firstItemIndex = 0;
			return;
		}
		final int itemsCount = parent.getItemFilter().size();

		int pageNum = firstItemIndex / itemsPerPage;
		if (pageNum == 0) {
			pageNum = itemsCount / itemsPerPage;
		} else {
			pageNum--;
		}

		firstItemIndex = itemsPerPage * pageNum;
		if (firstItemIndex > 0 && firstItemIndex == itemsCount) {
			pageNum--;
			firstItemIndex = itemsPerPage * pageNum;
		}
		updateLayout();
	}

	public void drawScreen(Minecraft minecraft, int mouseX, int mouseY) {
		GlStateManager.disableLighting();

		minecraft.fontRendererObj.drawString(pageNumDisplayString, pageNumDisplayX, pageNumDisplayY, Color.white.getRGB(), true);
		searchField.drawTextBox();

		nextButton.drawButton(minecraft, mouseX, mouseY);
		backButton.drawButton(minecraft, mouseX, mouseY);
		configButton.drawButton(minecraft, mouseX, mouseY);
		clearButton.drawButton(minecraft, mouseX, mouseY);

		IDrawable icon = Config.isCheatItemsEnabled() ? configButtonCheatIcon : configButtonIcon;
		icon.draw(minecraft, configButton.xPosition + 2, configButton.yPosition + 2);

		GlStateManager.disableBlend();

		if (shouldShowDeleteItemTooltip(minecraft)) {
			hovered = guiIngredientList.render(minecraft, false, mouseX, mouseY);
		} else {
			boolean mouseOver = isMouseOver(mouseX, mouseY);
			hovered = guiIngredientList.render(minecraft, mouseOver, mouseX, mouseY);
		}

		if (hovered == null) {
			hovered = guiBookmarks.render(minecraft, isMouseOver(mouseX, mouseY), mouseX, mouseY);
		} else {
			guiBookmarks.render(minecraft, isMouseOver(mouseX, mouseY), mouseX, mouseY);
		}

		Set<ItemStack> highlightedStacks = parent.getHighlightedStacks();
		if (!highlightedStacks.isEmpty()) {
			StackHelper helper = Internal.getHelpers().getStackHelper();
			for (GuiIngredientFast guiItemStack : guiIngredientList.getAllGuiIngredients()) {
				Object ingredient = guiItemStack.getIngredient();
				if (ingredient instanceof ItemStack) {
					if (helper.containsStack(highlightedStacks, (ItemStack) ingredient) != null) {
						guiItemStack.drawHighlight();
					}
				}
			}
			for (GuiIngredientFast guiItemStack : guiBookmarks.getAllGuiIngredients()) {
				Object ingredient = guiItemStack.getIngredient();
				if (ingredient instanceof ItemStack) {
					if (helper.containsStack(highlightedStacks, (ItemStack) ingredient) != null) {
						guiItemStack.drawHighlight();
					}
				}
			}
		}

		if (hovered != null) {
			hovered.drawHovered(minecraft);
		}

		GlStateManager.enableAlpha();
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

	public void drawTooltips(Minecraft minecraft, int mouseX, int mouseY) {
		boolean mouseOver = isMouseOver(mouseX, mouseY);
		if (mouseOver && shouldShowDeleteItemTooltip(minecraft)) {
			String deleteItem = Translator.translateToLocal("jei.tooltip.delete.item");
			TooltipRenderer.drawHoveringText(minecraft, deleteItem, mouseX, mouseY);
		}

		if (hovered != null) {
			hovered.drawTooltip(minecraft, mouseX, mouseY);
		}

		if (configButtonHoverChecker.checkHover(mouseX, mouseY)) {
			String configString = Translator.translateToLocal("jei.tooltip.config");
			if (Config.isCheatItemsEnabled()) {
				List<String> tooltip = Arrays.asList(
						configString,
						TextFormatting.RED + Translator.translateToLocal("jei.tooltip.cheat.mode")
				);
				TooltipRenderer.drawHoveringText(minecraft, tooltip, mouseX, mouseY);
			} else {
				TooltipRenderer.drawHoveringText(minecraft, configString, mouseX, mouseY);
			}
		}
	}

	public void handleTick() {
		searchField.updateCursorCounter();
	}

	@Override
	public boolean isMouseOver(int mouseX, int mouseY) {
		// Clickable area is anywhere outside of the inventory screen. Should probably
		// narrow this down, but I don't know how do detect other mods that might be
		// using the screen.
		if (mouseX > guiProperties.getGuiLeft() && mouseX < guiProperties.getGuiLeft() + guiProperties.getGuiXSize()
				&& mouseY > guiProperties.getGuiTop() && mouseY < guiProperties.getGuiTop() + guiProperties.getGuiYSize()) {
			return false;
		}
		// if (mouseX < guiProperties.getGuiLeft() + guiProperties.getGuiXSize()) {
		// return isSearchBarCentered(guiProperties) &&
		// (searchField.isMouseOver(mouseX, mouseY) ||
		// configButtonHoverChecker.checkHover(mouseX, mouseY));
		// }

		// for (Rectangle guiArea : guiAreas) {
		// if (guiArea.contains(mouseX, mouseY)) {
		// return false;
		// }
		// }

		return true;
	}

	@Override
	@Nullable
	public IClickedIngredient<?> getIngredientUnderMouse(int mouseX, int mouseY) {
		if (!isMouseOver(mouseX, mouseY)) {
			return null;
		}

		ClickedIngredient<?> clicked = guiIngredientList.getIngredientUnderMouse(mouseX, mouseY);
		if (clicked != null) {
			setKeyboardFocus(false);
			clicked.setAllowsCheating();
			return clicked;
		}
		clicked = guiBookmarks.getIngredientUnderMouse(mouseX, mouseY);
		if (clicked != null) {
			setKeyboardFocus(false);
			clicked.setAllowsCheating();
			return clicked;
		}
		return null;
	}

	@Override
	public boolean canSetFocusWithMouse() {
		return true;
	}

	@Override
	public boolean handleMouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (!isMouseOver(mouseX, mouseY)) {
			setKeyboardFocus(false);
			return false;
		}

		if (Config.isDeleteItemsInCheatModeActive()) {
			Minecraft minecraft = Minecraft.getMinecraft();
			EntityPlayerSP player = minecraft.thePlayer;
			ItemStack itemStack = player.inventory.getItemStack();
			if (itemStack != null) {
				player.inventory.setItemStack(null);
				PacketJei packet = new PacketDeletePlayerItem(itemStack);
				JustEnoughItems.getProxy().sendPacketToServer(packet);
				return true;
			}
		}

		boolean buttonClicked = handleMouseClickedButtons(mouseX, mouseY);
		if (buttonClicked) {
			setKeyboardFocus(false);
			return true;
		}

		return handleMouseClickedSearch(mouseX, mouseY, mouseButton);
	}

	@Override
	public boolean handleMouseScrolled(int mouseX, int mouseY, int scrollDelta) {
		if (!isMouseOver(mouseX, mouseY)) {
			return false;
		}
		if (scrollDelta < 0) {
			nextPage();
			return true;
		} else if (scrollDelta > 0) {
			previousPage();
			return true;
		}
		return false;
	}

	private boolean handleMouseClickedButtons(int mouseX, int mouseY) {
		Minecraft minecraft = Minecraft.getMinecraft();
		if (nextButton.mousePressed(minecraft, mouseX, mouseY)) {
			nextPage();
			nextButton.playPressSound(minecraft.getSoundHandler());
			return true;
		} else if (backButton.mousePressed(minecraft, mouseX, mouseY)) {
			previousPage();
			backButton.playPressSound(minecraft.getSoundHandler());
			return true;
		} else if (configButton.mousePressed(minecraft, mouseX, mouseY)) {
			configButton.playPressSound(minecraft.getSoundHandler());
			if (Keyboard.getEventKeyState() && (Keyboard.getEventKey() == Keyboard.KEY_LCONTROL || Keyboard.getEventKey() == Keyboard.KEY_RCONTROL)) {
				Config.toggleCheatItemsEnabled();
			} else {
				if (minecraft.currentScreen != null) {
					parent.close();
					GuiScreen configScreen = new JEIModConfigGui(minecraft.currentScreen);
					minecraft.displayGuiScreen(configScreen);
				}
			}
			return true;
		} else if (clearButton.mousePressed(minecraft, mouseX, mouseY)) {
			clearButton.playPressSound(minecraft.getSoundHandler());
			parent.getIngredientBookmarks().clear();
			updateLayout();
			return true;
		}
		return false;
	}

	private boolean handleMouseClickedSearch(int mouseX, int mouseY, int mouseButton) {
		boolean searchClicked = searchField.isMouseOver(mouseX, mouseY);
		setKeyboardFocus(searchClicked);
		if (searchClicked && searchField.handleMouseClicked(mouseX, mouseY, mouseButton)) {
			updateLayout();
		}
		return searchClicked;
	}

	@Override
	public boolean hasKeyboardFocus() {
		return searchField.isFocused();
	}

	@Override
	public void setKeyboardFocus(boolean keyboardFocus) {
		searchField.setFocused(keyboardFocus);
	}

	@Override
	public boolean onKeyPressed(char typedChar, int keyCode) {
		if (hasKeyboardFocus()) {
			boolean handled = searchField.textboxKeyTyped(typedChar, keyCode);
			if (handled) {
				boolean changed = Config.setFilterText(searchField.getText());
				if (changed) {
					firstItemIndex = 0;
					updateLayout();
				}
			}
			return handled;
		}
		return false;
	}

	// Finds the right edge of the inventory screen
	private static int getItemButtonXSpace(GuiProperties guiProperties) {
		return guiProperties.getScreenWidth() - (guiProperties.getGuiLeft() + guiProperties.getGuiXSize() + (2 * borderPadding));
	}

	private static int getItemButtonYSpace(GuiProperties guiProperties) {
		if (isSearchBarCentered(guiProperties)) {
			return guiProperties.getScreenHeight() - (buttonSize + (3 * borderPadding));
		}
		// finds the height of the list screen area minus the search and the buttons
		return guiProperties.getScreenHeight() - (buttonSize + searchHeight + 2 + (4 * borderPadding));
	}

	public static int getColumns(GuiProperties guiProperties) {
		return getItemButtonXSpace(guiProperties) / itemStackWidth;
	}

	public static int getRows(GuiProperties guiProperties) {
		return getItemButtonYSpace(guiProperties) / itemStackHeight;
	}

	private int getPageCount() {
		final int itemCount = parent.getItemFilter().size();
		final int stacksPerPage = guiIngredientList.size();
		if (stacksPerPage == 0) {
			return 1;
		}
		int pageCount = MathUtil.divideCeil(itemCount, stacksPerPage);
		pageCount = Math.max(1, pageCount);
		return pageCount;
	}

	private int getPageNum() {
		final int stacksPerPage = guiIngredientList.size();
		if (stacksPerPage == 0) {
			return 1;
		}
		return firstItemIndex / stacksPerPage;
	}

	public void close() {
		setKeyboardFocus(false);
		Config.saveFilterText();
	}

	@Nullable
	public ItemStack getStackUnderMouse() {
		if (hovered != null) {
			Object ingredient = hovered.getIngredient();
			if (ingredient instanceof ItemStack) {
				return (ItemStack) ingredient;
			}
		}
		return null;
	}

	public void setFilterText(String filterText) {
		searchField.setText(filterText);
		setToFirstPage();
		updateLayout();
	}

	public static void setToFirstPage() {
		firstItemIndex = 0;
	}

	public ImmutableList<ItemStack> getVisibleStacks() {
		ImmutableList.Builder<ItemStack> visibleStacks = ImmutableList.builder();
		for (GuiIngredientFast guiItemStack : guiIngredientList.getAllGuiIngredients()) {
			Object ingredient = guiItemStack.getIngredient();
			if (ingredient instanceof ItemStack) {
				ItemStack itemStack = (ItemStack) ingredient;
				visibleStacks.add(itemStack);
			}
		}
		for (GuiIngredientFast guiItemStack : guiBookmarks.getAllGuiIngredients()) {
			Object ingredient = guiItemStack.getIngredient();
			if (ingredient instanceof ItemStack) {
				ItemStack itemStack = (ItemStack) ingredient;
				visibleStacks.add(itemStack);
			}
		}
		return visibleStacks.build();
	}

}
