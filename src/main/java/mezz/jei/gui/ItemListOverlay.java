package mezz.jei.gui;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.HoverChecker;

import org.lwjgl.input.Keyboard;

import mezz.jei.Internal;
import mezz.jei.ItemFilter;
import mezz.jei.JustEnoughItems;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.config.Config;
import mezz.jei.config.Constants;
import mezz.jei.config.JEIModConfigGui;
import mezz.jei.gui.ingredients.GuiItemStackFast;
import mezz.jei.gui.ingredients.GuiItemStackFastList;
import mezz.jei.gui.ingredients.GuiItemStackGroup;
import mezz.jei.input.GuiTextFieldFilter;
import mezz.jei.input.IKeyable;
import mezz.jei.input.IMouseHandler;
import mezz.jei.input.IShowsRecipeFocuses;
import mezz.jei.network.packets.PacketDeletePlayerItem;
import mezz.jei.network.packets.PacketJEI;
import mezz.jei.util.ItemStackElement;
import mezz.jei.util.MathUtil;
import mezz.jei.util.Translator;

public class ItemListOverlay implements IShowsRecipeFocuses, IMouseHandler, IKeyable {

	private static final int borderPadding = 4;
	private static final int searchHeight = 16;
	private static final int buttonPaddingX = 14;
	private static final int buttonPaddingY = 8;
	private static final String nextLabel = ">";
	private static final String backLabel = "<";

	private static final int itemStackPadding = 1;
	private static final int itemStackWidth = GuiItemStackGroup.getWidth(itemStackPadding);
	private static final int itemStackHeight = GuiItemStackGroup.getHeight(itemStackPadding);
	private static int pageNum = 0;

	private final ItemFilter itemFilter;

	private int buttonHeight;
	private final GuiItemStackFastList guiItemStacks = new GuiItemStackFastList();
	private GuiButton nextButton;
	private GuiButton backButton;
	private GuiButton configButton;
	private IDrawable configButtonIcon;
	private HoverChecker configButtonHoverChecker;
	private GuiTextFieldFilter searchField;
	private int pageCount;

	private String pageNumDisplayString;
	private int pageNumDisplayX;
	private int pageNumDisplayY;

	private GuiItemStackFast hovered = null;

	// properties of the gui we're beside
	private int guiLeft;
	private int guiXSize;
	private int screenWidth;
	private int screenHeight;

	private boolean open = false;

	public ItemListOverlay(ItemFilter itemFilter) {
		this.itemFilter = itemFilter;
	}

	public void initGui(@Nonnull GuiContainer guiContainer) {
		this.guiLeft = guiContainer.guiLeft;
		this.guiXSize = guiContainer.xSize;
		this.screenWidth = guiContainer.width;
		this.screenHeight = guiContainer.height;

		final int columns = getColumns();
		if (columns < 4) {
			close();
			return;
		}

		FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
		final int nextButtonWidth = buttonPaddingX + fontRenderer.getStringWidth(nextLabel);
		final int backButtonWidth = buttonPaddingX + fontRenderer.getStringWidth(backLabel);
		buttonHeight = buttonPaddingY + fontRenderer.FONT_HEIGHT;

		final int rows = getRows();
		final int xSize = columns * itemStackWidth;
		final int xEmptySpace = screenWidth - guiLeft - guiXSize - xSize;

		final int leftEdge = guiLeft + guiXSize + (xEmptySpace / 2);
		final int rightEdge = leftEdge + xSize;

		final int yItemButtonSpace = getItemButtonYSpace();
		final int itemButtonsHeight = rows * itemStackHeight;

		final int buttonStartY = buttonHeight + (2 * borderPadding) + (yItemButtonSpace - itemButtonsHeight) / 2;
		createItemButtons(leftEdge, buttonStartY, columns, rows);

		nextButton = new GuiButtonExt(0, rightEdge - nextButtonWidth, borderPadding, nextButtonWidth, buttonHeight, nextLabel);
		backButton = new GuiButtonExt(1, leftEdge, borderPadding, backButtonWidth, buttonHeight, backLabel);

		int configButtonSize = searchHeight + 4;
		int configButtonX = rightEdge - configButtonSize + 1;
		int configButtonY = screenHeight - configButtonSize - borderPadding;
		configButton = new GuiButtonExt(2, configButtonX, configButtonY, configButtonSize, configButtonSize, null);
		ResourceLocation configButtonIconLocation = new ResourceLocation(Constants.RESOURCE_DOMAIN, Constants.TEXTURE_GUI_PATH + "recipeBackground.png");
		configButtonIcon = Internal.getHelpers().getGuiHelper().createDrawable(configButtonIconLocation, 0, 166, 16, 16);
		configButtonHoverChecker = new HoverChecker(configButton, 0);

		int searchFieldY = screenHeight - searchHeight - borderPadding - 2;
		int searchFieldWidth = rightEdge - leftEdge - configButtonSize - 1;
		searchField = new GuiTextFieldFilter(0, fontRenderer, leftEdge, searchFieldY, searchFieldWidth, searchHeight);
		setKeyboardFocus(false);
		searchField.setItemFilter(itemFilter);

		updateLayout();

		open();
	}

	public void updateGui(@Nonnull GuiContainer guiContainer) {
		if (this.guiLeft != guiContainer.guiLeft || this.guiXSize != guiContainer.xSize || this.screenWidth != guiContainer.width || this.screenHeight != guiContainer.height) {
			initGui(guiContainer);
		}
	}

	private void createItemButtons(final int xStart, final int yStart, final int columnCount, final int rowCount) {
		guiItemStacks.clear();

		for (int row = 0; row < rowCount; row++) {
			int y = yStart + (row * itemStackHeight);
			for (int column = 0; column < columnCount; column++) {
				int x = xStart + (column * itemStackWidth);
				guiItemStacks.add(new GuiItemStackFast(x, y, itemStackPadding));
			}
		}
	}

	private void updateLayout() {
		updatePageCount();
		if (pageNum >= getPageCount()) {
			pageNum = 0;
		}
		int i = pageNum * getCountPerPage();

		ImmutableList<ItemStackElement> itemList = itemFilter.getItemList();
		guiItemStacks.set(i, itemList);

		FontRenderer fontRendererObj = Minecraft.getMinecraft().fontRendererObj;

		pageNumDisplayString = (getPageNum() + 1) + "/" + getPageCount();
		int pageDisplayWidth = fontRendererObj.getStringWidth(pageNumDisplayString);
		pageNumDisplayX = ((backButton.xPosition + backButton.width) + nextButton.xPosition) / 2 - (pageDisplayWidth / 2);
		pageNumDisplayY = backButton.yPosition + Math.round((backButton.height - fontRendererObj.FONT_HEIGHT) / 2.0f);

		searchField.update();
	}

	private void nextPage() {
		if (pageNum == getPageCount() - 1) {
			setPageNum(0);
		} else {
			setPageNum(pageNum + 1);
		}
	}

	private void previousPage() {
		if (pageNum == 0) {
			setPageNum(getPageCount() - 1);
		} else {
			setPageNum(pageNum - 1);
		}
	}

	public void drawScreen(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		if (!isOpen()) {
			return;
		}

		GlStateManager.disableLighting();
		
		minecraft.fontRendererObj.drawString(pageNumDisplayString, pageNumDisplayX, pageNumDisplayY, Color.white.getRGB(), true);
		searchField.drawTextBox();

		nextButton.drawButton(minecraft, mouseX, mouseY);
		backButton.drawButton(minecraft, mouseX, mouseY);
		configButton.drawButton(minecraft, mouseX, mouseY);
		configButtonIcon.draw(minecraft, configButton.xPosition + 2, configButton.yPosition + 2);
		GlStateManager.disableBlend();

		boolean mouseOver = isMouseOver(mouseX, mouseY);

		if (mouseOver && shouldShowDeleteItemTooltip(minecraft)) {
			hovered = guiItemStacks.render(null, minecraft, false, mouseX, mouseY);
		} else {
			hovered = guiItemStacks.render(hovered, minecraft, mouseOver, mouseX, mouseY);
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

	public void drawHovered(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		if (!isOpen()) {
			return;
		}
		
		boolean mouseOver = isMouseOver(mouseX, mouseY);
		if (mouseOver && shouldShowDeleteItemTooltip(minecraft)) {
			String deleteItem = Translator.translateToLocal("jei.tooltip.delete.item");
			TooltipRenderer.drawHoveringText(minecraft, deleteItem, mouseX, mouseY);
		}

		if (hovered != null) {
			RenderHelper.enableGUIStandardItemLighting();
			hovered.drawHovered(minecraft, mouseX, mouseY);
			RenderHelper.disableStandardItemLighting();

			hovered = null;
		}

		if (configButtonHoverChecker.checkHover(mouseX, mouseY)) {
			String configString = Translator.translateToLocal("jei.tooltip.config");
			TooltipRenderer.drawHoveringText(minecraft, configString, mouseX, mouseY);
		}
	}

	public void handleTick() {
		if (searchField != null) {
			searchField.updateCursorCounter();
		}
	}

	@Override
	public boolean isMouseOver(int mouseX, int mouseY) {
		return isOpen() && (mouseX >= guiLeft + guiXSize);
	}

	@Override
	@Nullable
	public Focus getFocusUnderMouse(int mouseX, int mouseY) {
		if (!isMouseOver(mouseX, mouseY)) {
			return null;
		}

		Focus focus = guiItemStacks.getFocusUnderMouse(mouseX, mouseY);
		if (focus != null) {
			setKeyboardFocus(false);
		}
		return focus;
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
				PacketJEI packet = new PacketDeletePlayerItem(itemStack);
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
			return true;
		} else if (backButton.mousePressed(minecraft, mouseX, mouseY)) {
			previousPage();
			return true;
		} else if (configButton.mousePressed(minecraft, mouseX, mouseY)) {
			close();
			GuiScreen configScreen = new JEIModConfigGui(minecraft.currentScreen);
			minecraft.displayGuiScreen(configScreen);
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
		return searchField != null && searchField.isFocused();
	}

	@Override
	public void setKeyboardFocus(boolean keyboardFocus) {
		if (searchField != null) {
			searchField.setFocused(keyboardFocus);
		}
	}

	@Override
	public boolean onKeyPressed(int keyCode) {
		if (hasKeyboardFocus()) {
			char character = Keyboard.getEventCharacter();
			boolean changed = searchField.textboxKeyTyped(character, Keyboard.getEventKey());
			if (changed) {
				updateLayout();
			}
			return changed || ChatAllowedCharacters.isAllowedCharacter(character);
		}
		return false;
	}

	private int getItemButtonXSpace() {
		return screenWidth - (guiLeft + guiXSize + (2 * borderPadding));
	}

	private int getItemButtonYSpace() {
		return screenHeight - (buttonHeight + searchHeight + 2 + (4 * borderPadding));
	}

	private int getColumns() {
		return getItemButtonXSpace() / itemStackWidth;
	}

	private int getRows() {
		return getItemButtonYSpace() / itemStackHeight;
	}

	private int getCountPerPage() {
		return getColumns() * getRows();
	}

	private void updatePageCount() {
		int count = itemFilter.size();
		pageCount = MathUtil.divideCeil(count, getCountPerPage());
		if (pageCount == 0) {
			pageCount = 1;
		}
	}

	private int getPageCount() {
		return pageCount;
	}

	private int getPageNum() {
		return pageNum;
	}

	private void setPageNum(int pageNum) {
		if (ItemListOverlay.pageNum == pageNum) {
			return;
		}
		ItemListOverlay.pageNum = pageNum;
		updateLayout();
	}

	@Override
	public void open() {
		open = true;
		setKeyboardFocus(false);
	}

	@Override
	public void close() {
		open = false;
		setKeyboardFocus(false);
	}

	@Override
	public boolean isOpen() {
		return open && Config.isOverlayEnabled();
	}
}
