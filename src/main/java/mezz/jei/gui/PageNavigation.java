package mezz.jei.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.input.CombinedMouseHandler;
import mezz.jei.input.IMouseHandler;
import mezz.jei.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.Rect2i;

import mezz.jei.Internal;
import mezz.jei.gui.elements.GuiIconButton;
import mezz.jei.gui.textures.Textures;
import mezz.jei.input.IPaged;
import net.minecraft.util.Tuple;

public class PageNavigation {
	private final IPaged paged;
	private final GuiIconButton nextButton;
	private final GuiIconButton backButton;
	private final boolean hideOnSinglePage;
	private final CombinedMouseHandler mouseHandler;
	private String pageNumDisplayString = "1/1";
	private int pageNumDisplayX;
	private int pageNumDisplayY;
	private Rect2i area = new Rect2i(0, 0, 0, 0);

	public PageNavigation(IPaged paged, boolean hideOnSinglePage) {
		this.paged = paged;
		Textures textures = Internal.getTextures();
		this.nextButton = new GuiIconButton(textures.getArrowNext(), b -> paged.nextPage());
		this.backButton = new GuiIconButton(textures.getArrowPrevious(), b -> paged.previousPage());
		this.mouseHandler = new CombinedMouseHandler(this.nextButton.getMouseHandler(), this.backButton.getMouseHandler());
		this.hideOnSinglePage = hideOnSinglePage;
	}

	public void updateBounds(Rect2i area) {
		this.area = area;
		int buttonSize = area.getHeight();

		Tuple<Rect2i, Rect2i> result = MathUtil.splitX(area, buttonSize);
		this.backButton.updateBounds(result.getA());

		result = MathUtil.splitXRight(area, buttonSize);
		this.nextButton.updateBounds(result.getB());
	}

	public void updatePageState() {
		int pageNum = this.paged.getPageNumber();
		int pageCount = this.paged.getPageCount();
		Minecraft minecraft = Minecraft.getInstance();
		Font fontRenderer = minecraft.font;
		this.pageNumDisplayString = (pageNum + 1) + "/" + pageCount;
		Rect2i centerArea = MathUtil.centerTextArea(this.area, fontRenderer, this.pageNumDisplayString);
		this.pageNumDisplayX = centerArea.getX();
		this.pageNumDisplayY = centerArea.getY();
	}

	public void draw(Minecraft minecraft, PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		if (!hideOnSinglePage || this.paged.hasNext() || this.paged.hasPrevious()) {
			minecraft.font.drawShadow(poseStack, pageNumDisplayString, pageNumDisplayX, pageNumDisplayY, 0xFFFFFFFF);
			nextButton.render(poseStack, mouseX, mouseY, partialTicks);
			backButton.render(poseStack, mouseX, mouseY, partialTicks);
		}
	}

	public IMouseHandler getMouseHandler() {
		return mouseHandler;
	}
}
