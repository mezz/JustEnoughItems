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
		this.nextButton.field_230690_l_ = area.getX() + area.getWidth() - buttonSize;
		this.nextButton.field_230691_m_ = area.getY();
		this.nextButton.func_230991_b_(buttonSize);
		this.nextButton.setHeight(buttonSize);
		this.backButton.field_230690_l_ = area.getX();
		this.backButton.field_230691_m_ = area.getY();
		this.backButton.func_230991_b_(buttonSize);
		this.backButton.setHeight(buttonSize);
	}

	public void updatePageState() {
		int pageNum = this.paged.getPageNumber();
		int pageCount = this.paged.getPageCount();
		FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
		pageNumDisplayString = (pageNum + 1) + "/" + pageCount;
		int pageDisplayWidth = fontRenderer.getStringWidth(pageNumDisplayString);
		pageNumDisplayX = ((backButton.field_230690_l_ + backButton.func_230998_h_()) + nextButton.field_230690_l_) / 2 - (pageDisplayWidth / 2);
		pageNumDisplayY = backButton.field_230691_m_ + Math.round((backButton.getHeight() - fontRenderer.FONT_HEIGHT) / 2.0f);
	}

	public void draw(Minecraft minecraft, MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		if (!hideOnSinglePage || this.paged.hasNext() || this.paged.hasPrevious()) {
			minecraft.fontRenderer.func_238405_a_(matrixStack, pageNumDisplayString, pageNumDisplayX, pageNumDisplayY, 0xFFFFFFFF);
			nextButton.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);
			backButton.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);
		}
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return nextButton.func_231047_b_(mouseX, mouseY) ||
			backButton.func_231047_b_(mouseX, mouseY);
	}

	public boolean handleMouseClickedButtons(double mouseX, double mouseY, int mouseButton) {
		return nextButton.func_231044_a_(mouseX, mouseY, mouseButton) ||
			backButton.func_231044_a_(mouseX, mouseY, mouseButton);
	}
}
