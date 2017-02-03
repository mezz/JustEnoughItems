package mezz.jei.gui;

import java.awt.Color;
import java.awt.Rectangle;

import mezz.jei.input.IPaged;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;

public class PageNavigation {
	private static final String nextLabel = ">";
	private static final String backLabel = "<";

	private final IPaged paged;
	private final GuiButton nextButton;
	private final GuiButton backButton;
	private String pageNumDisplayString = "1/1";
	private int pageNumDisplayX;
	private int pageNumDisplayY;
	private Rectangle area;

	public PageNavigation(IPaged paged, Rectangle area) {
		this.paged = paged;
		int buttonSize = area.height;
		this.nextButton = new GuiButton(0, area.x + area.width - buttonSize, area.y, buttonSize, buttonSize, nextLabel);
		this.backButton = new GuiButton(1, area.x, area.y, buttonSize, buttonSize, backLabel);
		this.area = area;
	}

	public void updateLayout(int pageNum, int pageCount) {
		FontRenderer fontRendererObj = Minecraft.getMinecraft().fontRendererObj;
		pageNumDisplayString = (pageNum + 1) + "/" + pageCount;
		int pageDisplayWidth = fontRendererObj.getStringWidth(pageNumDisplayString);
		pageNumDisplayX = ((backButton.xPosition + backButton.width) + nextButton.xPosition) / 2 - (pageDisplayWidth / 2);
		pageNumDisplayY = backButton.yPosition + Math.round((backButton.height - fontRendererObj.FONT_HEIGHT) / 2.0f);
	}

	public void draw(Minecraft minecraft, int mouseX, int mouseY) {
		minecraft.fontRendererObj.drawString(pageNumDisplayString, pageNumDisplayX, pageNumDisplayY, Color.white.getRGB(), true);

		nextButton.drawButton(minecraft, mouseX, mouseY);
		backButton.drawButton(minecraft, mouseX, mouseY);
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
