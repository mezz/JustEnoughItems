package mezz.jei.gui;

import mezz.jei.JustEnoughItems;
import mezz.jei.KeyBindings;
import mezz.jei.util.Render;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class GuiItemListOverlay {

	private static final int borderPadding = 1;
	private static final int searchHeight = 16;
	protected int buttonHeight;
	protected int rightEdge;
	protected int leftEdge;

	protected ArrayList<GuiItemButton> itemButtons = new ArrayList<GuiItemButton>();

	protected GuiButton nextButton;
	protected GuiButton backButton;

	protected GuiTextField searchField;
	private static final int maxSearchLength = 32;

	private static int pageNum = 1;
	protected int pageCount;

	protected String pageNumDisplayString;
	protected int pageNumDisplayX;
	protected int pageNumDisplayY;

	private boolean clickHandled = false;

	private boolean overlayEnabled = true;

	// properties of the gui we're beside
	protected int guiLeft;
	protected int guiTop;
	protected int xSize;
	protected int ySize;
	protected int width;
	protected int height;

	public void initGui(int guiLeft, int guiTop, int xSize, int ySize, int width, int height) {
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
		buttonHeight = 5 + fontRenderer.FONT_HEIGHT;

		rightEdge = createItemButtons();

		leftEdge = this.guiLeft + this.xSize + borderPadding;

		nextButton = new GuiButton(0, rightEdge - nextButtonWidth, 0, nextButtonWidth, buttonHeight, next);
		backButton = new GuiButton(1, leftEdge, 0, backButtonWidth, buttonHeight, back);

		searchField = new GuiTextField(fontRenderer, leftEdge, this.height - searchHeight - (2 * borderPadding), rightEdge - leftEdge, searchHeight);
		searchField.setMaxStringLength(maxSearchLength);
		searchField.setFocused(false);
		searchField.setText(JustEnoughItems.itemFilter.getFilterText());

		updateLayout();
	}

	// creates buttons and returns the x value of the right edge of the rightmost button
	private int createItemButtons() {
		itemButtons.clear();

		final int xStart = guiLeft + xSize + borderPadding;
		final int yStart = buttonHeight + (2 * borderPadding);

		int x = xStart;
		int y = yStart;
		int maxX = 0;

		while (y + GuiItemButton.height + borderPadding <= height - searchHeight) {
			if (x > maxX)
				maxX = x;

			itemButtons.add(new GuiItemButton(null, x, y));

			x += GuiItemButton.width;
			if (x + GuiItemButton.width + borderPadding > width) {
				x = xStart;
				y += GuiItemButton.height;
			}
		}

		return maxX + GuiItemButton.width;
	}

	private void updateLayout() {
		updatePageCount();
		if (pageNum > getPageCount())
			pageNum = 1;
		int i = (pageNum - 1) * getCountPerPage();

		List<ItemStack> itemList = JustEnoughItems.itemFilter.getItemList();
		for (GuiItemButton itemButton : itemButtons) {
			if (i >= itemList.size()) {
				itemButton.setItemStack(null);
			} else {
				ItemStack stack = itemList.get(i);
				itemButton.setItemStack(stack);
			}
			i++;
		}

		FontRenderer fontRendererObj = Minecraft.getMinecraft().fontRenderer;

		pageNumDisplayString = getPageNum() + "/" + getPageCount();
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

	public void handleMouseEvent(Minecraft minecraft, int mouseX, int mouseY) {
		if (!overlayEnabled)
			return;

		if (Mouse.getEventButton() > -1) {
			int mouseButton = Mouse.getEventButton();
			if (Mouse.getEventButtonState()) {
				if (!clickHandled) {
					handleMouseClick(minecraft, mouseButton, mouseX, mouseY);
					clickHandled = true;
				}
			} else {
				clickHandled = false;
			}
		}
	}

	public void drawScreen(Minecraft minecraft, int mouseX, int mouseY) {
		if (!overlayEnabled)
			return;

		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		RenderHelper.enableGUIStandardItemLighting();

		drawPageNumbers(minecraft.fontRenderer);
		searchField.drawTextBox();
		drawButtons(minecraft, mouseX, mouseY);

		RenderHelper.disableStandardItemLighting();
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
	}

	private void drawPageNumbers(FontRenderer fontRendererObj) {
		fontRendererObj.drawString(pageNumDisplayString, pageNumDisplayX, pageNumDisplayY, Color.white.getRGB(), true);
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

	public void handleTick() {
		handleKeyEvent();
		searchField.updateCursorCounter();
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
		searchField.mouseClicked(mouseX, mouseY, mouseButton);
	}

	private void handleKeyEvent() {
		if (overlayEnabled && searchField.isFocused()) {
			boolean textChanged = false;
			while (Keyboard.next()) {
				if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
					searchField.setFocused(false);
					return;
				} else if (Keyboard.getEventKeyState()) {
					searchField.textboxKeyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey());
					textChanged = true;
				}
			}
			if (textChanged) {
				if (JustEnoughItems.itemFilter.setFilterText(searchField.getText()))
					updateLayout();
			}
		} else {
			if (isKeyDown(KeyBindings.toggleOverlay.getKeyCode())) {
				overlayEnabled = !overlayEnabled;
				searchField.setFocused(false);
			}
		}
	}

	private boolean isKeyDown(int key) {
		boolean keyDown = false;
		while (Keyboard.isKeyDown(key) && Keyboard.next()) {
			keyDown = true;
		}
		return keyDown;
	}

	private int getCountPerPage() {
		int xArea = width - (guiLeft + xSize + (2 * borderPadding));
		int yArea = height - (buttonHeight + (2 * borderPadding));

		int xCount = xArea / GuiItemButton.width;
		int yCount = yArea / GuiItemButton.height;

		return xCount * yCount;
	}

	private void updatePageCount() {
		int count = JustEnoughItems.itemFilter.size();
		pageCount = (int) Math.ceil((double) count / (double) getCountPerPage());
		if (pageCount == 0)
			pageCount = 1;
	}

	protected int getPageCount() {
		return pageCount;
	}

	protected int getPageNum() {
		return pageNum;
	}

	protected void setPageNum(int pageNum) {
		if (GuiItemListOverlay.pageNum == pageNum)
			return;
		GuiItemListOverlay.pageNum = pageNum;
		updateLayout();
	}

}
