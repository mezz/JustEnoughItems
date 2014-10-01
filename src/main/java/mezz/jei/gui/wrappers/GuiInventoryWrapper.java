package mezz.jei.gui.wrappers;

import cpw.mods.fml.client.FMLClientHandler;
import mezz.jei.JustEnoughItems;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

import java.awt.Color;

public class GuiInventoryWrapper extends GuiInventory {

	static final int iconPadding = 2;
	static final int iconSize = 16;

	protected GuiButton nextButton;
	protected GuiButton backButton;
	protected int pageNum = 1;

	public GuiInventoryWrapper(GuiInventory gui) {
		super(FMLClientHandler.instance().getClientPlayerEntity());
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		final int buttonWidth = 50;
		final int buttonHeight = 20;
		buttonList.add(nextButton = new GuiButton(-1, this.width - buttonWidth - 4, 0, buttonWidth, buttonHeight, "Next"));
		buttonList.add(backButton = new GuiButton(-2, this.guiLeft + this.xSize + 4, 0, buttonWidth, buttonHeight, "Back"));

		int pageCount = getPageCount();
		if (pageNum > pageCount)
			pageNum = pageCount;
		
		updateButtonEnabled();
	}

	private void updateButtonEnabled() {
		nextButton.enabled = pageNum < getPageCount();
		backButton.enabled = pageNum > 1;
	}

	protected void actionPerformed(GuiButton button) {
		if (button.id == -1 && pageNum < getPageCount()) {
			pageNum++;
		} else if (button.id == -2 && pageNum > 1) {
			pageNum--;
		}
		updateButtonEnabled();
	}

	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of the items)
	 */
	@Override
	protected void drawGuiContainerForegroundLayer(int p_146979_1_, int p_146979_2_) {
		RenderHelper.enableGUIStandardItemLighting();

		final int xStart = xSize + 4;
		final int yStart = -guiTop + backButton.height + 4;

		int x = xStart;
		int y = yStart;
		int maxX = 0;

		for (int i = (pageNum - 1) * getCountPerPage(); i < JustEnoughItems.itemRegistry.itemList.size(); i++) {
			ItemStack stack = JustEnoughItems.itemRegistry.itemList.get(i);
			drawItemStack(stack, x, y);

			x += iconSize + iconPadding;
			if (x + iconSize + guiLeft > width) {
				x = xStart;
				y += iconSize + iconPadding;
			}

			if (y + iconSize + guiTop > height)
				break;

			if (x > maxX)
				maxX = x;
		}

		RenderHelper.disableStandardItemLighting();

		nextButton.xPosition = this.guiLeft + maxX + iconSize - nextButton.width;

		drawPageNumbers();
		updateButtonEnabled();

		super.drawGuiContainerForegroundLayer(p_146979_1_, p_146979_2_);
	}

	private void drawPageNumbers() {
		String pageDisplay = getPageNum() + " / " + getPageCount();
		int pageDisplayWidth = fontRendererObj.getStringWidth(pageDisplay);

		int pageDisplayX = ((backButton.xPosition + backButton.width) + nextButton.xPosition) / 2;
		int pageDisplayY = backButton.yPosition + 6;

		fontRendererObj.drawString(pageDisplay, pageDisplayX - (pageDisplayWidth / 2) - guiLeft, pageDisplayY - guiTop, Color.white.getRGB());
	}

	private void drawItemStack(ItemStack stack, int xPos, int yPos) {
		FontRenderer font = stack.getItem().getFontRenderer(stack);
		if (font == null)
			font = fontRendererObj;
		itemRender.renderItemAndEffectIntoGUI(font, this.mc.getTextureManager(), stack, xPos, yPos);
		itemRender.renderItemOverlayIntoGUI(font, this.mc.getTextureManager(), stack, xPos, yPos);
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

}
