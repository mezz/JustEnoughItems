package mezz.jei.input.mouse.handlers;

import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.gui.Focus;
import mezz.jei.gui.GuiScreenHelper;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.input.UserInput;
import mezz.jei.input.mouse.IUserInputHandler;
import mezz.jei.util.MathUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;

import javax.annotation.Nullable;

public class GuiAreaClickHandler implements IUserInputHandler {
	private final GuiScreenHelper guiScreenHelper;
	private final RecipesGui recipesGui;

	public GuiAreaClickHandler(GuiScreenHelper guiScreenHelper, RecipesGui recipesGui) {
		this.guiScreenHelper = guiScreenHelper;
		this.recipesGui = recipesGui;
	}

	@Nullable
	@Override
	public IUserInputHandler handleUserInput(Screen screen, UserInput input) {
		if (!input.isLeftClick()) {
			return null;
		}
		if (screen instanceof AbstractContainerScreen<?> guiContainer) {
			double mouseX = input.getMouseX();
			double mouseY = input.getMouseY();
			IGuiClickableArea clickableArea = guiScreenHelper.getGuiClickableArea(guiContainer, mouseX - guiContainer.getGuiLeft(), mouseY - guiContainer.getGuiTop());
			if (clickableArea != null) {
				IUserInputHandler clickHandler = new InnerHandler(recipesGui, clickableArea, guiContainer);
				return clickHandler.handleUserInput(screen, input);
			}
		}
		return null;
	}

	private static class InnerHandler implements IUserInputHandler {
		private final RecipesGui recipesGui;
		private final IGuiClickableArea clickableArea;
		private final AbstractContainerScreen<?> guiContainer;

		public InnerHandler(RecipesGui recipesGui, IGuiClickableArea clickableArea, AbstractContainerScreen<?> guiContainer) {
			this.recipesGui = recipesGui;
			this.clickableArea = clickableArea;
			this.guiContainer = guiContainer;
		}

		@Override
		@Nullable
		public IUserInputHandler handleUserInput(Screen screen, UserInput input) {
			Rect2i area = MathUtil.copyRect(clickableArea.getArea());
			area.setX(area.getX() + guiContainer.getGuiLeft());
			area.setY(area.getY() + guiContainer.getGuiTop());

			if (!MathUtil.contains(area, input.getMouseX(), input.getMouseY())) {
				return null;
			}
			if (!input.isSimulate()) {
				clickableArea.onClick(Focus::new, recipesGui);
			}
			return this;
		}
	}

}
