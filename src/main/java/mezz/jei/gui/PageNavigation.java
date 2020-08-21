package mezz.jei.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Rectangle2d;

import mezz.jei.Internal;
import mezz.jei.gui.elements.GuiIconButton;
import mezz.jei.gui.textures.Textures;
import mezz.jei.input.IPaged;

public class PageNavigation {
	private final IPaged paged;
	private final GuiIconButton nextButton;
	private final GuiIconButton backButton;
	private final boolean hideOnSinglePage;
	private String pageNumDisplayString = "1/1";
	private int pageNumDisplayX;
	private int pageNumDisplayY;

	public PageNavigation(IPaged paged, boolean hideOnSinglePage) {
		this.paged = paged;
		Textures textures = Internal.getTextures();
		this.nextButton = new GuiIconButton(textures.getArrowNext(), b -> paged.nextPage());
		this.backButton = new GuiIconButton(textures.getArrowPrevious(), b -> paged.previousPage());
		this.hideOnSinglePage = hideOnSinglePage;
	}

	public void updateBounds(Rectangle2d area) {
		int buttonSize = area.getHeight();
		this.nextButton.x = area.getX() + area.getWidth() - buttonSize;
		this.nextButton.y = area.getY();
		this.nextButton.setWidth(buttonSize);
		this.nextButton.setHeight(buttonSize);
		this.backButton.x = area.getX();
		this.backButton.y = area.getY();
		this.backButton.setWidth(buttonSize);
		this.backButton.setHeight(buttonSize);
	}

	public void updatePageState() {
		int pageNum = this.paged.getPageNumber();
		int pageCount = this.paged.getPageCount();
		FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
		pageNumDisplayString = (pageNum + 1) + "/" + pageCount;
		int pageDisplayWidth = fontRenderer.getStringWidth(pageNumDisplayString);
		pageNumDisplayX = ((backButton.x + backButton.getWidth()) + nextButton.x) / 2 - (pageDisplayWidth / 2);
		pageNumDisplayY = backButton.y + Math.round((backButton.getWidth_CLASH() - fontRenderer.FONT_HEIGHT) / 2.0f);
	}

	public void draw(Minecraft minecraft, MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		if (!hideOnSinglePage || this.paged.hasNext() || this.paged.hasPrevious()) {
			minecraft.fontRenderer.drawStringWithShadow(matrixStack, pageNumDisplayString, pageNumDisplayX, pageNumDisplayY, 0xFFFFFFFF);
			nextButton.render(matrixStack, mouseX, mouseY, partialTicks);
			backButton.render(matrixStack, mouseX, mouseY, partialTicks);
		}
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return nextButton.isMouseOver(mouseX, mouseY) ||
			backButton.isMouseOver(mouseX, mouseY);
	}

	public boolean handleMouseClickedButtons(double mouseX, double mouseY, int mouseButton) {
		return nextButton.mouseClicked(mouseX, mouseY, mouseButton) ||
			backButton.mouseClicked(mouseX, mouseY, mouseButton);
	}
}
