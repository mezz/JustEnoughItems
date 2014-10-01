package mezz.jei.gui.wrappers;

import cpw.mods.fml.client.FMLClientHandler;
import mezz.jei.JustEnoughItems;
import mezz.jei.gui.GuiItemIcon;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import java.awt.Color;
import java.util.ArrayList;

public class GuiInventoryWrapper extends GuiInventory {

	static final int iconPadding = 2;
	static final int iconSize = 16;

	protected ArrayList<GuiItemIcon> items = new ArrayList<GuiItemIcon>();

	protected GuiButton nextButton;
	protected GuiButton backButton;
	protected static int pageNum = 1;

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
		String next = StatCollector.translateToLocal("jei.button.next");
		String back = StatCollector.translateToLocal("jei.button.back");
		buttonList.add(nextButton = new GuiButton(-1, this.width - buttonWidth - 4, 0, buttonWidth, buttonHeight, next));
		buttonList.add(backButton = new GuiButton(-2, this.guiLeft + this.xSize + 4, 0, buttonWidth, buttonHeight, back));

		int pageCount = getPageCount();
		if (pageNum > pageCount)
			setPageNum(pageCount);

		updatePage();
		updateButtonEnabled();
	}

	private void updatePage() {
		items.clear();

		final int xStart = guiLeft + xSize + 4;
		final int yStart = backButton.height + 4;

		int x = xStart;
		int y = yStart;
		int maxX = 0;

		for (int i = (pageNum - 1) * getCountPerPage(); i < JustEnoughItems.itemRegistry.itemList.size(); i++) {
			ItemStack stack = JustEnoughItems.itemRegistry.itemList.get(i);
			items.add(new GuiItemIcon(stack, x, y));

			x += iconSize + iconPadding;
			if (x + iconSize > width) {
				x = xStart;
				y += iconSize + iconPadding;
			}

			if (y + iconSize > height)
				break;

			if (x > maxX)
				maxX = x;
		}

		nextButton.xPosition = maxX + iconSize - nextButton.width;
	}

	private void updateButtonEnabled() {
		nextButton.enabled = pageNum < getPageCount();
		backButton.enabled = pageNum > 1;
	}

	protected void actionPerformed(GuiButton button) {
		if (button.id == -1 && pageNum < getPageCount()) {
			setPageNum(pageNum + 1);
		} else if (button.id == -2 && pageNum > 1) {
			setPageNum(pageNum - 1);
		}
		updateButtonEnabled();
	}

	@Override
	protected void mouseClicked(int xPos, int yPos, int mouseButton) {
		for (GuiItemIcon itemIcon : items) {
			if (itemIcon.isMouseOver(xPos, yPos)) {
				itemIcon.mouseClicked(xPos, yPos, mouseButton);
				return;
			}
		}
		super.mouseClicked(xPos, yPos, mouseButton);
	}

	@Override
	public void drawScreen(int p_73863_1_, int p_73863_2_, float p_73863_3_) {
		super.drawScreen(p_73863_1_, p_73863_2_, p_73863_3_);

		TextureManager textureManager = this.mc.getTextureManager();

		RenderHelper.enableGUIStandardItemLighting();

		for (GuiItemIcon itemIcon : items)
			itemIcon.draw(itemRender, fontRendererObj, textureManager);

		RenderHelper.disableStandardItemLighting();

		drawPageNumbers();
	}

	private void drawPageNumbers() {
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
		if (this.pageNum == pageNum)
			return;
		this.pageNum = pageNum;
		updatePage();
	}

}
