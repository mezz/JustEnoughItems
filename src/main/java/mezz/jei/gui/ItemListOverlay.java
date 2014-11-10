package mezz.jei.gui;

import cpw.mods.fml.client.FMLClientHandler;
import mezz.jei.JustEnoughItems;
import mezz.jei.KeyBindings;
import mezz.jei.config.Config;
import mezz.jei.util.Commands;
import mezz.jei.util.Permissions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
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

public class ItemListOverlay {

	private static final int borderPadding = 1;
	private static final int searchHeight = 16;
	private static final int itemStackPadding = 1;
	private static final int itemStackWidth = GuiItemStack.getWidth(itemStackPadding);
	private static final int itemStackHeight = GuiItemStack.getHeight(itemStackPadding);
	private static final int maxSearchLength = 32;
	private static int pageNum = 0;

	protected int buttonHeight;
	protected int rightEdge;
	protected int leftEdge;
	protected ArrayList<GuiItemStack> guiItemStacks = new ArrayList<GuiItemStack>();
	protected GuiButton nextButton;
	protected GuiButton backButton;
	protected GuiTextField searchField;
	protected int pageCount;

	protected String pageNumDisplayString;
	protected int pageNumDisplayX;
	protected int pageNumDisplayY;

	protected RecipesGui recipesGui;

	protected GuiItemStack hovered = null;

	// properties of the gui we're beside
	protected int guiLeft;
	protected int guiTop;
	protected int xSize;
	protected int ySize;
	protected int width;
	protected int height;

	private boolean clickHandled = false;
	private boolean overlayEnabled = true;

	public void initGui(GuiContainer guiContainer, RecipesGui recipesGui) {
		this.guiLeft = guiContainer.guiLeft;
		this.guiTop = guiContainer.guiTop;
		this.xSize = guiContainer.xSize;
		this.ySize = guiContainer.ySize;
		this.width = guiContainer.width;
		this.height = guiContainer.height;

		this.recipesGui = recipesGui;

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
		guiItemStacks.clear();

		final int xStart = guiLeft + xSize + borderPadding;
		final int yStart = buttonHeight + (2 * borderPadding);

		int x = xStart;
		int y = yStart;
		int maxX = 0;

		while (y + itemStackHeight + borderPadding <= height - searchHeight) {
			if (x > maxX)
				maxX = x;

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
		if (pageNum >= getPageCount())
			pageNum = 0;
		int i = pageNum * getCountPerPage();

		List<ItemStack> itemList = JustEnoughItems.itemFilter.getItemList();
		for (GuiItemStack itemButton : guiItemStacks) {
			if (i >= itemList.size()) {
				itemButton.clearItemStacks();
			} else {
				ItemStack stack = itemList.get(i);
				itemButton.setItemStacks(stack, null);
			}
			i++;
		}

		FontRenderer fontRendererObj = Minecraft.getMinecraft().fontRenderer;

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

	public void nextPage() {
		if (pageNum == getPageCount() - 1)
			setPageNum(0);
		else
			setPageNum(pageNum + 1);
	}

	public void backPage() {
		if (pageNum == 0)
			setPageNum(getPageCount() - 1);
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

		for (GuiItemStack guiItemStack : guiItemStacks) {
			if (hovered == null && guiItemStack.isMouseOver(mouseX, mouseY))
				hovered = guiItemStack;
			else
				guiItemStack.draw(minecraft);
		}
	}

	public void drawHovered(Minecraft minecraft, int mouseX, int mouseY) {
		if (hovered != null) {
			hovered.drawHovered(minecraft, mouseX, mouseY);
			hovered = null;
		}
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
			for (GuiItemStack guiItemStack : guiItemStacks) {
				if (guiItemStack.isMouseOver(mouseX, mouseY)) {
					handleMouseClickedItemStack(mouseButton, guiItemStack.getItemStack());
				}
			}
		}
		searchField.mouseClicked(mouseX, mouseY, mouseButton);
		recipesGui.handleMouseInput();
	}

	private void handleMouseClickedItemStack(int mouseButton, ItemStack itemStack) {
		EntityClientPlayerMP player = FMLClientHandler.instance().getClientPlayerEntity();
		if (Config.cheatItemsEnabled && Permissions.canPlayerSpawnItems(player) && player.inventory.getFirstEmptyStack() != -1) {
			if (mouseButton == 0) {
				Commands.giveFullStack(itemStack);
			} else if (mouseButton == 1) {
				Commands.giveOneFromStack(itemStack);
			}
		} else {
			boolean success = recipesGui.mouseClickedStack(mouseButton, itemStack);
			if (success)
				recipesGui.setVisible(true);
		}
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
				if (recipesGui.isVisible() && !overlayEnabled)
					recipesGui.setVisible(false);
			}
			if (recipesGui.isVisible() && isKeyDown(Keyboard.KEY_ESCAPE)) {
				recipesGui.setVisible(false);
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

		int xCount = xArea / itemStackWidth;
		int yCount = yArea / itemStackHeight;

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
		if (ItemListOverlay.pageNum == pageNum)
			return;
		ItemListOverlay.pageNum = pageNum;
		updateLayout();
	}

}
