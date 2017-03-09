package mezz.jei.gui;

import java.awt.Color;
import java.awt.Rectangle;

import mezz.jei.Internal;
import mezz.jei.input.IPaged;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;

public class PageNavigation {
	private final IPaged paged;
	private final GuiButton nextButton;
	private final GuiButton backButton;
	private final boolean hideOnSinglePage;
	private String pageNumDisplayString = "1/1";
	private int pageNumDisplayX;
	private int pageNumDisplayY;
	private Rectangle area;

	public PageNavigation(IPaged paged, boolean hideOnSinglePage, Rectangle area) {
		this.paged = paged;
		int buttonSize = area.height;
		GuiHelper guiHelper = Internal.getHelpers().getGuiHelper();
		this.nextButton = new GuiIconButton(0, area.x + area.width - buttonSize, area.y, buttonSize, buttonSize, guiHelper.getArrowNext());
		this.backButton = new GuiIconButton(1, area.x, area.y, buttonSize, buttonSize, guiHelper.getArrowPrevious());
		this.hideOnSinglePage = hideOnSinglePage;
		this.area = area;
	}

	public void setArea(Rectangle area) {
		this.area = area;
		int buttonSize = area.height;
		this.nextButton.xPosition = area.x + area.width - buttonSize;
		this.nextButton.yPosition = area.y;
		this.nextButton.width = this.nextButton.height = buttonSize;
		this.backButton.xPosition = area.x;
		this.backButton.yPosition = area.y;
		this.backButton.width = this.backButton.height = buttonSize;
	}

	public void updatePageState(int pageNum, int pageCount) {
		FontRenderer fontRendererObj = Minecraft.getMinecraft().fontRendererObj;
		pageNumDisplayString = (pageNum + 1) + "/" + pageCount;
		int pageDisplayWidth = fontRendererObj.getStringWidth(pageNumDisplayString);
		pageNumDisplayX = ((backButton.xPosition + backButton.width) + nextButton.xPosition) / 2 - (pageDisplayWidth / 2);
		pageNumDisplayY = backButton.yPosition + Math.round((backButton.height - fontRendererObj.FONT_HEIGHT) / 2.0f);
	}

	public void draw(Minecraft minecraft, int mouseX, int mouseY) {
		nextButton.enabled = this.paged.hasNext();
		backButton.enabled = this.paged.hasPrevious();

		if (!hideOnSinglePage || nextButton.enabled || backButton.enabled) {
			minecraft.fontRendererObj.drawString(pageNumDisplayString, pageNumDisplayX, pageNumDisplayY, Color.white.getRGB(), true);
			nextButton.drawButton(minecraft, mouseX, mouseY);
			backButton.drawButton(minecraft, mouseX, mouseY);
		}
	}

	public boolean isMouseOver(int mouseX, int mouseY) {
		return area.contains(mouseX, mouseY);
	}

	public boolean handleMouseClickedButtons(Minecraft minecraft, int mouseX, int mouseY) {
		if (nextButton.mousePressed(minecraft, mouseX, mouseY)) {
			paged.nextPage();
			nextButton.playPressSound(minecraft.getSoundHandler());
			return true;
		} else if (backButton.mousePressed(minecraft, mouseX, mouseY)) {
			paged.previousPage();
			backButton.playPressSound(minecraft.getSoundHandler());
			return true;
		}
		return false;
	}
}
