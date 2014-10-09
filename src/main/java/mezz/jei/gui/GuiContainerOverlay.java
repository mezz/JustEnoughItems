package mezz.jei.gui;

import mezz.jei.JustEnoughItems;
import mezz.jei.util.Log;
import mezz.jei.util.Render;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class GuiContainerOverlay {

	static final int borderPadding = 1;

	protected ArrayList<GuiItemButton> itemButtons = new ArrayList<GuiItemButton>();

	protected GuiButton nextButton;
	protected GuiButton backButton;

	protected static int pageNum = 1;

	protected int guiLeft;
	protected int guiTop;
	protected int xSize;
	protected int ySize;
	protected int width;
	protected int height;

	@SuppressWarnings("unchecked")
	public void initGui(int guiLeft, int guiTop, int xSize, int ySize, int width, int height, List buttonList) {
		this.guiLeft = guiLeft;
		this.guiTop = guiTop;
		this.xSize = xSize;
		this.ySize = ySize;
		this.width = width;
		this.height = height;

		String next = StatCollector.translateToLocal("jei.button.next");
		String back = StatCollector.translateToLocal("jei.button.back");

		FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
		final int nextButtonWidth = 10 + fontRenderer.getStringWidth(next);
		final int backButtonWidth = 10 + fontRenderer.getStringWidth(back);
		final int buttonHeight = 5 + fontRenderer.FONT_HEIGHT;

		ArrayList<Integer> buttonIDs = getUnusedButtonIDs(buttonList, 2);

		nextButton = new GuiButton(buttonIDs.get(0), this.width - nextButtonWidth - borderPadding, 0, nextButtonWidth, buttonHeight, next);
		backButton = new GuiButton(buttonIDs.get(1), this.guiLeft + this.xSize + borderPadding, 0, backButtonWidth, buttonHeight, back);
		buttonList.add(nextButton);
		buttonList.add(backButton);

		createItemButtons();
		updateItemButtons();

		buttonIDs = getUnusedButtonIDs(buttonList, itemButtons.size());
		for (int i = 0; i < itemButtons.size(); i++) {
			GuiItemButton button = itemButtons.get(i);
			button.id = buttonIDs.get(i);
			buttonList.add(button);
		}

		int pageCount = getPageCount();
		if (pageNum > pageCount)
			pageNum = pageCount;
	}

	private ArrayList<Integer> getUnusedButtonIDs(List<GuiButton> buttonList, int count) {
		HashSet<Integer> usedButtonIDs = new HashSet<Integer>();
		for (GuiButton button : buttonList)
			usedButtonIDs.add(button.id);

		ArrayList<Integer> availableButtonIDs = new ArrayList<Integer>();
		int id = 10;
		while (availableButtonIDs.size() < count) {
			if (!usedButtonIDs.contains(id))
				availableButtonIDs.add(id);
			id++;
		}
		return availableButtonIDs;
	}

	private void createItemButtons() {
		itemButtons.clear();

		final int xStart = guiLeft + xSize + borderPadding;
		final int yStart = backButton.height + (2 * borderPadding);

		int x = xStart;
		int y = yStart;
		int maxX = 0;

		while (y + GuiItemButton.height + borderPadding <= height) {
			if (x > maxX)
				maxX = x;

			itemButtons.add(new GuiItemButton(null, x, y));

			x += GuiItemButton.width;
			if (x + GuiItemButton.width + borderPadding > width) {
				x = xStart;
				y += GuiItemButton.height;
			}
		}

		nextButton.xPosition = maxX + GuiItemButton.width - nextButton.width - borderPadding;
	}

	private void updateItemButtons() {
		int i = (pageNum - 1) * getCountPerPage();

		for (GuiItemButton itemButton : itemButtons) {
			if (i >= JustEnoughItems.itemRegistry.itemList.size()) {
				itemButton.setItemStack(null);
			} else {
				ItemStack stack = JustEnoughItems.itemRegistry.itemList.get(i);
				itemButton.setItemStack(stack);
			}
			i++;
		}
	}

	public boolean actionPerformed(GuiButton button) {
		if (button.id == nextButton.id) {
			nextPage();
		} else if (button.id == backButton.id) {
			backPage();
		} else if (button instanceof GuiItemButton) {
			((GuiItemButton) button).actionPerformed();
		} else {
			Log.warning("Unknown button: " + button);
			return false;
		}

		return true;
	}

	public void nextPage() {
		if (pageNum == getPageCount())
			setPageNum(1);
		else
			setPageNum(pageNum + 1);
	}

	public void backPage() {
		if (pageNum == 1)
			setPageNum(getPageCount());
		else
			setPageNum(pageNum - 1);
	}

	public void drawScreen(Minecraft minecraft, int mouseX, int mouseY) {
		drawPageNumbers(minecraft.fontRenderer);
	}

	private void drawPageNumbers(FontRenderer fontRendererObj) {
		String pageDisplay = getPageNum() + "/" + getPageCount();
		int pageDisplayWidth = fontRendererObj.getStringWidth(pageDisplay);

		int pageDisplayX = ((backButton.xPosition + backButton.width) + nextButton.xPosition) / 2;
		int pageDisplayY = backButton.yPosition + Math.round((backButton.height - fontRendererObj.FONT_HEIGHT) / 2.0f);

		fontRendererObj.drawString(pageDisplay, pageDisplayX - (pageDisplayWidth / 2), pageDisplayY, Color.white.getRGB(), true);
	}

	public void drawTooltips(Minecraft minecraft, int mouseX, int mouseY) {
		for (GuiItemButton guiItemButton : itemButtons) {
			if (guiItemButton.mousePressed(minecraft, mouseX, mouseY))
				Render.renderToolTip(guiItemButton.getItemStack(), mouseX, mouseY);
		}
	}

	private int getCountPerPage() {
		int xArea = width - (guiLeft + xSize + (2 * borderPadding));
		int yArea = height - (backButton.height + (2 * borderPadding));

		int xCount = xArea / GuiItemButton.width;
		int yCount = yArea / GuiItemButton.height;

		return xCount * yCount;
	}

	private int getPageCount() {
		int count = JustEnoughItems.itemRegistry.itemList.size();
		return (int) Math.ceil((double) count / (double) getCountPerPage());
	}

	protected int getPageNum() {
		return pageNum;
	}

	protected void setPageNum(int pageNum) {
		if (GuiContainerOverlay.pageNum == pageNum)
			return;
		GuiContainerOverlay.pageNum = pageNum;
		updateItemButtons();
	}

}
