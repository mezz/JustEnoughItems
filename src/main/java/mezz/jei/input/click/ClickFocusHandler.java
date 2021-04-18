package mezz.jei.input.click;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.gui.Focus;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IMouseHandler;
import mezz.jei.input.InputHandler;
import net.minecraft.client.util.InputMappings;

public class ClickFocusHandler implements IMouseHandler {
	private final InputHandler inputHandler;
	private final RecipesGui recipesGui;

	public ClickFocusHandler(InputHandler inputHandler, RecipesGui recipesGui) {
		this.inputHandler = inputHandler;
		this.recipesGui = recipesGui;
	}

	@Override
	public boolean handleMouseClicked(double mouseX, double mouseY, int mouseButton, boolean doClick) {
		IClickedIngredient<?> clicked = inputHandler.getFocusUnderMouseForClick(mouseX, mouseY);
		if (clicked != null) {
			if (handleMouseClickedFocus(mouseButton, clicked, doClick)) {
				return true;
			}
			InputMappings.Input input = InputMappings.Type.MOUSE.getOrMakeInput(mouseButton);
			return inputHandler.handleFocusKeybinds(clicked, input, doClick);
		}
		return false;
	}

	private <V> boolean handleMouseClickedFocus(int mouseButton, IClickedIngredient<V> clicked, boolean doClick) {
		if (mouseButton == 0) {
			if (doClick) {
				Focus<?> focus = new Focus<>(IFocus.Mode.OUTPUT, clicked.getValue());
				recipesGui.show(focus);
				clicked.onClickHandled();
			}
			return true;
		} else if (mouseButton == 1) {
			if (doClick) {
				Focus<?> focus = new Focus<>(IFocus.Mode.INPUT, clicked.getValue());
				recipesGui.show(focus);
				clicked.onClickHandled();
			}
			return true;
		}

		return false;
	}
}
