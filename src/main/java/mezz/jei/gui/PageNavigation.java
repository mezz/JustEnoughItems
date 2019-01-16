package mezz.jei.gui;

import java.awt.Color;
import java.awt.Rectangle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;

import mezz.jei.Internal;
import mezz.jei.gui.elements.GuiIconButton;
import mezz.jei.input.IPaged;

public class PageNavigation {
	private final IPaged paged;
	private final GuiButton nextButton;
	private final GuiButton backButton;
	private final boolean hideOnSinglePage;
	private String pageNumDisplayString = "1/1";
	private int pageNumDisplayX;
	private int pageNumDisplayY;

	public PageNavigation(IPaged paged, boolean hideOnSinglePage) {
		this.paged = paged;
		GuiHelper guiHelper = Internal.getHelpers().getGuiHelper();
		this.nextButton = new GuiIconButton(0, guiHelper.getArrowNext(), (mc, mouseX, mouseY) -> paged.nextPage());
		this.backButton = new GuiIconButton(1, guiHelper.getArrowPrevious(), (mc, mouseX, mouseY) -> paged.previousPage());
		this.hideOnSinglePage = hideOnSinglePage;
	}

	public void updateBounds(Rectangle area) {
		int buttonSize = area.height;
		this.nextButton.x = area.x + area.width - buttonSize;
		this.nextButton.y = area.y;
		this.nextButton.width = this.nextButton.height = buttonSize;
		this.backButton.x = area.x;
		this.backButton.y = area.y;
		this.backButton.width = this.backButton.height = buttonSize;
	}

	public void updatePageState() {
		int pageNum = this.paged.getPageNumber();
		int pageCount = this.paged.getPageCount();
		FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
		pageNumDisplayString = (pageNum + 1) + "/" + pageCount;
		int pageDisplayWidth = fontRenderer.getStringWidth(pageNumDisplayString);
		pageNumDisplayX = ((backButton.x + backButton.width) + nextButton.x) / 2 - (pageDisplayWidth / 2);
		pageNumDisplayY = backButton.y + Math.round((backButton.height - fontRenderer.FONT_HEIGHT) / 2.0f);
	}

	public void draw(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
		if (!hideOnSinglePage || this.paged.hasNext() || this.paged.hasPrevious()) {
			minecraft.fontRenderer.drawString(pageNumDisplayString, pageNumDisplayX, pageNumDisplayY, Color.white.getRGB(), true);
			nextButton.drawButton(minecraft, mouseX, mouseY, partialTicks);
			backButton.drawButton(minecraft, mouseX, mouseY, partialTicks);
		}
	}

	public boolean isMouseOver() {
		return nextButton.isMouseOver() ||
			backButton.isMouseOver();
	}

	public boolean handleMouseClickedButtons(int mouseX, int mouseY) {
		Minecraft minecraft = Minecraft.getMinecraft();
		return nextButton.mousePressed(minecraft, mouseX, mouseY) ||
			backButton.mousePressed(minecraft, mouseX, mouseY);
	}
}
