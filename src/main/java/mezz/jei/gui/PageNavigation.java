package mezz.jei.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.input.mouse.handlers.CombinedInputHandler;
import mezz.jei.input.mouse.IUserInputHandler;
import mezz.jei.util.ImmutableRect2i;
import mezz.jei.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;

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
	private ImmutableRect2i area = ImmutableRect2i.EMPTY;

	public PageNavigation(IPaged paged, boolean hideOnSinglePage) {
		this.paged = paged;
		Textures textures = Internal.getTextures();
		this.nextButton = new GuiIconButton(textures.getArrowNext(), b -> paged.nextPage());
		this.backButton = new GuiIconButton(textures.getArrowPrevious(), b -> paged.previousPage());
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
