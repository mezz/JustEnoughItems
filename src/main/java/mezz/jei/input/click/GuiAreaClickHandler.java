package mezz.jei.input.click;

import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.gui.Focus;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.input.IMouseHandler;
import mezz.jei.util.MathUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import javax.annotation.Nullable;

public class GuiAreaClickHandler implements IMouseHandler {
	private final RecipesGui recipesGui;
	private final IGuiClickableArea clickableArea;
	private final AbstractContainerScreen<?> guiContainer;

	public GuiAreaClickHandler(RecipesGui recipesGui, IGuiClickableArea clickableArea, AbstractContainerScreen<?> guiContainer) {
		this.recipesGui = recipesGui;
		this.clickableArea = clickableArea;
		this.guiContainer = guiContainer;
	}

	@Override
	@Nullable
	public IMouseHandler handleClick(Screen screen, double mouseX, double mouseY, int mouseButton, MouseClickState clickState) {
		if (!MathUtil.contains(clickableArea.getArea(), mouseX - guiContainer.getGuiLeft(), mouseY - guiContainer.getGuiTop())) {
			return null;
		}
		if (!clickState.isSimulate()) {
			clickableArea.onClick(Focus::new, recipesGui);
		}
		return this;
	}
}
