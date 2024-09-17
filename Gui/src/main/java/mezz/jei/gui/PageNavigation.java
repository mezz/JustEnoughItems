package mezz.jei.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.common.Internal;
import mezz.jei.gui.elements.GuiIconButton;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.gui.input.IPaged;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.handlers.CombinedInputHandler;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;

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
			GuiComponent.fill(
				poseStack,
				backButton.getX() + backButton.getWidth(),
				backButton.getY(),
				nextButton.getX(),
				nextButton.getY() + nextButton.getHeight(),
				0x30000000
			);

			Font font = minecraft.font;
			ImmutableRect2i centerArea = MathUtil.centerTextArea(this.area, font, this.pageNumDisplayString);
			Screen.drawString(poseStack, font, pageNumDisplayString, centerArea.getX(), centerArea.getY(), 0xFFFFFFFF);
			nextButton.render(poseStack, mouseX, mouseY, partialTicks);
			backButton.render(poseStack, mouseX, mouseY, partialTicks);
		}
	}

	public ImmutableRect2i getNextButtonArea() {
		return nextButton.getArea();
	}

	public ImmutableRect2i getBackButtonArea() {
		return backButton.getArea();
	}

	public IUserInputHandler createInputHandler() {
		return new CombinedInputHandler(
			"PageNavigation",
			this.nextButton.createInputHandler(),
			this.backButton.createInputHandler()
		);
	}
}
