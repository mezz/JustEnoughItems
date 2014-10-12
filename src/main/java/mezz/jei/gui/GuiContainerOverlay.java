package mezz.jei.gui;

import mezz.jei.JustEnoughItems;
import mezz.jei.util.Log;
import mezz.jei.util.Render;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

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

	private boolean clickHandled = false;

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

		nextButton = new GuiButton(0, this.width - nextButtonWidth - borderPadding, 0, nextButtonWidth, buttonHeight, next);
		backButton = new GuiButton(1, this.guiLeft + this.xSize + borderPadding, 0, backButtonWidth, buttonHeight, back);

		createItemButtons();
		updateItemButtons();

		int pageCount = getPageCount();
		if (pageNum > pageCount)
			pageNum = pageCount;
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
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		RenderHelper.enableGUIStandardItemLighting();

		drawPageNumbers(minecraft.fontRenderer);
		drawButtons(minecraft, mouseX, mouseY);

		RenderHelper.disableStandardItemLighting();
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
	}

	private void drawPageNumbers(FontRenderer fontRendererObj) {
		String pageDisplay = getPageNum() + "/" + getPageCount();
		int pageDisplayWidth = fontRendererObj.getStringWidth(pageDisplay);

		int pageDisplayX = ((backButton.xPosition + backButton.width) + nextButton.xPosition) / 2;
		int pageDisplayY = backButton.yPosition + Math.round((backButton.height - fontRendererObj.FONT_HEIGHT) / 2.0f);

		fontRendererObj.drawString(pageDisplay, pageDisplayX - (pageDisplayWidth / 2), pageDisplayY, Color.white.getRGB(), true);
	}

	private void drawButtons(Minecraft minecraft, int mouseX, int mouseY) {

		nextButton.drawButton(minecraft, mouseX, mouseY);
		backButton.drawButton(minecraft, mouseX, mouseY);

		GuiItemButton hoveredItemButton = null;
		for (GuiItemButton guiItemButton : itemButtons) {
			guiItemButton.drawButton(minecraft, mouseX, mouseY);

			if (hoveredItemButton == null && guiItemButton.mousePressed(minecraft, mouseX, mouseY))
				hoveredItemButton = guiItemButton;
		}

		if (hoveredItemButton != null)
			Render.renderToolTip(hoveredItemButton.getItemStack(), mouseX, mouseY);
	}

	public void handleInput(Minecraft minecraft) {
		if (Mouse.getEventButtonState()) {
			if (!clickHandled) {
				int mouseX = Mouse.getEventX() * width / minecraft.displayWidth;
				int mouseY = this.height - Mouse.getEventY() * height / minecraft.displayHeight - 1;
				handleMouseClick(minecraft, Mouse.getEventButton(), mouseX, mouseY);
				clickHandled = true;
			}
		} else {
			clickHandled = false;
		}
	}

	private void handleMouseClick(Minecraft minecraft, int mouseButton, int mouseX, int mouseY) {
		if (nextButton.mousePressed(minecraft, mouseX, mouseY)) {
			nextPage();
		} else if (backButton.mousePressed(minecraft, mouseX, mouseY)) {
			backPage();
		} else {
			for (GuiItemButton guiItemButton : itemButtons) {
				if (guiItemButton.mousePressed(minecraft, mouseX, mouseY)) {
					guiItemButton.handleMouseClick(mouseButton);
				}
			}
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
