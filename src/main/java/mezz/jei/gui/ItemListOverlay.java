package mezz.jei.gui;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import org.lwjgl.input.Keyboard;

import mezz.jei.ItemFilter;
import mezz.jei.input.IClickable;
import mezz.jei.input.IKeyable;
import mezz.jei.input.IShowsItemStacks;
import mezz.jei.util.ItemStackElement;
import mezz.jei.util.MathUtil;

public class ItemListOverlay implements IShowsItemStacks, IClickable, IKeyable {

	private static final int borderPadding = 1;
	private static final int searchHeight = 16;
	private static final int itemStackPadding = 1;
	private static final int itemStackWidth = GuiItemStack.getWidth(itemStackPadding);
	private static final int itemStackHeight = GuiItemStack.getHeight(itemStackPadding);
	private static final int maxSearchLength = 32;
	private static int pageNum = 0;

	private final ItemFilter itemFilter;

	private int buttonHeight;
	private final ArrayList<GuiItemStack> guiItemStacks = new ArrayList<GuiItemStack>();
	private GuiButton nextButton;
	private GuiButton backButton;
	private GuiTextField searchField;
	private int pageCount;

	private String pageNumDisplayString;
	private int pageNumDisplayX;
	private int pageNumDisplayY;

	private GuiItemStack hovered = null;

	// properties of the gui we're beside
	private int guiLeft;
	private int xSize;
	private int width;
	private int height;

	private boolean isOpen = false;

	public ItemListOverlay(ItemFilter itemFilter) {
		this.itemFilter = itemFilter;
	}

	public void initGui(@Nonnull GuiContainer guiContainer) {
		this.guiLeft = guiContainer.guiLeft;
		this.xSize = guiContainer.xSize;
		this.width = guiContainer.width;
		this.height = guiContainer.height;

		String next = StatCollector.translateToLocal("jei.button.next");
		String back = StatCollector.translateToLocal("jei.button.back");

		FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
		final int nextButtonWidth = 10 + fontRenderer.getStringWidth(next);
		final int backButtonWidth = 10 + fontRenderer.getStringWidth(back);
		buttonHeight = 5 + fontRenderer.FONT_HEIGHT;

		int rightEdge = createItemButtons();

		int leftEdge = this.guiLeft + this.xSize + borderPadding;

		nextButton = new GuiButton(0, rightEdge - nextButtonWidth, 0, nextButtonWidth, buttonHeight, next);
		backButton = new GuiButton(1, leftEdge, 0, backButtonWidth, buttonHeight, back);

		searchField = new GuiTextField(0, fontRenderer, leftEdge, this.height - searchHeight - (2 * borderPadding), rightEdge - leftEdge, searchHeight);
		searchField.setMaxStringLength(maxSearchLength);
		searchField.setFocused(false);
		searchField.setText(itemFilter.getFilterText());

		updateLayout();
	}

	// creates buttons and returns the x value of the right edge of the rightmost button
	private int createItemButtons() {
		guiItemStacks.clear();

		final int xStart = guiLeft + xSize + borderPadding;
		final int yStart = buttonHeight + (2 * borderPadding);

		int x = xStart;
		int y = yStart;
		int maxX = 0;

		while (y + itemStackHeight + borderPadding <= height - searchHeight) {
			if (x > maxX) {
				maxX = x;
			}

			guiItemStacks.add(new GuiItemStack(x, y, itemStackPadding));

			x += itemStackWidth;
			if (x + itemStackWidth + borderPadding > width) {
				x = xStart;
				y += itemStackHeight;
			}
		}

		return maxX + itemStackWidth;
	}

	private void updateLayout() {
		updatePageCount();
		if (pageNum >= getPageCount()) {
			pageNum = 0;
		}
		int i = pageNum * getCountPerPage();

		ImmutableList<ItemStackElement> itemList = itemFilter.getItemList();
		for (GuiItemStack itemButton : guiItemStacks) {
			if (i >= itemList.size()) {
				itemButton.clearItemStacks();
			} else {
				ItemStack stack = itemList.get(i).getItemStack();
				itemButton.setItemStack(stack, null);
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

	private void backPage() {
		if (pageNum == 0) {
			setPageNum(getPageCount() - 1);
		} else {
			setPageNum(pageNum - 1);
		}
	}

	public void drawScreen(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		if (!isOpen) {
			return;
		}

		minecraft.fontRendererObj.drawString(pageNumDisplayString, pageNumDisplayX, pageNumDisplayY, Color.white.getRGB(), true);
		searchField.drawTextBox();

		nextButton.drawButton(minecraft, mouseX, mouseY);
		backButton.drawButton(minecraft, mouseX, mouseY);

		for (GuiItemStack guiItemStack : guiItemStacks) {
			if (hovered == null && guiItemStack.isMouseOver(mouseX, mouseY)) {
				hovered = guiItemStack;
			} else {
				guiItemStack.draw(minecraft);
			}
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
	@Nullable
	public ItemStack getStackUnderMouse(int mouseX, int mouseY) {
		if (!isOpen) {
			return null;
		}
		for (GuiItemStack guiItemStack : guiItemStacks) {
			if (guiItemStack.isMouseOver(mouseX, mouseY)) {
				return guiItemStack.getItemStack();
			}
		}
		return null;
	}

	@Override
	public boolean handleMouseClicked(int mouseX, int mouseY, int mouseButton) {
		Minecraft minecraft = Minecraft.getMinecraft();
		if (nextButton.mousePressed(minecraft, mouseX, mouseY)) {
			nextPage();
			return true;
		} else if (backButton.mousePressed(minecraft, mouseX, mouseY)) {
			backPage();
			return true;
		}

		boolean searchClicked = mouseX >= searchField.xPosition && mouseX < searchField.xPosition + searchField.width && mouseY >= searchField.yPosition && mouseY < searchField.yPosition + searchField.height;
		searchField.mouseClicked(mouseX, mouseY, mouseButton);
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
	public boolean onKeyPressed(int keyCode) {
		boolean success = searchField.textboxKeyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey());
		if (success && itemFilter.setFilterText(searchField.getText())) {
			updateLayout();
			return true;
		}
		return false;
	}

	private int getCountPerPage() {
		int xArea = width - (guiLeft + xSize + (2 * borderPadding));
		int yArea = height - (buttonHeight + (2 * borderPadding));

		int xCount = xArea / itemStackWidth;
		int yCount = yArea / itemStackHeight;

		return xCount * yCount;
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
		isOpen = true;
		searchField.setFocused(false);
	}

	@Override
	public void close() {
		isOpen = false;
		searchField.setFocused(false);
	}

	@Override
	public boolean isOpen() {
		return isOpen;
	}

}
