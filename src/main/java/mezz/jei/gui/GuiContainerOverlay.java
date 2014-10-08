package mezz.jei.gui;

import mezz.jei.JustEnoughItems;
import mezz.jei.util.Log;
import mezz.jei.util.Render;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GuiContainerOverlay {

	static final int iconPadding = 2;

	protected ArrayList<GuiItemButton> itemButtons = new ArrayList<GuiItemButton>();

	protected GuiButton nextButton;
	protected GuiButton backButton;

	protected static int pageNum = 1;

	public int guiLeft;
	public int guiTop;
	public int xSize;
	public int ySize;
	public int width;
	public int height;

	private boolean alreadyClicked = false;

	@SuppressWarnings("unchecked")
	public void initGui(int guiLeft, int guiTop, int xSize, int ySize, int width, int height, List buttonList) {
		this.guiLeft = guiLeft;
		this.guiTop = guiTop;
		this.xSize = xSize;
		this.ySize = ySize;
		this.width = width;
		this.height = height;

		final int buttonWidth = 50;
		final int buttonHeight = 20;
		String next = StatCollector.translateToLocal("jei.button.next");
		String back = StatCollector.translateToLocal("jei.button.back");
		nextButton = new GuiButton(0, this.width - buttonWidth - 4, 0, buttonWidth, buttonHeight, next);
		backButton = new GuiButton(0, this.guiLeft + this.xSize + 4, 0, buttonWidth, buttonHeight, back);
		buttonList.add(nextButton);
		buttonList.add(backButton);

		createItemButtons();
		updateItemButtons();
	}

	private void createItemButtons() {
		itemButtons.clear();

		final int xStart = guiLeft + xSize + 4;
		final int yStart = backButton.height + 4;

		int x = xStart;
		int y = yStart;
		int maxX = 0;

		while (y + GuiItemButton.height <= height) {
			if (x > maxX)
				maxX = x;

			itemButtons.add(new GuiItemButton(null, x, y));

			x += GuiItemButton.width + iconPadding;
			if (x + GuiItemButton.width > width) {
				x = xStart;
				y += GuiItemButton.height + iconPadding;
			}
		}

		nextButton.xPosition = maxX + GuiItemButton.width - nextButton.width;
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
		} else {
			Log.warning("Unknown button: " + button);
			return false;
		}

		return true;
	}

	public void handleInput() {
		Minecraft mc = Minecraft.getMinecraft();
		int i = Mouse.getEventX() * width / mc.displayWidth;
		int j = this.height - Mouse.getEventY() * height / mc.displayHeight - 1;
		int k = Mouse.getEventButton();
		if (Mouse.getEventButtonState()) {
			if (!alreadyClicked) {
				if (k == 0) {
					for (GuiItemButton guiItemButton : itemButtons) {
						if (guiItemButton.mousePressed(mc, i, j))
							guiItemButton.actionPerformed();
					}
				}
			}
			alreadyClicked = true;
		} else {
			alreadyClicked = false;
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
		drawPageNumbers(minecraft.fontRenderer);
		drawItemButtons(minecraft, mouseX, mouseY);
	}

	private void drawPageNumbers(FontRenderer fontRendererObj) {
		String pageDisplay = getPageNum() + "/" + getPageCount();
		int pageDisplayWidth = fontRendererObj.getStringWidth(pageDisplay);

		int pageDisplayX = ((backButton.xPosition + backButton.width) + nextButton.xPosition) / 2;
		int pageDisplayY = backButton.yPosition + 6;

		fontRendererObj.drawString(pageDisplay, pageDisplayX - (pageDisplayWidth / 2), pageDisplayY, Color.white.getRGB());
	}

	private void drawItemButtons(Minecraft minecraft, int mouseX, int mouseY) {
		GuiItemButton hoveredButton = null;
		for (GuiItemButton guiItemButton : itemButtons) {
			guiItemButton.drawButton(minecraft, mouseX, mouseY);

			if (guiItemButton.mousePressed(minecraft, mouseX, mouseY))
				hoveredButton = guiItemButton;
		}

		if (hoveredButton != null)
			Render.renderToolTip(this, hoveredButton.getItemStack(), mouseX, mouseY);
	}

	private int getCountPerPage() {
		int xArea = width - (guiLeft + xSize + 4);
		int yArea = height - (backButton.height + 4);

		int xCount = xArea / (GuiItemButton.width + iconPadding);
		int yCount = yArea / (GuiItemButton.height + iconPadding);

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
