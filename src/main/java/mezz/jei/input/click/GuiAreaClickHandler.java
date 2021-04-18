package mezz.jei.input.click;

import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.gui.Focus;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.input.IMouseHandler;
import mezz.jei.util.MathUtil;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;

public class GuiAreaClickHandler implements IMouseHandler {
	private final RecipesGui recipesGui;
	private final IGuiClickableArea clickableArea;
	private final ContainerScreen<?> guiContainer;

	public GuiAreaClickHandler(RecipesGui recipesGui, IGuiClickableArea clickableArea, ContainerScreen<?> guiContainer) {
		this.recipesGui = recipesGui;
		this.clickableArea = clickableArea;
		this.guiContainer = guiContainer;
	}

	@Override
	public boolean handleMouseClicked(double mouseX, double mouseY, int mouseButton, boolean doClick) {
		if (!MathUtil.contains(clickableArea.getArea(), mouseX - guiContainer.getGuiLeft(), mouseY - guiContainer.getGuiTop())) {
			return false;
		}
		if (doClick) {
			clickableArea.onClick(Focus::new, recipesGui);
		}
		return true;
	}
}
