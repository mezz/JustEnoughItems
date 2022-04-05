package mezz.jei.common.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.common.gui.elements.GuiIconButton;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.IPaged;
import mezz.jei.common.input.IUserInputHandler;
import mezz.jei.common.input.handlers.CombinedInputHandler;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;

public class PageNavigation {
	private final IPaged paged;
	private final GuiIconButton nextButton;
	private final GuiIconButton backButton;
	private final boolean hideOnSinglePage;
	private String pageNumDisplayString = "1/1";
	private ImmutableRect2i area = ImmutableRect2i.EMPTY;

	public PageNavigation(IPaged paged, boolean hideOnSinglePage, Textures textures) {
		this.paged = paged;
		this.nextButton = new GuiIconButton(textures.getArrowNext(), b -> paged.nextPage(), textures);
		this.backButton = new GuiIconButton(textures.getArrowPrevious(), b -> paged.previousPage(), textures);
		this.hideOnSinglePage = hideOnSinglePage;
	}

	private boolean isVisible() {
		if (area.isEmpty()) {
			return false;
		}
		return !hideOnSinglePage || this.paged.hasNext() || this.paged.hasPrevious();
	}

	public void updateBounds(ImmutableRect2i area) {
		this.area = area;
		int buttonSize = area.getHeight();

		ImmutableRect2i backArea = area.keepLeft(buttonSize);
		this.backButton.updateBounds(backArea);

		ImmutableRect2i nextArea = area.keepRight(buttonSize);
		this.nextButton.updateBounds(nextArea);
	}

	public void updatePageNumber() {
		int pageNum = this.paged.getPageNumber();
		int pageCount = this.paged.getPageCount();
		this.pageNumDisplayString = String.format("%d/%d", pageNum + 1, pageCount);
	}

	public void draw(Minecraft minecraft, PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		if (isVisible()) {
			GuiComponent.fill(poseStack,
				backButton.x + backButton.getWidth(),
				backButton.y,
				nextButton.x,
				nextButton.y + nextButton.getHeight(),
				0x30000000);

			Font font = minecraft.font;
			ImmutableRect2i centerArea = MathUtil.centerTextArea(this.area, font, this.pageNumDisplayString);
			font.drawShadow(poseStack, pageNumDisplayString, centerArea.getX(), centerArea.getY(), 0xFFFFFFFF);
			nextButton.render(poseStack, mouseX, mouseY, partialTicks);
			backButton.render(poseStack, mouseX, mouseY, partialTicks);
		}
	}

	public IUserInputHandler createInputHandler() {
		return new CombinedInputHandler(
			this.nextButton.createInputHandler(),
			this.backButton.createInputHandler()
		);
	}
}
