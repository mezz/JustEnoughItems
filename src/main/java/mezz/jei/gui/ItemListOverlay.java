package mezz.jei.gui;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.util.ArrayList;

import mezz.jei.util.ReflectionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.HoverChecker;

import org.lwjgl.input.Keyboard;

import mezz.jei.ItemFilter;
import mezz.jei.api.JEIManager;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.config.Constants;
import mezz.jei.config.JEIModConfigGui;
import mezz.jei.gui.ingredients.GuiIngredient;
import mezz.jei.gui.ingredients.GuiItemStackGroup;
import mezz.jei.input.IKeyable;
import mezz.jei.input.IMouseHandler;
import mezz.jei.input.IShowsRecipeFocuses;
import mezz.jei.util.ItemStackElement;
import mezz.jei.util.MathUtil;
import mezz.jei.util.Translator;

public class ItemListOverlay implements IShowsRecipeFocuses, IMouseHandler, IKeyable {

	private static final int borderPadding = 4;
	private static final int searchHeight = 16;
	private static final int buttonPaddingX = 14;
	private static final int buttonPaddingY = 8;

	private static final int itemStackPadding = 1;
	private static final int itemStackWidth = GuiItemStackGroup.getWidth(itemStackPadding);
	private static final int itemStackHeight = GuiItemStackGroup.getHeight(itemStackPadding);
	private static final int maxSearchLength = 32;
	private static int pageNum = 0;

	private final ItemFilter itemFilter;

	private int buttonHeight;
	private final ArrayList<GuiIngredient<ItemStack>> guiItemStacks = new ArrayList<>();
	private GuiButton nextButton;
	private GuiButton backButton;
	private GuiButton configButton;
	private IDrawable configButtonIcon;
	private HoverChecker configButtonHoverChecker;
	private GuiTextField searchField;
	private int pageCount;

	private String pageNumDisplayString;
	private int pageNumDisplayX;
	private int pageNumDisplayY;

	private GuiIngredient<ItemStack> hovered = null;

	// properties of the gui we're beside
	private int guiLeft;
	private int guiXSize;
	private int screenWidth;
	private int screenHeight;

	private boolean open = false;
	private boolean enabled = true;

	public ItemListOverlay(ItemFilter itemFilter) {
		this.itemFilter = itemFilter;
	}

	public void initGui(@Nonnull GuiContainer guiContainer) {
		this.guiLeft = ReflectionUtil.getInt(GuiContainer.class.getName(), "guiLeft", guiContainer);
		this.guiXSize = ReflectionUtil.getInt(GuiContainer.class.getName(), "xSize", guiContainer);
		this.screenWidth = guiContainer.width;
		this.screenHeight = guiContainer.height;

		String next = ">";
		String back = "<";

		FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
		final int nextButtonWidth = buttonPaddingX + fontRenderer.getStringWidth(next);
		final int backButtonWidth = buttonPaddingX + fontRenderer.getStringWidth(back);
		buttonHeight = buttonPaddingY + fontRenderer.FONT_HEIGHT;

		final int columns = getColumns();
		final int rows = getRows();
		final int xSize = columns * itemStackWidth;
		final int xEmptySpace = screenWidth - guiLeft - guiXSize - xSize;

		final int leftEdge = guiLeft + guiXSize + (xEmptySpace / 2);
		final int rightEdge = leftEdge + xSize;

		final int yItemButtonSpace = getItemButtonYSpace();
		final int itemButtonsHeight = rows * itemStackHeight;

		final int buttonStartY = buttonHeight + (2 * borderPadding) + (yItemButtonSpace - itemButtonsHeight) / 2;
		createItemButtons(leftEdge, buttonStartY, columns, rows);

		nextButton = new GuiButtonExt(0, rightEdge - nextButtonWidth, borderPadding, nextButtonWidth, buttonHeight, next);
		backButton = new GuiButtonExt(1, leftEdge, borderPadding, backButtonWidth, buttonHeight, back);

		int configButtonSize = searchHeight + 4;
		int configButtonX = rightEdge - configButtonSize + 1;
		int configButtonY = screenHeight - configButtonSize - borderPadding;
		configButton = new GuiButtonExt(2, configButtonX, configButtonY, configButtonSize, configButtonSize, null);
		ResourceLocation configButtonIconLocation = new ResourceLocation(Constants.RESOURCE_DOMAIN, Constants.TEXTURE_GUI_PATH + "recipeBackground.png");
		configButtonIcon = JEIManager.guiHelper.createDrawable(configButtonIconLocation, 0, 166, 16, 16);
		configButtonHoverChecker = new HoverChecker(configButton, 0);

		int searchFieldY = screenHeight - searchHeight - borderPadding - 2;
		int searchFieldWidth = rightEdge - leftEdge - configButtonSize - 1;
		searchField = new GuiTextField(0, fontRenderer, leftEdge, searchFieldY, searchFieldWidth, searchHeight);
		searchField.setMaxStringLength(maxSearchLength);
		setKeyboardFocus(false);
		searchField.setText(itemFilter.getFilterText());

		updateLayout();
	}

	private void createItemButtons(final int xStart, final int yStart, final int columnCount, final int rowCount) {
		guiItemStacks.clear();

		for (int row = 0; row < rowCount; row++) {
			int y = yStart + (row * itemStackHeight);
			for (int column = 0; column < columnCount; column++) {
				int x = xStart + (column * itemStackWidth);
				guiItemStacks.add(GuiItemStackGroup.createGuiItemStack(false, x, y, itemStackPadding));
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
		for (GuiIngredient<ItemStack> itemButton : guiItemStacks) {
			if (i >= itemList.size()) {
				itemButton.clear();
			} else {
				ItemStack stack = itemList.get(i).getItemStack();
				itemButton.set(stack, new Focus());
			}
			i++;
		}

		FontRenderer fontRendererObj = Minecraft.getMinecraft().fontRendererObj;

		pageNumDisplayString = (getPageNum() + 1) + "/" + getPageCount();
		int pageDisplayWidth = fontRendererObj.getStringWidth(pageNumDisplayString);
		pageNumDisplayX = ((backButton.xPosition + backButton.width) + nextButton.xPosition) / 2 - (pageDisplayWidth / 2);
		pageNumDisplayY = backButton.yPosition + Math.round((backButton.height - fontRendererObj.FONT_HEIGHT) / 2.0f);

		if (itemList.size() == 0) {
			searchField.setTextColor(Color.red.getRGB());
			searchField.setMaxStringLength(searchField.getText().length());
		} else {
			searchField.setTextColor(Color.white.getRGB());
			searchField.setMaxStringLength(maxSearchLength);
		}
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

		for (GuiIngredient<ItemStack> guiItemStack : guiItemStacks) {
			if (hovered == null && guiItemStack.isMouseOver(mouseX, mouseY)) {
				hovered = guiItemStack;
			} else {
				guiItemStack.draw(minecraft);
			}
		}

		if (configButtonHoverChecker.checkHover(mouseX, mouseY)) {
			String configString = Translator.translateToLocal("jei.tooltip.config");
			TooltipRenderer.drawHoveringText(minecraft, configString, mouseX, mouseY);
		}
	}

	public void drawHovered(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		if (hovered != null) {
			hovered.drawHovered(minecraft, mouseX, mouseY);
			hovered = null;
		}
	}

	public void handleTick() {
		searchField.updateCursorCounter();
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
		for (GuiIngredient<ItemStack> guiItemStack : guiItemStacks) {
			if (guiItemStack.isMouseOver(mouseX, mouseY)) {
				setKeyboardFocus(false);
				return new Focus(guiItemStack.get());
			}
		}
		return null;
	}

	@Override
	public boolean handleMouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (!isMouseOver(mouseX, mouseY)) {
			setKeyboardFocus(false);
			return false;
		}
		boolean buttonClicked = handleMouseClickedButtons(mouseX, mouseY, mouseButton);
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

	private boolean handleMouseClickedButtons(int mouseX, int mouseY, int mouseButton) {
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
		boolean searchClicked = mouseX >= searchField.xPosition && mouseX < searchField.xPosition + searchField.width && mouseY >= searchField.yPosition && mouseY < searchField.yPosition + searchField.height;
		setKeyboardFocus(searchClicked);
		if (searchClicked) {
			if (mouseButton == 1) {
				searchField.setText("");
				if (itemFilter.setFilterText(searchField.getText())) {
					updateLayout();
				}
			} else {
				searchField.mouseClicked(mouseX, mouseY, mouseButton);
			}
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
		Keyboard.enableRepeatEvents(keyboardFocus);
	}

	@Override
	public boolean onKeyPressed(int keyCode) {
		if (searchField.isFocused()) {
			boolean success = searchField.textboxKeyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey());
			if (success && itemFilter.setFilterText(searchField.getText())) {
				updateLayout();
			}
			return true;
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
		return open && enabled;
	}

	public void toggleEnabled() {
		enabled = !enabled;
	}
}
