package mezz.jei.gui;

import mezz.jei.JustEnoughItems;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class GuiContainerOverlay {

	static final int iconPadding = 2;
	static final int iconSize = 16;
	private static final RenderItem itemRender = new RenderItem();

	protected ArrayList<GuiItemIcon> items = new ArrayList<GuiItemIcon>();

	protected GuiButton nextButton;
	protected GuiButton backButton;
	protected static int pageNum = 1;

	protected int guiLeft;
	protected int xSize;
	protected int width;
	protected int height;

	public GuiContainerOverlay(int guiLeft, int xSize, int width, int height) {
		this.guiLeft = guiLeft;
		this.xSize = xSize;
		this.width = width;
		this.height = height;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	@SuppressWarnings("unchecked")
	public void initGui(List buttonList) {
		final int buttonWidth = 50;
		final int buttonHeight = 20;
		String next = StatCollector.translateToLocal("jei.button.next");
		String back = StatCollector.translateToLocal("jei.button.back");
		buttonList.add(nextButton = new GuiButton(-1, this.width - buttonWidth - 4, 0, buttonWidth, buttonHeight, next));
		buttonList.add(backButton = new GuiButton(-2, this.guiLeft + this.xSize + 4, 0, buttonWidth, buttonHeight, back));

		int pageCount = getPageCount();
		if (pageNum > pageCount)
			setPageNum(pageCount);

		updatePage();
	}

	private void updatePage() {
		items.clear();

		final int xStart = guiLeft + xSize + 4;
		final int yStart = backButton.height + 4;

		int x = xStart;
		int y = yStart;
		int maxX = 0;

		for (int i = (pageNum - 1) * getCountPerPage(); i < JustEnoughItems.itemRegistry.itemList.size() && y + iconSize <= height; i++) {
			if (x > maxX)
				maxX = x;

			ItemStack stack = JustEnoughItems.itemRegistry.itemList.get(i);
			items.add(new GuiItemIcon(stack, x, y));

			x += iconSize + iconPadding;
			if (x + iconSize > width) {
				x = xStart;
				y += iconSize + iconPadding;
			}
		}

		nextButton.xPosition = maxX + iconSize - nextButton.width;
	}

	public void actionPerformed(GuiButton button) {
		if (button.id == -1) {
			if (pageNum == getPageCount())
				setPageNum(1);
			else
				setPageNum(pageNum + 1);
		} else if (button.id == -2) {
			if (pageNum == 1)
				setPageNum(getPageCount());
			else
				setPageNum(pageNum - 1);
		}
	}

	public void mouseClicked(int xPos, int yPos, int mouseButton) {
		for (GuiItemIcon itemIcon : items) {
			if (itemIcon.isMouseOver(xPos, yPos)) {
				itemIcon.mouseClicked(xPos, yPos, mouseButton);
				return;
			}
		}
	}

	public void drawScreen(TextureManager textureManager, FontRenderer fontRendererObj) {
		RenderHelper.enableGUIStandardItemLighting();

		for (GuiItemIcon itemIcon : items)
			itemIcon.draw(itemRender, fontRendererObj, textureManager);

		RenderHelper.disableStandardItemLighting();

		drawPageNumbers(fontRendererObj);
	}

	private void drawPageNumbers(FontRenderer fontRendererObj) {
		String pageDisplay = getPageNum() + " / " + getPageCount();
		int pageDisplayWidth = fontRendererObj.getStringWidth(pageDisplay);

		int pageDisplayX = ((backButton.xPosition + backButton.width) + nextButton.xPosition) / 2;
		int pageDisplayY = backButton.yPosition + 6;

		fontRendererObj.drawString(pageDisplay, pageDisplayX - (pageDisplayWidth / 2), pageDisplayY, Color.white.getRGB());
	}

	private int getCountPerPage() {
		int xArea = width - (guiLeft + xSize + 4);
		int yArea = height - (backButton.height + 4);

		int xCount = xArea / (iconSize + iconPadding);
		int yCount = yArea / (iconSize + iconPadding);

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
		updatePage();
	}

}
